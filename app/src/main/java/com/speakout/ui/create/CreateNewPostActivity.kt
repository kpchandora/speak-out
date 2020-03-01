package com.speakout.ui.create

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.speakout.R
import com.speakout.extensions.addViewObserver
import com.speakout.extensions.getScreenSize
import com.speakout.ui.BottomDialogActivity
import com.speakout.utils.StringUtils
import kotlinx.android.synthetic.main.activity_create_new_post.*

class CreateNewPostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_post)


        create_post_content_tv.setOnClickListener {
            startActivityForResult(
                Intent(this, BottomDialogActivity::class.java).putExtra(
                    BottomDialogActivity.CONTENT, create_post_content_tv.text
                ), StringUtils.IntentStrings.CreatePost.REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == StringUtils.IntentStrings.CreatePost.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let {
                    it.getString(BottomDialogActivity.CONTENT)?.let { content ->
                        create_post_content_tv.text = content
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
