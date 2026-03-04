package org.gooseprjkt.bobomb.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.databinding.DialogAdvertisingBinding
import org.gooseprjkt.bobomb.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class AdvertisingDialog : DialogFragment() {
    private var _binding: DialogAdvertisingBinding? = null


    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAdvertisingBinding.inflate(getLayoutInflater())
        _binding!!.advertisingDescription.movementMethod = LinkMovementMethod.getInstance()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.getRoot())
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.setCancelable(false)

        val model by activityViewModels<MainViewModel>()

        model.getAdvertisingCounter().observe(viewLifecycleOwner) { counter: Int ->
            binding.skipButton.text = getString(R.string.skip, counter)
            binding.skipButton.setEnabled(counter == 0)
        }


        binding.advertisingDescription.text = getString(R.string.advertising_placeholder_text)
        binding.advertisingButton.text = getString(R.string.advertising_placeholder_button)
        binding.advertisingButton.setOnClickListener {

            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
            )
        }


        binding.advertisingImage.setVisibility(View.GONE)

        model.startCounting()

        binding.skipButton.setOnClickListener {
            dialog!!.cancel()
            model.advertisingTrigger.setValue(true)
        }

        return binding.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val HTML_KEY = "html"
        private const val IMAGE_KEY = "image"
        private const val URL_KEY = "url"
    }
}