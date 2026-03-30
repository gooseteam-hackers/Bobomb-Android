package org.gooseprjkt.bobomb.ui.dialog

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import org.gooseprjkt.bobomb.databinding.DialogServicesManagerBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class ServicesManagerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogServicesManagerBinding? = null
    private val binding get() = _binding!!

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importBslFile(it) }
    }

    private val textFilePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importEndpoints(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogServicesManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyMonospaceFontIfNeeded(view)

        val prefs = requireContext().getSharedPreferences("bobomb_services", Context.MODE_PRIVATE)
        val savedEndpoints = prefs.getString("endpoints_list", "") ?: ""
        binding.endpointsInput.setText(savedEndpoints)

        // Импорт .bsl файла
        binding.importBslButton.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        // Импорт текстового файла с эндпоинтами
        binding.importEndpointsButton.setOnClickListener {
            textFilePickerLauncher.launch("text/*")
        }

        // Отмена
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // Сохранение
        binding.saveButton.setOnClickListener {
            val endpoints = binding.endpointsInput.text.toString()
            prefs.edit().putString("endpoints_list", endpoints).apply()
            Toast.makeText(requireContext(), "Эндпоинты сохранены", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun importBslFile(uri: Uri) {
        try {
            val reader = BufferedReader(InputStreamReader(requireContext().contentResolver.openInputStream(uri)))
            val content = reader.readText()
            reader.close()

            // Показываем содержимое файла
            Snackbar.make(binding.root, "BSL файл импортирован: ${content.length} символов", Snackbar.LENGTH_LONG).show()

            // Здесь можно добавить парсинг BSL файла
            // Для простоты просто показываем уведомление
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Ошибка импорта BSL: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun importEndpoints(uri: Uri) {
        try {
            val reader = BufferedReader(InputStreamReader(requireContext().contentResolver.openInputStream(uri)))
            val content = reader.readText()
            reader.close()

            // Добавляем к текущим эндпоинтам
            val currentText = binding.endpointsInput.text.toString()
            val newText = if (currentText.isNotEmpty()) {
                "$currentText;${content.trim()}"
            } else {
                content.trim()
            }

            binding.endpointsInput.setText(newText)
            Snackbar.make(binding.root, "Эндпоинты импортированы", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Ошибка импорта: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            is android.widget.EditText -> view.typeface = typeface
            is com.google.android.material.textfield.TextInputEditText -> view.typeface = typeface
            is com.google.android.material.button.MaterialButton -> view.typeface = typeface
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
