package org.goosepjkt.bobomb.ui.dialog

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.goosepjkt.bobomb.BuildVars
import org.goosepjkt.bobomb.R
import org.goosepjkt.bobomb.databinding.DialogSettingsBinding
import org.goosepjkt.bobomb.ui.MainRepository
import org.goosepjkt.bobomb.ui.MainViewModel
import org.goosepjkt.bobomb.ui.adapters.BobombWorkAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsDialog : BottomSheetDialogFragment() {
    private val repository: MainRepository by lazy { MainRepository(requireContext()) }

    private val binding: DialogSettingsBinding by lazy {
        DialogSettingsBinding.inflate(getLayoutInflater())
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (dialog as BottomSheetDialog).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED)

        val workManager = WorkManager.getInstance(requireContext())
        val model by activityViewModels<MainViewModel>()

        val bobombWorkAdapter = BobombWorkAdapter(
            requireActivity()
        ) { workInfo: WorkInfo? ->
            workManager.cancelWorkById(workInfo!!.id)
        }

        model.scheduledAttacks.observe(this) { workInfoResult: List<WorkInfo> ->
            bobombWorkAdapter.setWorkInfo(workInfoResult)
            bobombWorkAdapter.notifyDataSetChanged()
        }

        model.servicesCount.observe(this) { servicesCount: Int ->
            binding.settingsServicesCount.text = servicesCount.toString()
        }


        binding.tasks.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.tasks.setAdapter(bobombWorkAdapter)

        binding.themeTile.setChecked(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        binding.themeTile.setOnClickListener {
            setCurrentTheme(
                if (binding.themeTile.isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.themeTile.setOnLongClickListener {
            setCurrentTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            true
        }


        binding.recentTile.setOnClickListener {
            showRecentNumbersDialog()
        }

        binding.proxyCard.setOnClickListener { ProxiesDialog().show(getParentFragmentManager(), null) }

        model.proxyEnabled.observe(getViewLifecycleOwner()) { enabled: Boolean? ->
            binding.proxySwitch.setEnabled(repository.proxy.isNotEmpty())
            binding.proxySwitch.setChecked(enabled!!)
        }

        binding.proxySwitch.setOnCheckedChangeListener { _, isChecked ->
            model.setProxyEnabled(isChecked)
        }

        binding.servicesCard.setOnClickListener {
            RepositoriesDialog().show(getParentFragmentManager(), null)
        }


        TooltipCompat.setTooltipText(binding.proxyTile, getString(R.string.proxy))
        TooltipCompat.setTooltipText(binding.recentTile, "Недавние номера")

        return binding.getRoot()
    }

    private fun showRecentNumbersDialog() {
        val recentNumbers = repository.getRecentNumbers()

        if (recentNumbers.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Недавние номера")
                .setMessage("Список недавних номеров пуст")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val items = recentNumbers.map { it }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Недавние номера")
            .setItems(items) { _, which ->
                val selectedNumber = recentNumbers[which].substring(1)
                (activity as? org.goosepjkt.bobomb.ui.MainActivity)?.let { mainActivity ->
                    mainActivity.setPhoneNumber(selectedNumber)
                }
            }
            .setPositiveButton("OK", null)
            .setNeutralButton("Очистить") { _, _ ->
                repository.clearRecentNumbers()
                Snackbar.make(binding.root, "Недавние номера очищены", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setCurrentTheme(theme: Int) {
        AppCompatDelegate.setDefaultNightMode(theme)
        repository.theme = theme
    }
}
