package com.speakout.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import kotlinx.android.synthetic.main.dialog_unfollow.view.*
import timber.log.Timber

class UnFollowDialog : AppCompatDialogFragment() {

    private val safeArgs: UnFollowDialogArgs by navArgs()
    private val profileViewModel: ProfileViewModel by navGraphViewModels(R.id.profile_navigation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView: $profileViewModel")
        val view = inflater.inflate(R.layout.dialog_unfollow, container, false)
        isCancelable = false
        view.dialog_unfollow_confirm.setOnClickListener {
            profileViewModel.confirmUnfollow()
            dismiss()
        }
        view.dialog_unfollow_cancel.setOnClickListener {
            dismiss()
        }

        view.dialog_unfollow_iv.loadImage(
            safeArgs.profileUrl, makeRound = true,
            placeholder = R.drawable.ic_account_circle_grey
        )
        view.dialog_unfollow_hint_tv.text =
            "You won't get updates from @${safeArgs.username}"

        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (6 * requireActivity().getScreenSize().widthPixels) / 7,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

}