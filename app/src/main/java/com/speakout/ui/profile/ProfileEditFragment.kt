package com.speakout.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mlsdev.rximagepicker.RxImageConverters
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources

import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.UserDetails
import com.speakout.auth.Type
import com.speakout.auth.UserViewModel
import com.speakout.auth.UsersItem
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.events.PostEventTypes
import com.speakout.events.PostEvents
import com.speakout.extensions.*
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import timber.log.Timber
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class ProfileEditFragment : Fragment() {

    private var mProfileUrl = ""
    private var isUploading = false
    private val profileViewModel: ProfileViewModel by viewModels() {
        val appPreference = AppPreference
        ProfileViewModel(
            appPreference,
            UsersRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }
    private val userViewModel: UserViewModel by viewModels() {
        UserViewModel(UsersRepository(RetrofitBuilder.apiService, AppPreference)).createFactory()
    }
    private val safeArgs: ProfileEditFragmentArgs by navArgs()
    private lateinit var mUserDetails: UserDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserDetails = safeArgs.userDetails
        mProfileUrl = mUserDetails.photoUrl ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.post {
            setUpToolbar(view)?.let {
                it.title = getString(R.string.edit)
            }
        }

        populateDetails()

        observeViewModels()

        profile_edit_fragment_iv.setOnClickListener {
            if (!isUploading) {
                pickImage()
            }
        }

        profile_edit_update_btn.setOnClickListener {
            userViewModel.updateUserDetails(
                UsersItem(
                    userId = AppPreference.getUserId(),
                    photoUrl = mProfileUrl,
                    name = profile_edit_full_name_et.text.toString().trim(),
                    phoneNumber = profile_edit_mobile_et.text.toString().trim()
                )
            )
        }

        profile_edit_full_name_et.doAfterTextChanged { text: Editable? ->
            val isValid =
                profile_edit_full_name_til.checkAndShowError(text, getString(R.string.error_empty))
            profile_edit_update_btn.isEnabled = isValid
        }

        profile_edit_mobile_et.doAfterTextChanged {
            it?.let {
                if (it.toString().trim().isEmpty() || it.toString().trim().length == 10) {
                    profile_edit_mobile_til.error = null
                    profile_edit_update_btn.enable()
                } else {
                    profile_edit_update_btn.disable()
                    profile_edit_mobile_til.error = getString(R.string.mobile_error)
                }
            }
        }

        profile_edit_username_et.setOnClickListener {
            val action = ProfileEditFragmentDirections
                .actionProfileEditFragmentToUserNameFragment(
                    type = Type.Edit,
                    username = profile_edit_username_et.text.toString()
                )

            findNavController().navigate(action)
        }
    }

    private fun populateDetails() {
        val screenSize = requireActivity().getScreenSize()
        profile_edit_update_btn.layoutParams.width = screenSize.widthPixels / 4
        profile_edit_fragment_iv.layoutParams.width = screenSize.widthPixels / 3
        profile_edit_bg_view.layoutParams.width = screenSize.widthPixels / 3

        updatePicture()

        profile_edit_add_iv.layoutParams.width = screenSize.widthPixels / 10
        profile_edit_pb.layoutParams.width = screenSize.widthPixels / 10
        profile_edit_username_et.setText(mUserDetails.username)
        profile_edit_full_name_et.setText(mUserDetails.name)
        profile_edit_full_name_et.setSelection(mUserDetails.name?.length ?: 0)
        profile_edit_mobile_et.setText(mUserDetails.phoneNumber ?: "")

    }

    private fun updatePicture() {
        profile_edit_bg_view.gone()
        profile_edit_fragment_iv.loadImage(
            url = mProfileUrl,
            makeRound = true,
            placeholder = R.drawable.ic_account_circle_grey
        )
    }

    private fun observeViewModels() {
        profileViewModel.uploadProfilePicture.observe(requireActivity(), EventObserver {
            Timber.d("Picture Uploaded")
            isUploading = false
            profile_edit_pb.gone()
            profile_edit_update_btn.enable()
            if (it is Result.Success) {
                mProfileUrl = it.data
                updatePicture()
            } else {
                updatePicture()
                requireActivity().showShortToast("Failed to upload profile picture")
            }
        })

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("username")
            ?.observe(
                viewLifecycleOwner
            ) {
                profile_edit_username_et.setText(it)
            }

        userViewModel.updateUserDetails.observe(requireActivity(), EventObserver {
            if (it is Result.Success) {
                PostEvents.sendEvent(
                    context = requireContext(),
                    event = PostEventTypes.USER_DETAILS_UPDATE
                )
                findNavController().navigateUp()
            } else {
                requireActivity().showShortToast("Failed to update details")
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun pickImage() {
        RxImagePicker.with(requireActivity().supportFragmentManager).requestImage(Sources.GALLERY)
            .flatMap {
                RxImageConverters.uriToFile(requireContext(), it, createTempFile())
            }
            .subscribe({
                if (it != null) {
                    profile_edit_pb.visible()
                    profile_edit_update_btn.disable()
                    isUploading = true
                    profileViewModel.uploadProfilePicture(it)
                } else {
                    requireActivity().showShortToast("Failed to get image file")
                }
            }, {
                requireActivity().showShortToast(it.message ?: "Failed to get image file")
            })
    }

    private fun createTempFile(): File {
        return File(
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            System.currentTimeMillis().toString() + "_image.jpeg"
        )
    }

}
