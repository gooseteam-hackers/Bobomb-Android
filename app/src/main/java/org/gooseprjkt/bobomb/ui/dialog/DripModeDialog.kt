package org.gooseprjkt.bobomb.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.gooseprjkt.bobomb.BuildVars
import org.gooseprjkt.bobomb.BuildVars.DripModeType
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.databinding.DialogDripModeBinding
import org.gooseprjkt.bobomb.ui.MainRepository
import org.gooseprjkt.bobomb.ui.MainViewModel

class DripModeDialog : BottomSheetDialogFragment() {
    
    private var _binding: DialogDripModeBinding? = null
    private val binding get() = _binding!!
    
    private val repository by lazy { MainRepository(requireContext()) }
    private val model by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDripModeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyMonospaceFontIfNeeded(view)
        
        // Устанавливаем текущие значения
        binding.delayInput.setText((repository.dripDelayMs / 60000).toString())
        binding.randomDelaySwitch.isChecked = repository.isDripRandomDelayEnabled
        binding.randomMinInput.setText((repository.dripRandomDelayMinMs / 60000).toString())
        binding.randomMaxInput.setText((repository.dripRandomDelayMaxMs / 60000).toString())
        binding.persistentSwitch.isChecked = repository.dripModeType == BuildVars.DripModeType.PERSISTENT
        
        // Показываем/скрываем поля случайной задержки
        updateRandomDelayVisibility()
        
        // Обработчик переключения случайной задержки
        binding.randomDelaySwitch.setOnCheckedChangeListener { _, isChecked ->
            updateRandomDelayVisibility()
        }
        
        // Кнопка отмены
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        
        // Кнопка включения
        binding.enableButton.setOnClickListener {
            if (validateAndSave()) {
                // Включаем drip mode
                repository.isDripModeEnabled = true
                
                // Отправляем результат родительскому фрагменту
                val result = Bundle().apply {
                    putBoolean("drip_mode_enabled", true)
                }
                parentFragmentManager.setFragmentResult("drip_mode_request", result)
                
                model.collectAll()
                Toast.makeText(requireContext(), "Капельный режим включён 💧", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
    
    private fun updateRandomDelayVisibility() {
        val isVisible = binding.randomDelaySwitch.isChecked
        binding.randomDelayContainer.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
    
    private fun validateAndSave(): Boolean {
        // Валидация основной задержки
        val delayStr = binding.delayInput.text.toString()
        if (delayStr.isEmpty()) {
            binding.delayInputLayout.error = "Введите задержку"
            return false
        }
        
        var delayMinutes = delayStr.toLongOrNull()
        if (delayMinutes == null) {
            binding.delayInputLayout.error = "Неверный формат"
            return false
        }
        
        if (delayMinutes < 1) {
            binding.delayInputLayout.error = "Минимум 1 минута"
            return false
        }
        
        if (delayMinutes > 60) {
            binding.delayInputLayout.error = "Максимум 60 минут"
            return false
        }
        
        binding.delayInputLayout.error = null
        
        // Сохраняем задержку
        repository.dripDelayMs = delayMinutes * 60 * 1000L
        
        // Валидация случайной задержки
        if (binding.randomDelaySwitch.isChecked) {
            val minStr = binding.randomMinInput.text.toString()
            val maxStr = binding.randomMaxInput.text.toString()
            
            if (minStr.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните поля X и Y для случайной задержки", Toast.LENGTH_SHORT).show()
                return false
            }
            
            var minMinutes = minStr.toLongOrNull()
            var maxMinutes = maxStr.toLongOrNull()
            
            if (minMinutes == null || maxMinutes == null) {
                Toast.makeText(requireContext(), "Неверный формат случайной задержки", Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (minMinutes < 1 || maxMinutes < 1) {
                Toast.makeText(requireContext(), "Минимум 1 минута", Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (minMinutes > 60 || maxMinutes > 60) {
                Toast.makeText(requireContext(), "Максимум 60 минут", Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (minMinutes >= maxMinutes) {
                Toast.makeText(requireContext(), "X должен быть меньше Y", Toast.LENGTH_SHORT).show()
                return false
            }
            
            repository.isDripRandomDelayEnabled = true
            repository.dripRandomDelayMinMs = minMinutes * 60 * 1000L
            repository.dripRandomDelayMaxMs = maxMinutes * 60 * 1000L
        } else {
            repository.isDripRandomDelayEnabled = false
        }
        
        // Сохраняем тип режима
        repository.dripModeType = if (binding.persistentSwitch.isChecked) {
            DripModeType.PERSISTENT
        } else {
            DripModeType.SINGLE_SESSION
        }
        
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyMonospaceFontIfNeeded(view: View) {
        val prefs = requireContext().getSharedPreferences("bobomb_experiments", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("monospace_font", false)) {
            applyMonospaceFont(view, android.graphics.Typeface.MONOSPACE)
        }
    }

    private fun applyMonospaceFont(view: View, typeface: android.graphics.Typeface) {
        when (view) {
            is android.widget.TextView -> view.typeface = typeface
            is android.widget.Button -> view.typeface = typeface
            is android.widget.EditText -> view.typeface = typeface
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
