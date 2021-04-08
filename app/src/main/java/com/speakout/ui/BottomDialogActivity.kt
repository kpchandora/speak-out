package com.speakout.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.speakout.R
import com.speakout.extensions.addViewObserver
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.hideKeyboard
import com.speakout.extensions.showKeyboard
import kotlinx.android.synthetic.main.activity_bottom_dialog.*

class BottomDialogActivity : Activity() {

    companion object {
        const val CONTENT = "content"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.gravity = Gravity.BOTTOM
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes.verticalMargin = 0f
        window.attributes.dimAmount = .5f
        setContentView(R.layout.activity_bottom_dialog)
        setFinishOnTouchOutside(false)

        bottom_dialog_main_layout.addViewObserver {
            val screenSize = getScreenSize()
            bottom_dialog_main_layout.layoutParams.height = screenSize.heightPixels / 3
            bottom_dialog_main_layout.layoutParams.width = screenSize.widthPixels
            bottom_dialog_done_btn.layoutParams.width = screenSize.widthPixels / 4
            bottom_dialog_main_layout.requestLayout()
        }

        intent.extras?.let {
            it.getString(CONTENT)?.let { content ->
                bottom_dialog_et.setText(content)
                bottom_dialog_et.setSelection(content.length)
                bottom_dialog_done_btn.isEnabled = content.length > 10
            }
        }

        bottom_dialog_et.doOnTextChanged { text, start, count, after ->
            text?.toString()?.trim()?.let {
                bottom_dialog_done_btn.isEnabled = it.length > 10
            }
        }

        bottom_dialog_et.requestFocus()
        bottom_dialog_done_btn.setOnClickListener {
            hideKeyboard()
            val text = bottom_dialog_et.text.toString().trim()
            setResult(RESULT_OK, Intent().putExtra(CONTENT, text))
            finish()
        }

        showKeyboard()
    }

}
