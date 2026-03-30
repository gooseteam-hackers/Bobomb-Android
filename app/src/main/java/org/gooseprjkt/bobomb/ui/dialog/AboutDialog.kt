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

        // Кнопка проверки обновления приложения
        binding.checkAppUpdate.setOnClickListener {
            checkAppUpdate()
        }

        // Кнопка обновления сервисов
        binding.checkServicesUpdate.setOnClickListener {
            checkServicesUpdate()
        }

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
                val response = java.net.URL(BuildVars.ANDROID_UPDATE_URL).openConnection()
                    .apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    .inputStream.bufferedReader().use { it.readText() }
                
                val json = JSONObject(response)
                val updates = json.getJSONObject("updates")
                val versionCode = updates.getInt("versionCode")
                
                if (versionCode > BuildVars.VERSION_CODE) {
                    val description = updates.getJSONObject("description")
                    val descText = description.getString("Bobomb ${versionCode}")
                    Dispatchers.Main.run {
                        showUpdateNotification("Приложение", descText, updates.optString("directUrl", ""))
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun checkAppUpdate() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = java.net.URL(BuildVars.ANDROID_UPDATE_URL).openConnection()
                    .apply { connectTimeout = 5000 }
                    .inputStream.bufferedReader().use { it.readText() }
                
                val json = JSONObject(response)
                val updates = json.getJSONObject("updates")
                val versionCode = updates.getInt("versionCode")
                val description = updates.getJSONObject("description")
                val descText = description.getString("Bobomb ${versionCode}")
                val directUrl = updates.optString("directUrl", "")
                
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
                val response = java.net.URL(BuildVars.SERVICES_UPDATE_URL).openConnection()
                    .apply { connectTimeout = 5000 }
                    .inputStream.bufferedReader().use { it.readText() }
                
                val json = JSONObject(response)
                val updates = json.getJSONObject("updates")
                val description = updates.getJSONObject("description")
                val descText = description.getString("Bobomb Services")
                val directUrl = updates.optString("directUrl", "")
                
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
