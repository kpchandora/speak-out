package com.speakoutall.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.speakoutall.R
import com.speakoutall.databinding.FragmentProfileOptionsBottomSheetBinding
import com.speakoutall.ui.about.AboutActivity

class ProfileOptionsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var mBinding: FragmentProfileOptionsBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ProfileBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentProfileOptionsBottomSheetBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvBookmarks.setOnClickListener {
            val action =
                ProfileOptionsBottomSheetFragmentDirections.actionProfileOptionsToBookmarksPostFragment()
            findNavController().navigate(action)
        }
        mBinding.tvLogout.setOnClickListener {
            dismiss()
            val action =
                ProfileOptionsBottomSheetFragmentDirections.actionProfileOptionsBottomSheetFragmentToLogoutDialog()
            findNavController().navigate(action)
        }
        mBinding.tvAbout.setOnClickListener {
            dismiss()
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

}