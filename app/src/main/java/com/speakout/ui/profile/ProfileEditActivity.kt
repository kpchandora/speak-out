package com.speakout.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.mlsdev.rximagepicker.RxImageConverters
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.auth.UserNameActivity
import com.speakout.auth.UserViewModel
import com.speakout.extensions.*
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.activity_bottom_dialog.*
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.android.synthetic.main.activity_profile_edit.profile_edit_username_et
import kotlinx.android.synthetic.main.activity_profile_edit.profile_edit_username_til
import kotlinx.android.synthetic.main.activity_user_name.*
import org.koin.android.viewmodel.compat.ViewModelCompat.viewModel
import timber.log.Timber
import java.io.File

class ProfileEditActivity : AppCompatActivity() {


    companion object {
        private const val REQUEST_CODE_USERNAME = 1001
    }

    private val profileViewModel: ProfileViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var mProfileUrl = ""
    private var isUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        mProfileUrl = AppPreference.getPhotoUrl()
        populateDetails()

        observeViewModels()

        profile_edit_iv.setOnClickListener {
            if (!isUploading) {
                pickImage()
            }
        }

        profile_edit_update_btn.setOnClickListener {
            userViewModel.updateUserDetails(
                mapOf(
                    UserDetails.updatePhoto(mProfileUrl),
                    UserDetails.updateName(profile_edit_full_name_et.text.toString().trim()),
                    UserDetails.updateNumber(profile_edit_mobile_et.text.toString().trim()),
                    UserDetails.updateTimeStamp(System.currentTimeMillis())
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
            openActivityForResult(
                clazz = UserNameActivity::class.java,
                requestCode = REQUEST_CODE_USERNAME,
                extras = Bundle().also {
                    it.putString("username", profile_edit_username_et.text.toString())
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_USERNAME && resultCode == Activity.RESULT_OK) {
            profile_edit_username_et.setText(data?.getStringExtra("username") ?: "")
        }
    }

    private fun populateDetails() {
        val screenSize = getScreenSize()
        Timber.d("Height: ${screenSize.heightPixels}, Width: ${screenSize.widthPixels}")
        profile_edit_update_btn.layoutParams.width = screenSize.widthPixels / 4
        profile_edit_iv.layoutParams.width = screenSize.widthPixels / 3
        profile_edit_bg_view.layoutParams.width = screenSize.widthPixels / 3
        updatePicture(AppPreference.getPhotoUrl())
        profile_edit_add_iv.layoutParams.width = screenSize.widthPixels / 10
        profile_edit_pb.layoutParams.width = screenSize.widthPixels / 10
        profile_edit_username_et.setText(AppPreference.getUserUniqueName())
        profile_edit_full_name_et.setText(AppPreference.getUserDisplayName())
        profile_edit_full_name_et.setSelection(AppPreference.getUserDisplayName().length)
        profile_edit_mobile_et.setText(AppPreference.getPhoneNumber())
    }

    private fun updatePicture(url: String) {
        profile_edit_bg_view.gone()
        profile_edit_iv.loadImageWithCallback(url, makeRound = true,
            onSuccess = {
                profile_edit_bg_view.visible()
            },
            onFailed = {
                profile_edit_bg_view.gone()
                profile_edit_iv.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_account_circle_grey
                    )
                )
            })
    }

    private fun observeViewModels() {
        profileViewModel.uploadProfilePicture.observe(this, Observer {
            isUploading = false
            profile_edit_pb.gone()
            profile_edit_update_btn.enable()
            if (it.isNotNullOrEmpty()) {
                mProfileUrl = it!!
                updatePicture(mProfileUrl)
            } else {
                updatePicture(mProfileUrl)
                showShortToast("Failed to upload profile picture")
            }
        })

        userViewModel.updateDetailsObserver.observe(this, Observer {
            if (it) {
                finish()
            } else {
                showShortToast("Failed to update details")
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun pickImage() {
        RxImagePicker.with(supportFragmentManager).requestImage(Sources.GALLERY)
            .flatMap {
                RxImageConverters.uriToFile(this, it, createTempFile())
            }
            .subscribe({
                if (it != null) {
                    profile_edit_pb.visible()
                    profile_edit_update_btn.disable()
                    isUploading = true
                    profileViewModel.uploadProfilePicture(it)
                } else {
                    showShortToast("Failed to get image file")
                }
            }, {
                showShortToast("Failed to get image file")
            })
    }

    private fun createTempFile(): File {
        return File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            System.currentTimeMillis().toString() + "_image.jpeg"
        )
    }

    override fun onBackPressed() {
        if (isUploading) return
        super.onBackPressed()
    }
}
