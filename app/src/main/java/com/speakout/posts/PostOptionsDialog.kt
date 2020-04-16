package com.speakout.posts

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.speakout.R
import com.speakout.extensions.addViewObserver
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.gone
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.dialog_post_options.*
import timber.log.Timber

class PostOptionsDialog(private val mContext: Context) :
    Dialog(mContext) {

    private var mListener: OnPostOptionsClickListener? = null
    private var mPost: PostData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_post_options)
        dialog_post_container.addViewObserver {
            (mContext as? Activity)?.getScreenSize()?.widthPixels?.let {
                Timber.d("On create")
                dialog_post_container.layoutParams.width = 6 * it / 7
                dialog_post_container.requestLayout()
            }
        }

        dialog_option_save.setOnClickListener {
            dismiss()
            mListener?.onSave(mPost ?: PostData())
        }

        dialog_option_copy.setOnClickListener {
            dismiss()
            mListener?.onCopy(mPost ?: PostData())
        }

        dialog_option_delete.setOnClickListener {
            dismiss()
            mListener?.onDelete(mPost ?: PostData())
        }
    }

    fun setPost(post: PostData) {
        mPost = post
        if (AppPreference.getUserId() == mPost?.userId) {
            dialog_option_delete.visible()
        } else {
            dialog_option_delete.gone()
        }
    }

    fun setListener(listener: OnPostOptionsClickListener) {
        mListener = listener
    }

}