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
import org.gooseprjkt.bobomb.databinding.DialogProxyManagerBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

class ProxyManagerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogProxyManagerBinding? = null
    private val binding get() = _binding!!

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importProxyFile(it) }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProxyManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyMonospaceFontIfNeeded(view)

        val prefs = requireContext().getSharedPreferences("org.gooseprjkt.bobomb_preferences", Context.MODE_PRIVATE)
        val savedProxy = prefs.getString("proxy", "") ?: ""
        binding.proxyInput.setText(savedProxy)

        // Импорт файла с прокси
        binding.importProxyButton.setOnClickListener {
            filePickerLauncher.launch("text/*")
        }

        // Тестирование прокси
        binding.testProxyButton.setOnClickListener {
            testProxies()
        }

        // Отбор самых быстрых
        binding.keepFastestButton.setOnClickListener {
            keepFastestProxies()
        }

        // Отмена
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // Сохранение
        binding.saveButton.setOnClickListener {
            val proxyList = binding.proxyInput.text.toString()
            prefs.edit().putString("proxy", proxyList).apply()
            Toast.makeText(requireContext(), "Прокси сохранены", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun importProxyFile(uri: Uri) {
        try {
            val reader = BufferedReader(InputStreamReader(requireContext().contentResolver.openInputStream(uri)))
            val content = reader.readText()
            reader.close()

            val currentText = binding.proxyInput.text.toString()
            val newText = if (currentText.isNotEmpty()) {
                "$currentText;${content.trim()}"
            } else {
                content.trim()
            }

            binding.proxyInput.setText(newText)
            Snackbar.make(binding.root, "Прокси импортированы", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Ошибка импорта: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun testProxies() {
        val proxyText = binding.proxyInput.text.toString()
        if (proxyText.isEmpty()) {
            Snackbar.make(binding.root, "Список прокси пуст", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.testResults.visibility = View.VISIBLE
        binding.testResultsText.visibility = View.VISIBLE
        binding.testResults.text = "Тестирование..."

        scope.launch {
            val proxies = proxyText.split(";").filter { it.isNotBlank() }
            val results = mutableListOf<String>()

            for (proxy in proxies) {
                val parts = proxy.trim().split(" ")
                val proxyAddr = parts[0].split(":")
                
                if (proxyAddr.size < 2) {
                    results.add("❌ $proxy - неверный формат")
                    continue
                }

                try {
                    val host = proxyAddr[0]
                    val port = proxyAddr[1].toInt()
                    
                    val startTime = System.currentTimeMillis()
                    val isReachable = testProxyConnection(host, port)
                    val ping = System.currentTimeMillis() - startTime

                    if (isReachable) {
                        results.add("✅ $proxy - ${ping}ms")
                    } else {
                        results.add("❌ $proxy - недоступен")
                    }
                } catch (e: Exception) {
                    results.add("❌ $proxy - ${e.message}")
                }
            }

            binding.testResults.text = results.joinToString("\n")
        }
    }

    private suspend fun testProxyConnection(host: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
                val connection = URL("https://www.google.com").openConnection(proxy)
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                connection.getInputStream().close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun keepFastestProxies() {
        val proxyText = binding.proxyInput.text.toString()
        if (proxyText.isEmpty()) {
            Snackbar.make(binding.root, "Список прокси пуст", Snackbar.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val proxies = proxyText.split(";").filter { it.isNotBlank() }
            val workingProxies = mutableListOf<Pair<String, Long>>()

            for (proxy in proxies) {
                val parts = proxy.trim().split(" ")
                val proxyAddr = parts[0].split(":")

                if (proxyAddr.size < 2) continue

                try {
                    val host = proxyAddr[0]
                    val port = proxyAddr[1].toInt()

                    val startTime = System.currentTimeMillis()
                    val isReachable = testProxyConnection(host, port)

                    if (isReachable) {
                        val ping = System.currentTimeMillis() - startTime
                        workingProxies.add(proxy to ping)
                    }
                } catch (e: Exception) {
                    // Skip unreachable proxies
                }
            }

            // Сортируем по пингу и оставляем топ-10
            val sorted = workingProxies.sortedBy { it.second }.take(10)
            val result = sorted.joinToString(";") { it.first }

            binding.proxyInput.setText(result)
            Snackbar.make(binding.root, "Оставлено ${sorted.size} самых быстрых прокси", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
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
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
