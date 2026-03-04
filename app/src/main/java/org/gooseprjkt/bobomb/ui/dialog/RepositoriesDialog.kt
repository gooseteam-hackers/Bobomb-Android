package org.gooseprjkt.bobomb.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.gooseprjkt.bobomb.BuildVars.AttackSpeed
import org.gooseprjkt.bobomb.R
import org.gooseprjkt.bobomb.databinding.DialogRepositoriesBinding
import org.gooseprjkt.bobomb.databinding.TextInputRowBinding
import org.gooseprjkt.bobomb.ui.MainRepository
import org.gooseprjkt.bobomb.ui.MainViewModel

class RepositoriesDialog : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogRepositoriesBinding.inflate(inflater)
        val repository = MainRepository(requireContext())

        val model by activityViewModels<MainViewModel>()

        (dialog as BottomSheetDialog).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED)

        binding.remoteServices.setChecked(repository.isRemoteServicesEnabled)
        binding.callCenters.setChecked(repository.isCallCentersEnabled)

        setupSpeedSelection(binding, repository)

        binding.callCenters.setOnCheckedChangeListener { _: CompoundButton?, enabled: Boolean ->
            repository.isCallCentersEnabled = enabled
        }

        binding.remoteServices.setOnCheckedChangeListener { _: CompoundButton?, enabled: Boolean ->
            val textInputRowBinding = TextInputRowBinding.inflate(getLayoutInflater())

            val builder = StringBuilder()

            for (url in repository.remoteServicesUrls!!) {
                builder.append(url)
                builder.append(";")
            }

            if (builder.isNotEmpty())
                builder.deleteCharAt(builder.length - 1)

            textInputRowBinding.textInput.editText?.setText(builder.toString())

            if (enabled)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.enter_source)
                    .setView(textInputRowBinding.getRoot())
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        repository.remoteServicesUrls = HashSet(textInputRowBinding.textInput.editText!!.getText().toString().split(";"))
                    }
                    .show()

            repository.isRemoteServicesEnabled = enabled
        }

        binding.apply.setOnClickListener {
            model.collectAll()
            dismiss()
        }

        return binding.getRoot()
    }

    private fun setupSpeedSelection(binding: DialogRepositoriesBinding, repository: MainRepository) {
        val initialSpeed = repository.attackSpeed
        val checkedId = when (initialSpeed) {
            AttackSpeed.FAST -> R.id.btnFast
            AttackSpeed.DEFAULT -> R.id.btnDefault
            AttackSpeed.SLOW -> R.id.btnSlow
        }

        binding.speedToggleGroup.check(checkedId)

        binding.speedToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val newSpeed = when (checkedId) {
                R.id.btnSlow -> AttackSpeed.SLOW
                R.id.btnDefault -> AttackSpeed.DEFAULT
                R.id.btnFast -> AttackSpeed.FAST
                else -> AttackSpeed.DEFAULT
            }
            repository.attackSpeed = newSpeed
        }
    }
}
