package org.gooseprjkt.bobomb.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.gooseprjkt.bobomb.BuildVars.AttackSpeed
import org.gooseprjkt.bobomb.BuildVars.DripModeType
import org.gooseprjkt.bobomb.databinding.DialogRepositoriesBinding
import org.gooseprjkt.bobomb.ui.MainRepository

class RepositoriesDialog : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogRepositoriesBinding.inflate(inflater)
        val repository = MainRepository(requireContext())

        // Устанавливаем текущие значения переключателей
        binding.remoteServices.isChecked = repository.isRemoteServicesEnabled
        binding.callCenters.isChecked = repository.isCallCentersEnabled
        binding.dripMode.isChecked = repository.isDripModeEnabled

        // Устанавливаем текущую скорость
        setupSpeedSelection(binding, repository)
        
        // Применяем моноширинный шрифт
        applyMonospaceFontIfNeeded(binding.root)

        // Обработчик переключения удалённых сервисов
        binding.remoteServices.setOnCheckedChangeListener { _, isChecked ->
            repository.isRemoteServicesEnabled = isChecked
        }

        // Обработчик переключения колл-центров
        binding.callCenters.setOnCheckedChangeListener { _, isChecked ->
            repository.isCallCentersEnabled = isChecked
        }
        
        // Обработчик переключения drip mode
        binding.dripMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                DripModeDialog().show(parentFragmentManager, "DripModeDialog")
                binding.dripMode.isChecked = repository.isDripModeEnabled
            } else {
                repository.isDripModeEnabled = false
            }
        }

        // Кнопка управления сервисами
        binding.manageServicesButton.setOnClickListener {
            ServicesManagerDialog().show(parentFragmentManager, "ServicesManagerDialog")
        }

        return binding.root
    }

    private fun setupSpeedSelection(binding: DialogRepositoriesBinding, repository: MainRepository) {
        val initialSpeed = repository.attackSpeed

        // Устанавливаем начальное состояние кнопок
        when (initialSpeed) {
            AttackSpeed.FAST -> binding.btnFast.isSelected = true
            AttackSpeed.DEFAULT -> binding.btnDefault.isSelected = true
            AttackSpeed.SLOW -> binding.btnSlow.isSelected = true
        }

        binding.btnSlow.setOnClickListener {
            binding.btnSlow.isSelected = true
            binding.btnDefault.isSelected = false
            binding.btnFast.isSelected = false
            repository.attackSpeed = AttackSpeed.SLOW
        }
        binding.btnDefault.setOnClickListener {
            binding.btnSlow.isSelected = false
            binding.btnDefault.isSelected = true
            binding.btnFast.isSelected = false
            repository.attackSpeed = AttackSpeed.DEFAULT
        }
        binding.btnFast.setOnClickListener {
            binding.btnSlow.isSelected = false
            binding.btnDefault.isSelected = false
            binding.btnFast.isSelected = true
            repository.attackSpeed = AttackSpeed.FAST
        }
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
            is com.google.android.material.button.MaterialButton -> view.typeface = typeface
            is com.google.android.material.materialswitch.MaterialSwitch -> view.typeface = typeface
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
