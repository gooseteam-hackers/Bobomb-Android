package org.gooseprjkt.bobomb.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.gooseprjkt.bobomb.databinding.DialogThemeCustomizerBinding
import org.gooseprjkt.bobomb.ui.utils.ThemeHelper

class ThemeCustomizerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogThemeCustomizerBinding? = null
    private val binding get() = _binding!!

    private var selectedTheme = "monet"
    private var customColor = 0xFF6750A4

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogThemeCustomizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        selectedTheme = prefs.getString("selected_theme", "monet") ?: "monet"

        updateRadioButtons()

        binding.monetPresetCard.setOnClickListener { selectTheme("monet") }
        binding.monetRadio.setOnClickListener { selectTheme("monet") }
        binding.matrixPresetCard.setOnClickListener { selectTheme("matrix") }
        binding.matrixRadio.setOnClickListener { selectTheme("matrix") }

        binding.cancelButton.setOnClickListener { dismiss() }
        binding.applyButton.setOnClickListener { applyAndSave() }
    }

    private fun selectTheme(theme: String) {
        selectedTheme = theme
        updateRadioButtons()
    }

    private fun updateRadioButtons() {
        binding.monetRadio.isChecked = selectedTheme == "monet"
        binding.matrixRadio.isChecked = selectedTheme == "matrix"
    }

    private fun applyAndSave() {
        val prefs = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("selected_theme", selectedTheme)
        }
        
        ThemeHelper.applyTheme(requireContext(), selectedTheme, customColor)
        
        Toast.makeText(requireContext(), "Тема применена! Перезапустите приложение", Toast.LENGTH_LONG).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
