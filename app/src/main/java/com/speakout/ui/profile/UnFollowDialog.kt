package com.speakout.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import kotlinx.android.synthetic.main.dialog_unfollow.view.*

class UnFollowDialog : AppCompatDialogFragment() {

    companion object {
        const val USER_DETAILS = "user_details"
        fun newInstance(args: Bundle?) = UnFollowDialog().apply {
            arguments = args
        }
    }

    private var mListener: OnUnFollowClickListener? = null
    private var mUserDetails: UserDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        mUserDetails = arguments?.getParcelable(USER_DETAILS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_unfollow, container, false)
        isCancelable = false
        view.dialog_unfollow_confirm.setOnClickListener {
            mListener?.onUnFollow(mUserDetails?.userId ?: "")
            dismiss()
        }
        view.dialog_unfollow_cancel.setOnClickListener {
            dismiss()
        }

        arguments?.let {
            mUserDetails?.let { userDetails ->
                view.dialog_unfollow_iv.loadImage(
                    userDetails.photoUrl, makeRound = true,
                    placeholder = R.drawable.ic_account_circle_grey
                )
                view.dialog_unfollow_hint_tv.text =
                    "You won't get updates from @${userDetails.username}"
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (6 * requireActivity().getScreenSize().widthPixels) / 7,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    fun setListener(listener: OnUnFollowClickListener) {
        mListener = listener
    }

    interface OnUnFollowClickListener {
        fun onUnFollow(userId: String)
    }

}