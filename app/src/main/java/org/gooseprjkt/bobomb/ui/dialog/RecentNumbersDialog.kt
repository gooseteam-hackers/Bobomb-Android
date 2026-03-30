package org.gooseprjkt.bobomb.ui.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.databinding.DialogRecentNumbersBinding
import org.gooseprjkt.bobomb.databinding.RecentNumberRowBinding
import org.gooseprjkt.bobomb.ui.MainRepository
import org.gooseprjkt.bobomb.ui.MainViewModel

class RecentNumbersDialog : BottomSheetDialogFragment() {

    private var _binding: DialogRecentNumbersBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { MainRepository(requireContext()) }
    private val model by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecentNumbersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRecentNumbers()

        // Кнопка очистки
        binding.clearRecentButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Очистить историю?")
                .setMessage("Все недавние номера будут удалены")
                .setPositiveButton("Очистить") { _, _ ->
                    repository.clearRecentNumbers()
                    loadRecentNumbers()
                    Toast.makeText(requireContext(), "История очищена", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        // Кнопка закрытия
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun loadRecentNumbers() {
        val recentNumbers = repository.getRecentNumbers()
        binding.recentNumbersContainer.removeAllViews()

        if (recentNumbers.isEmpty()) {
            val emptyText = android.widget.TextView(requireContext()).apply {
                text = "Нет недавних номеров"
                textSize = 14f
                setTextColor(requireContext().getColor(android.R.color.darker_gray))
                setPadding(16, 32, 16, 32)
                gravity = android.view.Gravity.CENTER
            }
            binding.recentNumbersContainer.addView(emptyText)
            binding.clearRecentButton.isEnabled = false
            return
        }

        binding.clearRecentButton.isEnabled = true

        // Добавляем карточки для каждого номера
        for ((index, number) in recentNumbers.withIndex()) {
            val rowBinding = RecentNumberRowBinding.inflate(layoutInflater)
            
            // Устанавливаем номер (убираем + для отображения)
            rowBinding.phoneNumber.text = number
            
            // Кнопка Play (вставить номер) - убираем + и код страны
            rowBinding.playButton.setOnClickListener {
                val phoneWithoutPlus = number.substring(1) // Убираем +
                // Находим код страны и убираем его
                val phoneCode = findPhoneCode(phoneWithoutPlus)
                val phoneNumber = if (phoneCode.isNotEmpty()) {
                    phoneWithoutPlus.substring(phoneCode.length)
                } else {
                    phoneWithoutPlus
                }
                (activity as? org.gooseprjkt.bobomb.ui.MainActivity)?.setPhoneNumber(phoneNumber)
                Toast.makeText(requireContext(), "Номер вставлен", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            
            // Кнопка Copy (копировать)
            rowBinding.copyButton.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("phone_number", number)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Скопировано", Toast.LENGTH_SHORT).show()
            }
            
            // Кнопка Delete (удалить)
            rowBinding.deleteButton.setOnClickListener {
                repository.removeRecentNumber(number)
                loadRecentNumbers() // Перезагружаем список
                Toast.makeText(requireContext(), "Номер удалён", Toast.LENGTH_SHORT).show()
            }
            
            binding.recentNumbersContainer.addView(rowBinding.root)
            
            // Добавляем разделитель кроме последнего элемента
            if (index < recentNumbers.size - 1) {
                val divider = android.view.View(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.divider_height)
                    )
                    setBackgroundColor(requireContext().getColor(android.R.color.darker_gray))
                    alpha = 0.1f
                }
                binding.recentNumbersContainer.addView(divider)
            }
        }
    }

    private fun findPhoneCode(phone: String): String {
        val codes = listOf("7", "380", "375", "77", "90", "91", "98")
        for (code in codes) {
            if (phone.startsWith(code)) {
                return code
            }
        }
        return ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
