package org.goosepjkt.bobomb.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.goosepjkt.bobomb.R
import org.goosepjkt.bobomb.databinding.DialogProxiesBinding
import org.goosepjkt.bobomb.ui.MainRepository
import org.goosepjkt.bobomb.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProxiesDialog : DialogFragment() {

    private var _binding: DialogProxiesBinding? = null


    private val binding get() = _binding!!

    private val repository: MainRepository by lazy { MainRepository(requireContext()) }

    private val model: MainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogProxiesBinding.inflate(getLayoutInflater())
        _binding!!.proxies.setText(repository.rawProxy)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.proxy)
            .setView(binding.getRoot())
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.getRoot()
    }

    override fun onStart() {
        super.onStart()

        (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            try {
                val proxyText = binding.proxies.getText().toString().trim()
                
                // Validate proxy format
                if (proxyText.isNotEmpty()) {
                    val proxies = repository.parseProxy(proxyText)
                    if (proxies.isEmpty()) {
                        Toast.makeText(requireContext(), "Неверный формат прокси! Используйте format: host:port или host:port login:password", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    Toast.makeText(requireContext(), "Найдено прокси: ${proxies.size}", Toast.LENGTH_SHORT).show()
                }

                repository.rawProxy = proxyText
                model.setProxyEnabled(proxyText.isNotEmpty())

                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
