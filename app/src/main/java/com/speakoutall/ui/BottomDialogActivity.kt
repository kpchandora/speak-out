package com.speakoutall.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.core.widget.doOnTextChanged
import com.speakoutall.R
import com.speakoutall.databinding.ActivityBottomDialogBinding
import com.speakoutall.extensions.addViewObserver
import com.speakoutall.extensions.getScreenSize
import com.speakoutall.extensions.hideKeyboard
import com.speakoutall.extensions.showKeyboard

class BottomDialogActivity : Activity() {

    companion object {
        const val CONTENT = "content"
    }

    private lateinit var _binding: ActivityBottomDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.gravity = Gravity.BOTTOM
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes.verticalMargin = 0f
        window.attributes.dimAmount = .5f
        _binding = ActivityBottomDialogBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        setFinishOnTouchOutside(false)

        _binding.bottomDialogMainLayout.addViewObserver {
            val screenSize = getScreenSize()
            _binding.bottomDialogMainLayout.layoutParams.height = screenSize.heightPixels / 3
            _binding.bottomDialogMainLayout.layoutParams.width = screenSize.widthPixels
            _binding.bottomDialogDoneBtn.layoutParams.width = screenSize.widthPixels / 4
            _binding.bottomDialogMainLayout.requestLayout()
        }

        intent.extras?.let {
            it.getString(CONTENT)?.let { content ->
                _binding.bottomDialogEt.setText(content)
                _binding.bottomDialogEt.setSelection(content.length)
                _binding.bottomDialogDoneBtn.isEnabled = content.length > 10
            }
        }

        _binding.bottomDialogEt.doOnTextChanged { text, start, count, after ->
            text?.toString()?.trim()?.let {
                _binding.bottomDialogDoneBtn.isEnabled = it.length > 10
            }
        }

        _binding.bottomDialogEt.requestFocus()
        _binding.bottomDialogDoneBtn.setOnClickListener {
            hideKeyboard()
            val text = _binding.bottomDialogEt.text.toString().trim()
            setResult(RESULT_OK, Intent().putExtra(CONTENT, text))
            finish()
        }

        showKeyboard()
    }

    override fun finish() {
        overridePendingTransition(R.anim.slide_down, R.anim.slide_up)
        super.finish()
    }

}
