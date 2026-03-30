package org.gooseprjkt.bobomb.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
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
import org.gooseprjkt.bobomb.BuildVars
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.databinding.DialogSettingsBinding
import org.gooseprjkt.bobomb.ui.MainRepository
import org.gooseprjkt.bobomb.ui.MainViewModel
import org.gooseprjkt.bobomb.ui.adapters.BobombWorkAdapter
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

        // Применяем моноширинный шрифт если включено
        applyMonospaceFontIfNeeded(binding.root)
        
        // Применяем шрифт к RecyclerView (список задач)
        model.scheduledAttacks.observe(this) { workInfoResult: List<WorkInfo> ->
            bobombWorkAdapter.setWorkInfo(workInfoResult)
            bobombWorkAdapter.notifyDataSetChanged()
            applyMonospaceFontIfNeeded(binding.tasks)
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

        binding.proxyCard.setOnClickListener { ProxyManagerDialog().show(getParentFragmentManager(), null) }

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
        RecentNumbersDialog().show(parentFragmentManager, "RecentNumbersDialog")
    }

    private fun setCurrentTheme(theme: Int) {
        AppCompatDelegate.setDefaultNightMode(theme)
        repository.theme = theme
    }

    private fun applyMonospaceFontIfNeeded(view: View) {
        val prefs = requireContext().getSharedPreferences("bobomb_experiments", Context.MODE_PRIVATE)
        if (prefs.getBoolean("monospace_font", false)) {
            applyMonospaceFont(view, android.graphics.Typeface.MONOSPACE)
        }
    }

    private fun applyMonospaceFont(view: View, typeface: android.graphics.Typeface) {
        when (view) {
            is android.widget.TextView -> view.typeface = typeface
            is android.widget.Button -> view.typeface = typeface
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
