package com.speakout.ui.profile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.speakout.R
import com.speakout.databinding.DialogLogoutBinding
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import com.speakout.ui.SplashScreen
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils

class LogoutDialog : AppCompatDialogFragment() {

    private lateinit var mBinding: DialogLogoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        isCancelable = false
        mBinding = DialogLogoutBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ivProfile.loadImage(
            AppPreference.getPhotoUrl(),
            placeholder = R.drawable.ic_account_circle_grey, makeRound = true
        )
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
        mBinding.tvLogout.setOnClickListener {
            dismiss()
            FirebaseUtils.signOut(requireActivity())
            val intent = Intent(requireContext(), SplashScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (6 * requireActivity().getScreenSize().widthPixels) / 7,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

}