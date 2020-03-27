package com.speakout.ui.profile

import androidx.lifecycle.*
import com.speakout.auth.AuthService
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.create.PostData
import com.speakout.ui.home.HomeService
import com.speakout.ui.observers.FollowersFollowingsLiveData
import com.speakout.ui.observers.UserLiveData
import com.speakout.utils.ImageUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.io.File

class ProfileViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val profileObserver = MediatorLiveData<UserDetails?>()
    val followersFollowingsObserver = MediatorLiveData<FollowersFollowingsData?>()

    private val _userDetails = MutableLiveData<String>()

    val userDetails: LiveData<UserDetails?> = _userDetails.switchMap {
        AuthService.getUserData(it)
    }

    private val _followUser = MutableLiveData<UserMiniDetails>()
    val followUser: LiveData<Boolean> = _followUser.switchMap {
        ProfileService.unfollowUser(it)
    }

    private val _uploadProfilePicture = MutableLiveData<String?>()
    val uploadProfilePicture: LiveData<String?>
        get() = _uploadProfilePicture

    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        HomeService.getPosts(it)
    }

    fun getPosts(id: String) {
        _posts.value = id
    }

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

    fun addFFObserver(userId: String) {
        followersFollowingsObserver.addSource(FollowersFollowingsLiveData(userId)) {
            followersFollowingsObserver.value = it
        }
    }

    fun addProfileObserver() {
        profileObserver.addSource(UserLiveData()) {
            profileObserver.value = it
        }
    }

    fun getUser(uid: String) {
        _userDetails.value = uid
    }

    fun followUser(userMiniDetails: UserMiniDetails) {
        _followUser.value = userMiniDetails
    }

}