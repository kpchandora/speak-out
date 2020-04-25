package com.speakout.ui.profile

import androidx.lifecycle.*
import com.speakout.auth.AuthService
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.common.Event
import com.speakout.common.Result
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.create.PostData
import com.speakout.posts.PostsService
import com.speakout.ui.observers.FollowersFollowingsLiveData
import com.speakout.ui.observers.UserLiveData
import com.speakout.utils.ImageUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class ProfileViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mPostList = ArrayList<PostData>()

    val profileObserver = MediatorLiveData<UserDetails?>()
    val followersFollowingsObserver = MediatorLiveData<FollowersFollowingsData?>()

    private val _confirmUnfollow = MutableLiveData<Event<Unit>>()
    val confirmUnfollow: LiveData<Event<Unit>> = _confirmUnfollow

    private val _userDetails = MutableLiveData<String>()
    val userDetails: LiveData<UserDetails?> = _userDetails.switchMap {
        AuthService.getUserData(it)
    }

    private val _isFollowing = MutableLiveData<String>()
    val isFollowing: LiveData<Event<Boolean?>> = _isFollowing.switchMap {
        ProfileService.isFollowing(it)
    }

    private val _followUser = MutableLiveData<Event<Boolean>>()
    val followUser: LiveData<Event<Boolean>>
        get() = _followUser

    private val _unFollowUser = MutableLiveData<Event<Boolean>>()
    val unFollowUser: LiveData<Event<Boolean>>
        get() = _unFollowUser

    private val _uploadProfilePicture = MutableLiveData<Event<String?>>()
    val uploadProfilePicture: LiveData<Event<String?>>
        get() = _uploadProfilePicture

    private val _posts = MutableLiveData<Result<List<PostData>>>()
    val posts: LiveData<Result<List<PostData>>> = _posts

    fun getPosts(id: String) {
        viewModelScope.launch {
//            _posts.value = PostsService.getProfilePosts(id)
        }
    }

    fun uploadProfilePicture(imageFile: File) {
        compositeDisposable += ImageUtils.uploadImageFromFile(imageFile)
            .withDefaultSchedulers()
            .subscribe({
                _uploadProfilePicture.value = Event(it)
            }, {
                Timber.d("Error: $it")
                _uploadProfilePicture.value = Event(null)
            })

    }

    fun isFollowing(userId: String) {
        _isFollowing.value = userId
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
        compositeDisposable += ProfileService.follow(userMiniDetails)
            .withDefaultSchedulers()
            .subscribe({
                _followUser.value = Event(it)
            }, {
                _followUser.value = Event(false)
            })
    }

    fun unFollowUser(userMiniDetails: UserMiniDetails) {
        compositeDisposable += ProfileService.unFollowUser(userMiniDetails)
            .withDefaultSchedulers()
            .subscribe({
                _unFollowUser.value = Event(it)
            }, {
                _unFollowUser.value = Event(false)
            })
    }

    fun confirmUnfollow() {
        _confirmUnfollow.value = Event(Unit)
    }

    fun addPosts(list: List<PostData>) {
        mPostList.addAll(list)
    }

    fun getPosts() = mPostList

}