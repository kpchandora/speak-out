package com.speakoutall.posts.view

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.speakoutall.databinding.DialogPostOptionsBinding
import com.speakoutall.extensions.addViewObserver
import com.speakoutall.extensions.getScreenSize
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.visible
import com.speakoutall.posts.create.PostData
import com.speakoutall.utils.AppPreference

class PostOptionsDialog(private val mContext: Context) : Dialog(mContext) {

    private var mListener: OnPostOptionsClickListener? = null
    private var mPost: PostData? = null
    private var mPostView: View? = null
    private var binding: DialogPostOptionsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogPostOptionsBinding.inflate(layoutInflater)
        binding?.run {
            dialogPostContainer.addViewObserver {
                (mContext as? Activity)?.getScreenSize()?.widthPixels?.let {
                    dialogPostContainer.layoutParams.width = 6 * it / 7
                    dialogPostContainer.requestLayout()
                }
            }

            dialogOptionSave.setOnClickListener {
                dismiss()
                mListener?.onSave(mPost ?: PostData(), mPostView)
            }

            dialogOptionCopy.setOnClickListener {
                dismiss()
                mListener?.onCopy(mPost ?: PostData())
            }

            dialogOptionDelete.setOnClickListener {
                dismiss()
                mListener?.onDelete(mPost ?: PostData())
            }
        }
    }

    fun setPost(post: PostData) {
        mPost = post
        if (AppPreference.getUserId() == mPost?.userId) {
            binding?.dialogOptionDelete?.visible()
        } else {
            binding?.dialogOptionDelete?.gone()
        }
    }

    fun setPostView(view: View) {
        mPostView = view
    }

    fun setListener(listener: OnPostOptionsClickListener) {
        mListener = listener
    }

}