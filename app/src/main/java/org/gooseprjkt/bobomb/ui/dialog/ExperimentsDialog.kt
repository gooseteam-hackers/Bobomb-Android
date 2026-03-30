package org.gooseprjkt.bobomb.ui.dialog

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.gooseprjkt.bobomb.databinding.DialogExperimentsBinding
import java.io.File

class ExperimentsDialog : BottomSheetDialogFragment() {

    private var _binding: DialogExperimentsBinding? = null
    private val binding get() = _binding!!

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importDslFile(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExperimentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyMonospaceFontIfNeeded(view)

        val prefs = requireContext().getSharedPreferences("bobomb_experiments", Context.MODE_PRIVATE)

        binding.debugModeSwitch.isChecked = prefs.getBoolean("debug_mode", false)
        binding.uiDebugSwitch.isChecked = prefs.getBoolean("ui_debug", false)
        binding.bomberServiceDebugSwitch.isChecked = prefs.getBoolean("bomber_service_debug", false)
        binding.dripModeDebugSwitch.isChecked = prefs.getBoolean("drip_mode_debug", false)
        binding.dripModeSwitch.isChecked = prefs.getBoolean("drip_mode_enabled", false)
        binding.monospaceFontSwitch.isChecked = prefs.getBoolean("monospace_font", false)
        binding.verboseLoggingSwitch.isChecked = prefs.getBoolean("verbose_logging", false)
        binding.networkDebugSwitch.isChecked = prefs.getBoolean("network_debug", false)
        binding.serviceTimeoutSwitch.isChecked = prefs.getBoolean("service_timeout", false)
        binding.randomUaSwitch.isChecked = prefs.getBoolean("random_ua", true)
        binding.retrySwitch.isChecked = prefs.getBoolean("retry_failed", true)
        binding.sslSkipSwitch.isChecked = prefs.getBoolean("ssl_skip", false)
        
        // Новые эксперименты
        binding.animationsSwitch.isChecked = prefs.getBoolean("animations_enabled", true)
        binding.hapticFeedbackSwitch.isChecked = prefs.getBoolean("haptic_feedback_enabled", true)
        binding.compactModeSwitch.isChecked = prefs.getBoolean("compact_mode_enabled", false)
        binding.forceHttpSwitch.isChecked = prefs.getBoolean("force_http2_enabled", false)
        binding.dnsSwitch.isChecked = prefs.getBoolean("private_dns_enabled", false)
        binding.ipv6Switch.isChecked = prefs.getBoolean("ipv6_preferred", false)
        binding.batteryOptimizationSwitch.isChecked = prefs.getBoolean("battery_optimization_ignored", true)
        binding.parallelRequestsSwitch.isChecked = prefs.getBoolean("parallel_requests_enabled", true)

        binding.debugModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("debug_mode", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Режим отладки включен", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.uiDebugSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("ui_debug", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Отладка UI включена", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.bomberServiceDebugSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("bomber_service_debug", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Отладка Bomber Service включена", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.dripModeDebugSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("drip_mode_debug", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Отладка капельного режима включена", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.dripModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("drip_mode_enabled", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Капельный режим активирован! 🚿", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Капельный режим деактивирован", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.monospaceFontSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("monospace_font", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Моноширинный шрифт включён", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(view, "Моноширинный шрифт выключен", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.disableSpamSwitch.isChecked = prefs.getBoolean("disable_spam", false)
        binding.disableSpamSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("disable_spam", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Режим 'Отключить спам' включен", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Режим 'Отключить спам' выключен", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.importDslButton.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.manageServicesButton.setOnClickListener {
            showServicesManager(view)
        }

        binding.dripDelayButton.setOnClickListener {
            showDripDelayDialog(view)
        }

        // Кнопка "О приложении"
        binding.aboutButton.setOnClickListener {
            AboutDialog().show(parentFragmentManager, "AboutDialog")
        }

        // Кнопка кастомизации темы
        binding.themeCustomizerButton.setOnClickListener {
            ThemeCustomizerDialog().show(parentFragmentManager, "ThemeCustomizerDialog")
        }

        // Новые эксперименты - обработчики
        binding.animationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("animations_enabled", isChecked).apply()
            Snackbar.make(view, if (isChecked) "Анимации включены" else "Анимации выключены", Snackbar.LENGTH_SHORT).show()
        }

        binding.hapticFeedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("haptic_feedback_enabled", isChecked).apply()
            Snackbar.make(view, if (isChecked) "Виброотклик включён" else "Виброотклик выключен", Snackbar.LENGTH_SHORT).show()
        }

        binding.compactModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("compact_mode_enabled", isChecked).apply()
            Snackbar.make(view, if (isChecked) "Компактный режим включён" else "Компактный режим выключен", Snackbar.LENGTH_SHORT).show()
        }

        // Сеть
        binding.forceHttpSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("force_http2_enabled", isChecked).apply()
            Snackbar.make(view, if (isChecked) "HTTP/2 включён" else "HTTP/2 выключен", Snackbar.LENGTH_SHORT).show()
        }

        binding.dnsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("private_dns_enabled", isChecked).apply()
            Snackbar.make(view, if (isChecked) "Частный DNS включён" else "Частный DNS выключен", Snackbar.LENGTH_SHORT).show()
        }

        binding.ipv6Switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("ipv6_preferred", isChecked).apply()
            Snackbar.make(view, if (isChecked) "IPv6 включён" else "IPv6 выключен", Snackbar.LENGTH_SHORT).show()
        }

