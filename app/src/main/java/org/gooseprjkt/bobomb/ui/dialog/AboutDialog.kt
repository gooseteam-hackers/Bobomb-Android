package org.gooseprjkt.bobomb.ui.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import org.gooseprjkt.bobomb.BuildVars
import org.gooseprjkt.bobomb.databinding.DialogAboutBinding
import org.json.JSONObject

class AboutDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyMonospaceFontIfNeeded(view)
        applyMatrixHeart()
        checkUpdatesOnStart()

        // Кнопка GitHub
        binding.githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildVars.SOURCECODE_URL))
            startActivity(intent)
        }

        // Кнопка Telegram
        binding.telegramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildVars.TELEGRAM_URL))
            startActivity(intent)
        }

        // Кнопки обновлений (скрыты до следующей версии)
        // binding.checkAppUpdate.setOnClickListener { checkAppUpdate() }
        // binding.checkServicesUpdate.setOnClickListener { checkServicesUpdate() }

        // Кнопка закрытия
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun applyMatrixHeart() {
        val prefs = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val selectedTheme = prefs.getString("selected_theme", "monet") ?: "monet"
        
        if (selectedTheme == "matrix") {
            // Делаем сердце зелёным
            val text = "Разработано с ❤️ командой GooseTeam"
            val spannable = SpannableString(text)
            val heartIndex = text.indexOf('❤')
            if (heartIndex >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00FF00")),
                    heartIndex,
                    heartIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.teamText.text = spannable
        }
    }

    private fun checkUpdatesOnStart() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL(BuildVars.ANDROID_UPDATE_URL)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    return@launch
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                if (!json.has("updates")) {
                    return@launch
                }
                
                val updates = json.getJSONObject("updates")
                val versionCode = updates.optInt("versionCode", 0)
                
                if (versionCode > BuildVars.VERSION_CODE) {
                    val description = updates.optJSONObject("description")
                    val descText = if (description != null) {
                        val keys = description.keys()
                        if (keys.hasNext()) description.getString(keys.next()) else "Доступно обновление"
                    } else "Доступно обновление"
                    
                    val directUrl = updates.optString("directUrl", "")
                    
                    Dispatchers.Main.run {
                        showUpdateNotification("Приложение", descText, directUrl)
                    }
                }
            } catch (e: Exception) {
                // Silent fail for background check
            }
        }
    }

    private fun checkAppUpdate() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL(BuildVars.ANDROID_UPDATE_URL)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    Dispatchers.Main.run {
                        android.widget.Toast.makeText(requireContext(), "Ошибка сети: код $responseCode", android.widget.Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                if (!json.has("updates")) {
                    Dispatchers.Main.run {
                        android.widget.Toast.makeText(requireContext(), "Неверный формат JSON", android.widget.Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                
                val updates = json.getJSONObject("updates")
                val versionCode = updates.optInt("versionCode", 0)
                val description = updates.optJSONObject("description")
                val directUrl = updates.optString("directUrl", "")
                
                val descText = if (description != null) {
                    val keys = description.keys()
                    if (keys.hasNext()) description.getString(keys.next()) else "Новая версия доступна"
                } else "Новая версия доступна"
                
                Dispatchers.Main.run {
                    showUpdateNotification("Приложение", descText, directUrl)
                }
            } catch (e: Exception) {
                Dispatchers.Main.run {
                    android.widget.Toast.makeText(requireContext(), "Ошибка: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkServicesUpdate() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL(BuildVars.SERVICES_UPDATE_URL)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    Dispatchers.Main.run {
                        android.widget.Toast.makeText(requireContext(), "Ошибка сети: код $responseCode", android.widget.Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                if (!json.has("updates")) {
                    Dispatchers.Main.run {
                        android.widget.Toast.makeText(requireContext(), "Неверный формат JSON", android.widget.Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                
                val updates = json.getJSONObject("updates")
                val description = updates.optJSONObject("description")
                val directUrl = updates.optString("directUrl", "")
                
                val descText = if (description != null) {
                    val keys = description.keys()
                    if (keys.hasNext()) description.getString(keys.next()) else "Обновление сервисов доступно"
                } else "Обновление сервисов доступно"
                
                Dispatchers.Main.run {
                    showUpdateNotification("Сервисы", descText, directUrl)
                }
            } catch (e: Exception) {
                Dispatchers.Main.run {
                    android.widget.Toast.makeText(requireContext(), "Ошибка: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showUpdateNotification(title: String, description: String, downloadUrl: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📦 Доступно обновление: $title")
            .setMessage(description)
            .setPositiveButton("Скачать") { _, _ ->
                if (downloadUrl.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    startActivity(intent)
                }
            }
            .setNegativeButton("Позже", null)
            .show()
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
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
