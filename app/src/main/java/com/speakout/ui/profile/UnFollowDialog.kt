package com.speakout.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.speakout.R
import com.speakout.events.ProfileEventTypes
import com.speakout.events.ProfileEvents
import com.speakout.events.UserEventType
import com.speakout.events.UserEvents
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import com.speakout.users.UsersListFragment
import kotlinx.android.synthetic.main.dialog_unfollow.view.*

class UnFollowDialog : AppCompatDialogFragment() {

    private lateinit var dialogModel: UnFollowDialogModel
    private var mListener: UnFollowDialogListener? = null

    companion object {
        private const val ARG_MODEL = "ARG_MODEL"
        fun newInstance(model: UnFollowDialogModel): UnFollowDialog {
            return UnFollowDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MODEL, model)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogModel =
            arguments!!.getParcelable<UnFollowDialogModel>(ARG_MODEL) as UnFollowDialogModel
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_unfollow, container, false)
        isCancelable = false
        view.dialog_unfollow_confirm.setOnClickListener {
            mListener?.onUnFollow(dialogModel.userId)
            dismiss()
        }
        view.dialog_unfollow_cancel.setOnClickListener {
            dismiss()
        }

        view.dialog_unfollow_iv.loadImage(
            dialogModel.profileUrl, makeRound = true,
            placeholder = R.drawable.ic_account_circle_grey
        )
        view.dialog_unfollow_hint_tv.text = getString(R.string.hint_un_follow, dialogModel.username)

        return view
    }

    fun setListener(listener: UnFollowDialogListener) {
        mListener = listener
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (6 * requireActivity().getScreenSize().widthPixels) / 7,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    interface UnFollowDialogListener {
        fun onUnFollow(userId: String)
    }

}