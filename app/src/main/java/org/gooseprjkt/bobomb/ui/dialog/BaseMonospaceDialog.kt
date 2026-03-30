package org.gooseprjkt.bobomb.ui.dialog

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

abstract class BaseMonospaceDialog : DialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyMonospaceFontIfNeeded(view)
    }

    private fun applyMonospaceFontIfNeeded(view: View) {
        val prefs = requireContext().getSharedPreferences("bobomb_experiments", Context.MODE_PRIVATE)
        if (prefs.getBoolean("monospace_font", false)) {
            applyMonospaceFont(view, Typeface.MONOSPACE)
        }
    }

    private fun applyMonospaceFont(view: View, typeface: Typeface) {
        when (view) {
            is TextView -> view.typeface = typeface
            is EditText -> view.typeface = typeface
            is Button -> view.typeface = typeface
            is CheckBox -> view.typeface = typeface
            is RadioButton -> view.typeface = typeface
            is Switch -> view.typeface = typeface
            is ToggleButton -> view.typeface = typeface
            is MaterialButton -> view.typeface = typeface
            is TextInputEditText -> view.typeface = typeface
            is MaterialCheckBox -> view.typeface = typeface
            is MaterialRadioButton -> view.typeface = typeface
            is SwitchMaterial -> view.typeface = typeface
            is MaterialTextView -> view.typeface = typeface
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyMonospaceFont(view.getChildAt(i), typeface)
            }
        }
    }
}
