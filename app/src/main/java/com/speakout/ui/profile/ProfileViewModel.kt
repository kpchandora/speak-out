package com.speakout.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.speakout.auth.AuthService
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.people.FollowUnfollowService
import com.speakout.posts.create.PostData
import com.speakout.ui.UserLiveData
import com.speakout.utils.ImageUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class ProfileViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val profileObserver: LiveData<UserDetails?> = UserLiveData()

    private val _userDetails = MutableLiveData<String>()

    val userDetails: LiveData<UserDetails?> = _userDetails.switchMap {
        AuthService.getUserData(it)
    }

    private val _followUser = MutableLiveData<UserMiniDetails>()
    val followUser: LiveData<Boolean> = _followUser.switchMap {
        FollowUnfollowService.unfollow(it)
    }

    private val _uploadProfilePicture = MutableLiveData<String?>()
    val uploadProfilePicture: LiveData<String?>
        get() = _uploadProfilePicture

    fun uploadProfilePicture(imageFile: File) {
        compositeDisposable += ImageUtils.uploadImageFromFile(imageFile)
            .withDefaultSchedulers()
            .subscribe({
                _uploadProfilePicture.value = it
            }, {
                Timber.d("Error: $it")
                _uploadProfilePicture.value = null
            })

    }

    fun getUser(uid: String) {
        _userDetails.value = uid
    }

    fun followUser(userMiniDetails: UserMiniDetails) {
        _followUser.value = userMiniDetails
    }

}