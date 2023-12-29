package com.speakoutall.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.speakoutall.R
import com.speakoutall.databinding.DialogUnfollowBinding
import com.speakoutall.extensions.getScreenSize
import com.speakoutall.extensions.loadImage

class UnFollowDialog : AppCompatDialogFragment() {

    private lateinit var dialogModel: UnFollowDialogModel
    private var mListener: UnFollowDialogListener? = null
    private var _binding: DialogUnfollowBinding? = null

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
        _binding = DialogUnfollowBinding.inflate(inflater, container, false)
        isCancelable = false
        _binding?.dialogUnfollowConfirm?.setOnClickListener {
            mListener?.onUnFollow(dialogModel.userId)
            dismiss()
        }
        _binding?.dialogUnfollowCancel?.setOnClickListener {
            dismiss()
        }

        _binding?.dialogUnfollowIv?.loadImage(
            dialogModel.profileUrl, makeRound = true,
            placeholder = R.drawable.ic_account_circle_grey
        )
        _binding?.dialogUnfollowHintTv?.text =
            getString(R.string.hint_un_follow, dialogModel.username)

        return _binding?.root
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