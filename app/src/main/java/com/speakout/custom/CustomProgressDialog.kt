package com.speakout.custom

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.speakout.R

class CustomProgressDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_progress_dialog)
        setCancelable(false)
    }
}