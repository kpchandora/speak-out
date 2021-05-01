package com.speakoutall.ui.profile

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

import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.auth.UserDetails
import com.speakoutall.auth.Type
import com.speakoutall.auth.UserViewModel
import com.speakoutall.auth.UsersItem
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.databinding.FragmentProfileEditBinding
import com.speakoutall.events.PostEventTypes
import com.speakoutall.events.PostEvents
import com.speakoutall.events.ProfileEventTypes
import com.speakoutall.events.ProfileEvents
import com.speakoutall.extensions.*
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.ImageUtils
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.layout_toolbar.view.*
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
    private lateinit var mDataBinding: FragmentProfileEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserDetails = safeArgs.userDetails
        mProfileUrl = mUserDetails.photoUrl ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDataBinding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return mDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(view)?.toolbar_title?.text = getString(R.string.edit)

        populateDetails()

        observeViewModels()

        mDataBinding.profileEditFragmentIv.setOnClickListener {
            if (!isUploading) {
                pickImage()
            }
        }

        mDataBinding.profileEditUpdateBtn.setOnClickListener {
            userViewModel.updateUserDetails(
                UsersItem(
                    userId = AppPreference.getUserId(),
                    photoUrl = mProfileUrl,
                    name = mDataBinding.profileEditFullNameEt.text.toString().trim(),
                    phoneNumber = mDataBinding.profileEditMobileEt.text.toString().trim()
                )
            )
        }

        mDataBinding.profileEditFullNameEt.doAfterTextChanged { text: Editable? ->
            val isValid =
                mDataBinding.profileEditFullNameTil.checkAndShowError(
                    text,
                    getString(R.string.error_empty)
                )
            mDataBinding.profileEditUpdateBtn.isEnabled = isValid
        }

        mDataBinding.profileEditMobileEt.doAfterTextChanged {
            it?.let {
                if (it.toString().trim().isEmpty() || it.toString().trim().length == 10) {
                    mDataBinding.profileEditMobileTil.error = null
                    mDataBinding.profileEditUpdateBtn.enable()
                } else {
                    mDataBinding.profileEditUpdateBtn.disable()
                    mDataBinding.profileEditMobileTil.error = getString(R.string.mobile_error)
                }
            }
        }

        mDataBinding.profileEditUsernameEt.setOnClickListener {
            val action = ProfileEditFragmentDirections
                .actionProfileEditFragmentToUserNameFragment(
                    type = Type.Edit,
                    username = mDataBinding.profileEditUsernameEt.text.toString()
                )

            findNavController().navigate(action)
        }
    }

    private fun populateDetails() {
        val screenSize = requireActivity().getScreenSize()
        mDataBinding.profileEditUpdateBtn.layoutParams.width = screenSize.widthPixels / 4
        mDataBinding.profileEditFragmentIv.layoutParams.width = screenSize.widthPixels / 3
        mDataBinding.profileEditBgView.layoutParams.width = screenSize.widthPixels / 3

        updatePicture()

        mDataBinding.profileEditAddIv.layoutParams.width = screenSize.widthPixels / 10
        mDataBinding.profileEditPb.layoutParams.width = screenSize.widthPixels / 10
        mDataBinding.profileEditUsernameEt.setText(mUserDetails.username)
        mDataBinding.profileEditFullNameEt.setText(mUserDetails.name)
        mDataBinding.profileEditFullNameEt.setSelection(mUserDetails.name?.length ?: 0)
        mDataBinding.profileEditMobileEt.setText(mUserDetails.phoneNumber ?: "")

    }

    private fun updatePicture() {
        mDataBinding.profileEditBgView.gone()
        mDataBinding.profileEditFragmentIv.loadImage(
            url = mProfileUrl,
            makeRound = true,
            placeholder = R.drawable.ic_account_circle_grey
        )
    }

    private fun observeViewModels() {
        profileViewModel.uploadProfilePicture.observe(requireActivity(), EventObserver {
            Timber.d("Picture Uploaded")
            isUploading = false
            mDataBinding.profileEditPb.gone()
            mDataBinding.profileEditUpdateBtn.enable()
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
                mDataBinding.profileEditUsernameEt.setText(it)
            }

        userViewModel.updateUserDetails.observe(requireActivity(), EventObserver {
            if (it is Result.Success) {
                PostEvents.sendEvent(
                    context = requireContext(),
                    event = PostEventTypes.USER_DETAILS_UPDATE
                )
                ProfileEvents.sendEvent(
                    context = requireContext(),
                    userId = it.data.userId,
                    eventType = ProfileEventTypes.DETAILS_UPDATE
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
            .flatMap {
                val path = ImageUtils.compressImage(it.path, requireContext()) ?: throw Exception()
                Observable.just(path)
            }
            .subscribe({
                if (it != null) {
                    mDataBinding.profileEditPb.visible()
                    mDataBinding.profileEditUpdateBtn.disable()
                    isUploading = true
                    profileViewModel.uploadProfilePicture(File(it))
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