        // Производительность
        binding.batteryOptimizationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("battery_optimization_ignored", isChecked).apply()
            Snackbar.make(view, if (isChecked) "Оптимизация батареи игнорируется" else "Оптимизация батареи включена", Snackbar.LENGTH_SHORT).show()
        }

        binding.parallelRequestsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("parallel_requests_enabled", isChecked).apply()
            Snackbar.make(view, if (isChecked) "Параллельные запросы включены" else "Параллельные запросы выключены", Snackbar.LENGTH_SHORT).show()
        }

        val currentDripDelay = prefs.getLong("drip_delay_ms", 1200000L)
        binding.dripDelayButton.text = "Настройка задержки (капельный режим: ${currentDripDelay / 60000}мин)"

        binding.valuesButton.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("bobomb_experiments", Context.MODE_PRIVATE)
            val mainPrefs = requireContext().getSharedPreferences("org.gooseprjkt.bobomb_preferences", Context.MODE_PRIVATE)

            val attackSpeed = mainPrefs.getInt("attack_speed", 1)
            val dripDelay = prefs.getLong("drip_delay_ms", 5000L)
            val serviceTimeout = if (prefs.getBoolean("service_timeout", false)) "15000ms" else "10000ms"

            val message = """
                Настройки приложения:

                • Скорость атаки: ${when (attackSpeed) { 0 -> "Медленно"; 2 -> "Быстро"; else -> "Обычно" }}
                • Задержка капельного режима: ${dripDelay}ms
                • Таймаут сервиса: $serviceTimeout
                • Повтор запросов: ${if (prefs.getBoolean("retry_failed", true)) "Включен" else "Выключен"}
                • Случайный User-Agent: ${if (prefs.getBoolean("random_ua", true)) "Включен" else "Выключен"}
            """.trimIndent()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Значения")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Сбросить") { _, _ ->
                    prefs.edit()
                        .putLong("drip_delay_ms", 5000L)
                        .apply()
                    Snackbar.make(view, "Настройки сброшены", Snackbar.LENGTH_SHORT).show()
                }
                .show()
        }

        binding.inspectorButton.setOnClickListener {
            try {
                val process = Runtime.getRuntime().exec("logcat -d -t 100")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                val logs = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    logs.append(line).append("\n")
                }

                val logText = if (logs.isNotEmpty()) logs.toString() else "Нет доступных логов"

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Инспектор (Логи)")
                    .setMessage(logText)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Копировать") { _, _ ->
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Bobomb Logs", logText)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, "Логи скопированы", Snackbar.LENGTH_SHORT).show()
                    }
                    .show()
            } catch (e: Exception) {
                Snackbar.make(view, "Не удалось получить логи: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.verboseLoggingSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("verbose_logging", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Подробное логирование включено", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.networkDebugSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("network_debug", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Отладка сети включена", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.serviceTimeoutSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("service_timeout", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Увеличенный таймаут установлен", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.randomUaSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("random_ua", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Случайный User-Agent включен", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.retrySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("retry_failed", isChecked).apply()
            if (isChecked) {
                Snackbar.make(view, "Повтор неудачных запросов включен", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.sslSkipSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("ssl_skip", isChecked).apply()
            if (isChecked) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Предупреждение!")
                    .setMessage("Пропуск SSL проверки опасен! Используйте только в отладочных целях.")
                    .setPositiveButton("Понятно", null)
                    .show()
            }
        }

        binding.resetSettingsButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Сброс настроек")
                .setMessage("Вы уверены, что хотите сбросить все настройки экспериментов?")
                .setPositiveButton("Сбросить") { _, _ ->
                    binding.debugModeSwitch.isChecked = false
                    binding.uiDebugSwitch.isChecked = false
                    binding.bomberServiceDebugSwitch.isChecked = false
                    binding.dripModeDebugSwitch.isChecked = false
                    binding.dripModeSwitch.isChecked = false
                    binding.disableSpamSwitch.isChecked = false
                    binding.verboseLoggingSwitch.isChecked = false
                    binding.networkDebugSwitch.isChecked = false
                    binding.serviceTimeoutSwitch.isChecked = false
                    binding.randomUaSwitch.isChecked = true
                    binding.retrySwitch.isChecked = true
                    binding.sslSkipSwitch.isChecked = false

                    prefs.edit().clear().apply()
                    Snackbar.make(view, "Все настройки сброшены", Snackbar.LENGTH_LONG).show()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun importDslFile(uri: Uri) {
        try {
            val context = requireContext()
            val inputStream = context.contentResolver.openInputStream(uri)
            val bslContent = inputStream?.readBytes()?.decodeToString()
            inputStream?.close()

            if (bslContent.isNullOrEmpty()) {
                Snackbar.make(binding.root, "Файл пустой", Snackbar.LENGTH_SHORT).show()
                return
            }

            val mainBslFile = File(context.filesDir, "main.bsl")
            val markerStart = "### BEGIN_IMPORT: ${System.currentTimeMillis()} ###\n"
            val markerEnd = "### END_IMPORT ###\n"

            val existingContent = if (mainBslFile.exists()) {
                mainBslFile.readText()
            } else ""

            val newContent = existingContent + markerStart + bslContent + markerEnd

            mainBslFile.writeText(newContent)

            Snackbar.make(binding.root, "BSL файл импортирован в main.bsl", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Ошибка импорта: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showServicesManager(view: View) {
        val context = requireContext()
        val mainBslFile = File(context.filesDir, "main.bsl")

        if (!mainBslFile.exists() || mainBslFile.length() == 0L) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Менеджер сервисов")
                .setMessage("Файл main.bsl пуст или не найден.\n\nИмпортируйте .bsl файл или используйте встроенные сервисы из assets/services.bsl")
                .setPositiveButton("OK", null)
                .setNeutralButton("Импортировать") { _, _ ->
                    filePickerLauncher.launch("*/*")
                }
                .show()
            return
        }

        val content = mainBslFile.readText()
        val imports = mutableListOf<String>()
        val lines = content.lines()

        var inImport = false
        var currentImport = StringBuilder()

        for (line in lines) {
            if (line.startsWith("### BEGIN_IMPORT:")) {
                inImport = true
                currentImport = StringBuilder()
            } else if (line.startsWith("### END_IMPORT ###")) {
                inImport = false
                if (currentImport.isNotEmpty()) {
                    imports.add(currentImport.toString().trim())
                }
            } else if (inImport) {
                currentImport.append(line).append("\n")
            }
        }

        if (imports.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Менеджер сервисов")
                .setMessage("Нет импортированных сервисов.\n\nИспользуйте встроенные сервисы из assets/services.bsl")
                .setPositiveButton("OK", null)
                .setNeutralButton("Импортировать") { _, _ ->
                    filePickerLauncher.launch("*/*")
                }
                .show()
            return
        }

        val items = imports.mapIndexed { index, _ -> "Импорт #$index" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Менеджер сервисов")
            .setItems(items) { _, which ->
                removeImport(view, which)
            }
            .setPositiveButton("OK", null)
            .setNeutralButton("Удалить всё") { _, _ ->
                mainBslFile.writeText("")
                Snackbar.make(view, "Все импорты удалены", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun removeImport(view: View, importIndex: Int) {
        try {
            val context = requireContext()
            val mainBslFile = File(context.filesDir, "main.bsl")
            val content = mainBslFile.readText()

            val lines = content.lines().toMutableList()
            var inImport = false
            var currentImportIndex = -1
            val linesToRemove = mutableListOf<Int>()

            for ((i, line) in lines.withIndex()) {
                if (line.startsWith("### BEGIN_IMPORT:")) {
                    inImport = true
                    currentImportIndex++
                    if (currentImportIndex == importIndex) {
                        linesToRemove.add(i)
                    }
                } else if (line.startsWith("### END_IMPORT ###")) {
                    if (currentImportIndex == importIndex) {
                        linesToRemove.add(i)
                    }
                    inImport = false
                } else if (inImport && currentImportIndex == importIndex) {
                    linesToRemove.add(i)
                }
            }

            for (i in linesToRemove.sortedDescending()) {
                lines.removeAt(i)
            }

            mainBslFile.writeText(lines.joinToString("\n"))
            Snackbar.make(view, "Импорт #$importIndex удален", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Snackbar.make(view, "Ошибка удаления: ${e.message}", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showDripDelayDialog(view: View) {
        val prefs = requireContext().getSharedPreferences("bobomb_experiments", Context.MODE_PRIVATE)
        val currentDelay = prefs.getLong("drip_delay_ms", 1200000L)

        val editText = EditText(requireContext()).apply {
            setText((currentDelay / 60000).toString())
            hint = "Задержка в минутах"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Задержка капельного режима")
            .setMessage("Введите задержку в минутах (текущая: ${currentDelay / 60000}мин):")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                try {
                    val minutes = editText.text.toString().toLong()
                    val delayMs = minutes * 60000
                    prefs.edit().putLong("drip_delay_ms", delayMs).apply()
                    binding.dripDelayButton.text = "Настройка задержки (капельный режим: ${minutes}мин)"
                    Snackbar.make(view, "Задержка установлена: ${minutes}мин", Snackbar.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Snackbar.make(view, "Неверное значение", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
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
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
