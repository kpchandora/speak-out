package com.speakout.ui.profile

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.mlsdev.rximagepicker.RxImageConverters
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.auth.UserViewModel
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.isNotNullOrEmpty
import com.speakout.extensions.loadImage
import com.speakout.extensions.showShortToast
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.activity_profile_edit.*
import org.koin.android.viewmodel.compat.ViewModelCompat.viewModel
import timber.log.Timber
import java.io.File

class ProfileEditActivity : AppCompatActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        populateDetails()

        observeViewModels()

        profile_edit_iv.setOnClickListener {
            pickImage()
        }

    }

    private fun populateDetails() {
        val screenSize = getScreenSize()
        Timber.d("Height: ${screenSize.heightPixels}, Width: ${screenSize.widthPixels}")
        profile_edit_update_btn.layoutParams.width = screenSize.widthPixels / 4
        profile_edit_iv.layoutParams.width = screenSize.widthPixels / 3
        profile_edit_add_iv.layoutParams.width = screenSize.widthPixels / 10
        profile_edit_pb.layoutParams.width = screenSize.widthPixels / 10
        profile_edit_iv.loadImage(
            AppPreference.getPhotoUrl(),
            R.drawable.ic_profile_placeholder,
            true
        )
        profile_edit_username_et.setText(AppPreference.getUserUniqueName())
        profile_edit_full_name_et.setText(AppPreference.getUserDisplayName())
        profile_edit_mobile_et.setText(AppPreference.getPhoneNumber())
    }

    private fun observeViewModels() {
        profileViewModel.uploadProfilePicture.observe(this, Observer {
            if (it.isNotNullOrEmpty()) {
                userViewModel.updateUserDetails(
                    mapOf(
                        UserDetails.updatePhoto(it!!),
                        UserDetails.updateTimeStamp(System.currentTimeMillis())
                    )
                )
            } else {
                showShortToast("Failed")
            }
        })

        userViewModel.updateDetailsObserver.observe(this, Observer {
            if (it) {
                finish()
            } else {
                showShortToast("Failed")
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

}
