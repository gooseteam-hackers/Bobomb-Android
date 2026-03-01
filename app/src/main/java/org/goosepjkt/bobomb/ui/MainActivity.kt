package org.goosepjkt.bobomb.ui

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.goosepjkt.bobomb.BuildConfig
import org.goosepjkt.bobomb.BuildVars
import org.goosepjkt.bobomb.R
import org.goosepjkt.bobomb.databinding.ActivityMainBinding
import org.goosepjkt.bobomb.ui.adapters.CountryCodeAdapter
import org.goosepjkt.bobomb.ui.dialog.AdvertisingDialog
import org.goosepjkt.bobomb.ui.dialog.ExperimentsDialog
import org.goosepjkt.bobomb.ui.dialog.RepositoriesDialog
import org.goosepjkt.bobomb.ui.dialog.SettingsDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.blurry.Blurry
import java.util.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val repository: MainRepository by lazy { MainRepository(this) }
    private val inputManager: InputMethodManager by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }
    private val workManager: WorkManager by lazy { WorkManager.getInstance(this) }

    private val model by viewModels<MainViewModel> { MainModelFactory(this, repository, workManager) }

    private var clipText: String? = null
    private var advertisingAvailable = false

    @SuppressLint("SetTextI18n", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {

        theme.applyStyle(R.style.ThemeOverlay_Material3Expressive, true)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)

        setContentView(binding.getRoot())

        model.progress.observe(this) { progress ->

            binding.wavyProgress.setIndeterminate(false)


            val percentage = if (progress.maxProgress > 0) {
                (progress.currentProgress * 100 / progress.maxProgress)
            } else 0

            binding.wavyProgress.setProgressCompat(percentage, true)
            binding.attackCountText.text = "${progress.currentProgress}/${progress.maxProgress}  ${percentage}%"
        }

        model.workStatus.observe(this) { workStatus: Boolean ->
            if (workStatus) {
                binding.getRoot().requestLayout()
                binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(BlurListener())


                binding.attackToolbar.visibility = View.VISIBLE
            } else {
                repeat(binding.getRoot().childCount) { i ->
                    val view = binding.getRoot().getChildAt(i)

                    view.visibility = View.VISIBLE
                }

                binding.workScreen.visibility = View.GONE
                binding.attackToolbar.visibility = View.GONE
            }
        }


        model.getAdvertisingAvailable().observe(this) { available: Boolean? ->
            advertisingAvailable = available!!
        }

        val limitSchedule = OnLongClickListener { view: View? ->
            inputManager.hideSoftInputFromWindow(binding.getRoot().windowToken, 0)
            Snackbar.make(view!!, R.string.limit_reached, Snackbar.LENGTH_LONG).show()
            true
        }

        val schedule = OnLongClickListener { view: View? ->
            inputManager.hideSoftInputFromWindow(binding.getRoot().windowToken, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.setData(Uri.parse("package:$packageName"))
                    startActivity(intent)
                }
            }

            val phoneNumber = binding.phoneNumber.getText().toString()
            val repeats = binding.repeats.getText().toString()

            val currentDate = Calendar.getInstance()
            val date = Calendar.getInstance()

            if (checkPhoneNumberLength(phoneNumber, currentPhoneCodeMaxPhoneLength)) DatePickerDialog(
                this@MainActivity,
                { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    date[year, monthOfYear] = dayOfMonth
                    TimePickerDialog(
                        this@MainActivity,
                        OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                            date[Calendar.HOUR_OF_DAY] = hourOfDay
                            date[Calendar.MINUTE] = minute

                            if (date.getTimeInMillis() < currentDate.getTimeInMillis()) {
                                Snackbar.make(view!!, R.string.time_is_incorrect, Snackbar.LENGTH_LONG).show()
                                return@OnTimeSetListener
                            }

                            showAdvertisingWithCallback {
                                model.scheduleAttack(
                                    BuildVars.COUNTRY_CODES[binding.phoneCode.selectedItemPosition], phoneNumber,
                                    if (repeats.isEmpty()) 1 else repeats.toInt(),
                                    date.getTimeInMillis(), currentDate.getTimeInMillis()
                                )

                                SettingsDialog().show(supportFragmentManager, null)
                            }
                        },
                        currentDate[Calendar.HOUR_OF_DAY],
                        currentDate[Calendar.MINUTE],
                        true
                    ).show()
                },
                currentDate[Calendar.YEAR],
                currentDate[Calendar.MONTH],
                currentDate[Calendar.DATE]
            ).show()

            true
        }

        model.scheduledAttacks.observe(this) { attacks: List<WorkInfo?> ->
            binding.startAttack.setOnLongClickListener(
                if (attacks.size >= BuildVars.SCHEDULED_ATTACKS_LIMIT) limitSchedule else schedule
            )
        }


        model.servicesCount.observe(this) { count ->
            binding.servicesCount.text = count.toString()
        }

        model.repositoriesProgress.observe(this) { progress ->
            binding.repositoriesLoading.setMax(progress.maxProgress)
            binding.repositoriesLoading.setProgress(progress.currentProgress)
        }

        val countryCodeAdapter = CountryCodeAdapter(this, BuildVars.COUNTRY_FLAGS, BuildVars.COUNTRY_CODES)

        val hints = resources.getStringArray(R.array.hints)

        binding.phoneNumber.setHint(hints[0])
        binding.phoneCode.setAdapter(countryCodeAdapter)

        binding.phoneCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, l: Long) {
                binding.phoneNumber.setHint(hints[index])
                model.selectCountryCode(BuildVars.COUNTRY_CODES[index])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }


        binding.repeatsIcon.visibility = View.VISIBLE


        binding.repeats.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val text = s?.toString() ?: ""
                binding.repeatsIcon.visibility = if (text.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.repeats.setFilters(
            arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                try {
                    val value = (dest.subSequence(0, dstart).toString() +
                            source.subSequence(start, end).toString() +
                            dest.subSequence(dend, dest.length))


                    if (value.isEmpty()) {
                        return@InputFilter null
                    }

                    val repeats = value.toInt()


                    if (repeats in 1..10) {
                        return@InputFilter null
                    }
                } catch (ignored: NumberFormatException) {
                }
                ""
            })
        )

        binding.startAttack.setOnClickListener {
            try {

                inputManager.hideSoftInputFromWindow(binding.getRoot().windowToken, 0)

                val phoneNumber = binding.phoneNumber.getText().toString()
                val repeats = binding.repeats.getText().toString()

                if (checkPhoneNumberLength(phoneNumber, currentPhoneCodeMaxPhoneLength)) {
                    showAdvertisingWithCallback {
                        repository.lastCountryCode = binding.phoneCode.selectedItemPosition
                        repository.lastPhone = phoneNumber

                        model.startAttack(
                            BuildVars.COUNTRY_CODES[binding.phoneCode.selectedItemPosition], phoneNumber,
                            if (repeats.isEmpty()) 1 else repeats.toInt()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting attack", e)
            }
        }


        binding.startAttack.setOnLongClickListener {
            inputManager.hideSoftInputFromWindow(binding.getRoot().windowToken, 0)

            val phoneNumber = binding.phoneNumber.getText().toString()
            val repeats = binding.repeats.getText().toString()

            if (checkPhoneNumberLength(phoneNumber, currentPhoneCodeMaxPhoneLength)) {
                // First show date/time picker for scheduling
                val intent = Intent()
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.setData(Uri.parse("package:$packageName"))
                    startActivity(intent)
                }

                val currentDate = Calendar.getInstance()
                val date = Calendar.getInstance()

                DatePickerDialog(
                    this@MainActivity,
                    { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        date[year, monthOfYear] = dayOfMonth
                        TimePickerDialog(
                            this@MainActivity,
                            OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                                date[Calendar.HOUR_OF_DAY] = hourOfDay
                                date[Calendar.MINUTE] = minute

                                if (date.getTimeInMillis() < currentDate.getTimeInMillis()) {
                                    Snackbar.make(binding.root, R.string.time_is_incorrect, Snackbar.LENGTH_LONG).show()
                                    return@OnTimeSetListener
                                }

                                // After selecting time, show attack mode selector
                                val prefs = getSharedPreferences("bobomb_experiments", Context.MODE_PRIVATE)
                                val dripModeEnabled = prefs.getBoolean("drip_mode_enabled", false)

                                val items = if (dripModeEnabled) {
                                    arrayOf("Обычная атака", "Капельный режим 💧", "Расписание")
                                } else {
                                    arrayOf("Обычная атака", "Расписание")
                                }

                                MaterialAlertDialogBuilder(this)
                                    .setTitle("Режим атаки")
                                    .setItems(items) { _, which ->
                                        if (dripModeEnabled) {
                                            when (which) {
                                                0 -> startNormalAttack(phoneNumber, repeats)
                                                1 -> startDripModeAttack(phoneNumber, repeats)
                                                2 -> scheduleAttackWithDate(phoneNumber, repeats, date, currentDate)
                                            }
                                        } else {
                                            when (which) {
                                                0 -> startNormalAttack(phoneNumber, repeats)
                                                1 -> scheduleAttackWithDate(phoneNumber, repeats, date, currentDate)
                                            }
                                        }
                                    }
                                    .show()
                            },
                            currentDate[Calendar.HOUR_OF_DAY],
                            currentDate[Calendar.MINUTE],
                            true
                        ).show()
                    },
                    currentDate[Calendar.YEAR],
                    currentDate[Calendar.MONTH],
                    currentDate[Calendar.DATE]
                ).show()
            }

            true // Consume the long click event
        }

        binding.stopButton.setOnClickListener { model.cancelCurrentWork() }

        binding.bomb.setOnLongClickListener {

            val snackbar = Snackbar.make(binding.getRoot(), R.string.hacker_mode_activated, Snackbar.LENGTH_LONG)
            snackbar.setAction(R.string.experiments) {
                ExperimentsDialog().show(supportFragmentManager, null)
            }
            snackbar.show()
            false
        }

        binding.bomb.setOnClickListener { view: View ->
            view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(90)
                            .setListener(null)
                            .start()
                    }
                })
                .start()
        }


        binding.appTitle.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Bobomb")
                .setMessage("Открытый SMS бомбер на Android\n\nВерсия 2.01 (код 201)\n\nGitHub: https://github.com/gooseteam-hackers/Bobomb-Android.git")
                .setIcon(R.drawable.bobomb_logo)
                .setPositiveButton("OK", null)
                .setNeutralButton("GitHub") { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildVars.SOURCECODE_URL)))
                }
                .setNegativeButton("Telegram") { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/itgoose_adapter")))
                }
                .show()
        }

        binding.phoneNumber.setOnLongClickListener {
            if (binding.phoneNumber.getText().toString()
                    .isEmpty() && clipText != null && !processPhoneNumber(clipText!!)
            ) {
                binding.phoneCode.setSelection(repository.lastCountryCode)
                binding.phoneNumber.setText(repository.lastPhone)
            }

            false
        }

        binding.settings.setOnClickListener {
            try {
                val settingsDialog = SettingsDialog()
                settingsDialog.show(supportFragmentManager, null)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error showing settings dialog", e)
            }
        }
        binding.servicesCount.setOnClickListener { RepositoriesDialog().show(supportFragmentManager, null) }


        binding.wavyProgress.setIndeterminate(true)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result: Boolean? ->
                if (!result!!) {
                    Snackbar.make(
                        binding.getRoot(),
                        R.string.notification_permission,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (intent != null) {
            if (Intent.ACTION_DIAL == intent.action)
                processPhoneNumber(intent.data!!.schemeSpecificPart)

            if (intent.hasExtra(TASK_ID)) {
                val taskId = UUID.fromString(intent.getStringExtra(TASK_ID))
                workManager.cancelWorkById(taskId)

                SettingsDialog().show(supportFragmentManager, null)

                val notificationManager = NotificationManagerCompat.from(applicationContext)
                notificationManager.cancel(taskId.hashCode())
            }
        }


        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    model.cancelCurrentWork()

                    if (binding.workScreen.visibility != View.VISIBLE)
                        finish()
                }
            })
    }


    private fun processPhoneNumber(data: String): Boolean {
        var phoneNumber = data

        if (phoneNumber.matches("(8|\\+(7|380|375|77))([\\d()\\-\\s])*".toRegex())) {
            if (phoneNumber.startsWith("8"))
                phoneNumber = "+7" + phoneNumber.substring(1)

            phoneNumber = phoneNumber.substring(1)

            for (i in BuildVars.COUNTRY_CODES.indices) {
                if (phoneNumber.startsWith(BuildVars.COUNTRY_CODES[i])) {
                    binding.phoneCode.setSelection(i)
                    binding.phoneNumber.setText(
                        phoneNumber
                            .substring(BuildVars.COUNTRY_CODES[i].length)
                            .replace("[^\\d.]", "")
                    )

                    return true
                }
            }
        }

        return false
    }

    private fun showAdvertisingWithCallback(callback: () -> Unit) {
        if (!advertisingAvailable) {
            callback()
            return
        }

        AdvertisingDialog().show(supportFragmentManager, null)

        model.advertisingTrigger.value = false

        model.advertisingTrigger.observeForever(object : Observer<Boolean?> {
            override fun onChanged(value: Boolean?) {
                if (value != true) return
                callback()
                model.advertisingTrigger.removeObserver(this)
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                try {
                    val clipData = clipboard.primaryClip
                    if (clipData != null)
                        clipText = clipData.getItemAt(0).coerceToText(this).toString()
                } catch (ignored: SecurityException) {
                }
            }
        }
    }

    private inner class BlurListener : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            try {
                binding.blur.setImageBitmap(
                    Blurry.with(this@MainActivity)
                        .radius(20)
                        .sampling(2)
                        .capture(binding.getRoot())
                        .get()
                )
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }

            repeat(binding.getRoot().childCount) { i ->
                val view = binding.getRoot().getChildAt(i)
                view.visibility = View.GONE
            }

            binding.workScreen.visibility = View.VISIBLE
            binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                SettingsDialog().show(supportFragmentManager, null)
                true
            }
            R.id.action_services -> {
                RepositoriesDialog().show(supportFragmentManager, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPhoneNumberLength(phoneNumber: String, length: Int): Boolean {
        if (phoneNumber.length != length && length != BuildVars.PHONE_ANY_LENGTH || length == BuildVars.PHONE_ANY_LENGTH && phoneNumber.length < 5) {
            Snackbar.make(binding.getRoot(), R.string.phone_error, Snackbar.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun setPhoneNumber(phoneNumber: String) {
        binding.phoneNumber.setText(phoneNumber)
    }

    private fun startNormalAttack(phoneNumber: String, repeats: String) {
        showAdvertisingWithCallback {
            repository.lastCountryCode = binding.phoneCode.selectedItemPosition
            repository.lastPhone = phoneNumber
            repository.addRecentNumber("$phoneNumber")

            model.startAttack(
                BuildVars.COUNTRY_CODES[binding.phoneCode.selectedItemPosition], phoneNumber,
                if (repeats.isEmpty()) 1 else repeats.toInt()
            )
        }
    }

    private fun startDripModeAttack(phoneNumber: String, repeats: String) {
        showAdvertisingWithCallback {
            repository.lastCountryCode = binding.phoneCode.selectedItemPosition
            repository.lastPhone = phoneNumber
            repository.addRecentNumber("$phoneNumber")

            model.startDripAttack(
                BuildVars.COUNTRY_CODES[binding.phoneCode.selectedItemPosition], phoneNumber,
                if (repeats.isEmpty()) 1 else repeats.toInt()
            )
        }
    }

    private fun showSchedulePicker(phoneNumber: String, repeats: String) {
        val intent = Intent()
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        val currentDate = Calendar.getInstance()
        val date = Calendar.getInstance()

        DatePickerDialog(
            this@MainActivity,
            { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                date[year, monthOfYear] = dayOfMonth
                TimePickerDialog(
                    this@MainActivity,
                    OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                        date[Calendar.HOUR_OF_DAY] = hourOfDay
                        date[Calendar.MINUTE] = minute

                        if (date.getTimeInMillis() < currentDate.getTimeInMillis()) {
                            Snackbar.make(binding.root, R.string.time_is_incorrect, Snackbar.LENGTH_LONG).show()
                            return@OnTimeSetListener
                        }

                        showAdvertisingWithCallback {
                            model.scheduleAttack(
                                BuildVars.COUNTRY_CODES[binding.phoneCode.selectedItemPosition], phoneNumber,
                                if (repeats.isEmpty()) 1 else repeats.toInt(),
                                date.getTimeInMillis(), currentDate.getTimeInMillis()
                            )

                            SettingsDialog().show(supportFragmentManager, null)
                        }
                    },
                    currentDate[Calendar.HOUR_OF_DAY],
                    currentDate[Calendar.MINUTE],
                    true
                ).show()
            },
            currentDate[Calendar.YEAR],
            currentDate[Calendar.MONTH],
            currentDate[Calendar.DATE]
        ).show()
    }

    private fun scheduleAttackWithDate(phoneNumber: String, repeats: String, date: Calendar, currentDate: Calendar) {
        showAdvertisingWithCallback {
            model.scheduleAttack(
                BuildVars.COUNTRY_CODES[binding.phoneCode.selectedItemPosition], phoneNumber,
                if (repeats.isEmpty()) 1 else repeats.toInt(),
                date.getTimeInMillis(), currentDate.getTimeInMillis()
            )
            SettingsDialog().show(supportFragmentManager, null)
        }
    }

    private val currentPhoneCodeMaxPhoneLength: Int
        get() = BuildVars.MAX_PHONE_LENGTH[binding.phoneCode.selectedItemPosition]

    companion object {
        const val TASK_ID = "task_id"
    }
}