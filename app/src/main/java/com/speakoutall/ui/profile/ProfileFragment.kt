package com.speakoutall.ui.profile

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.auth.UserDetails
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.events.*
import com.speakoutall.extensions.*
import com.speakoutall.posts.PostsRepository
import com.speakoutall.posts.create.PostData
import com.speakoutall.ui.MainActivity
import com.speakoutall.ui.home.HomeViewModel
import com.speakoutall.users.ActionType
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.Constants
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_profile.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*

class ProfileFragment : Fragment(), MainActivity.BottomIconDoubleClick {

    companion object {
        const val TAG = "ProfileFragment"
    }

    private val profileViewModel: ProfileViewModel by navGraphViewModels(R.id.profile_navigation) {
        val appPreference = AppPreference
        ProfileViewModel(
            appPreference,
            UsersRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }
    private val homeViewModel: HomeViewModel by navGraphViewModels(R.id.profile_navigation) {
        val appPreference = AppPreference
        HomeViewModel(
            appPreference,
            PostsRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }
    private lateinit var mPostsAdapter: ProfilePostsAdapter
    private var mUserId = ""
    private var isSelf = false
    private lateinit var screenSize: DisplayMetrics
    private var mUserDetails: UserDetails? = null
    private lateinit var safeArgs: ProfileFragmentArgs
    private var mProfileEvents: ProfileEvents? = null
    private var isLoading = false
    private var key: Long = 0L
    private var postEvents: PostEvents? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        safeArgs = ProfileFragmentArgs.fromBundle(arguments!!)
        mUserId = safeArgs.userId ?: ""
        isSelf = mUserId == AppPreference.getUserId()
        screenSize = requireActivity().getScreenSize()

        mPostsAdapter = ProfilePostsAdapter(homeViewModel.mPostList)

        mProfileEvents = ProfileEvents(requireContext()) {
            val userId = it.getStringExtra(ProfileEvents.USER_ID) ?: return@ProfileEvents
            when (it.getIntExtra(ProfileEvents.EVENT_TYPE, -1)) {
                ProfileEventTypes.CREATE_POST,
                ProfileEventTypes.DELETE_POST -> {
                    if (userId == mUserId) {
                        key = 0
                        homeViewModel.mPostList.clear()
                        homeViewModel.getProfilePosts(mUserId, key)
                    }
                }
                ProfileEventTypes.FOLLOW,
                ProfileEventTypes.UN_FOLLOW,
                ProfileEventTypes.DETAILS_UPDATE -> {
                    if (userId == mUserId || isSelf) {
                        profileViewModel.getUser(mUserId)
                    }
                }
            }
        }
        postEvents = PostEvents(requireContext()) {
            val postId: String = it.getStringExtra(PostEvents.POST_ID) ?: ""
            when (it.extras?.getInt(PostEvents.EVENT_TYPE)) {
                PostEventTypes.DELETE -> mPostsAdapter.deletePost(postId)
                PostEventTypes.LIKE -> mPostsAdapter.addLike(postId)
                PostEventTypes.REMOVE_LIKE -> mPostsAdapter.removeLike(postId)
                PostEventTypes.ADD_BOOKMARK -> mPostsAdapter.addBookmark(postId)
                PostEventTypes.REMOVE_BOOKMARK -> mPostsAdapter.removeBookmark(postId)
            }
        }
        homeViewModel.getProfilePosts(mUserId, key)
        profileViewModel.getUser(mUserId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        safeArgs.transitionTag?.let {
            sharedElementEnterTransition =
                TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setUpWithAppBarConfiguration(view)?.let {

            if (isSelf) {
                it.iv_settings.visible()
                it.toolbar_title.text = AppPreference.getUserUniqueName()
            } else {
                it.toolbar_title.text = safeArgs.username
                it.iv_settings.gone()
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        safeArgs.transitionTag?.let {
            layout_profile_iv.transitionName = it
            loadImage(safeArgs.profileUrl)
        }

        iv_settings.setOnClickListener {
            val action =
                ProfileFragmentDirections.actionNavigationProfileToProfileOptionsBottomSheetFragment()
            findNavController().navigate(action)
        }

        swipe_profile.setOnRefreshListener {
            view_empty_profile_posts.gone()
            key = 0
            homeViewModel.mPostList.clear()
            mPostsAdapter.notifyDataSetChanged()
            homeViewModel.getProfilePosts(mUserId, key)
            profileViewModel.getUser(mUserId)
        }

        mPostsAdapter.mListener = mListener
        profile_post_rv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mPostsAdapter
        }

        profile_post_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || key == Constants.INVALID_KEY) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            homeViewModel.getProfilePosts(mUserId, key)
                            isLoading = true
                        }
                    }
                }
            }
        })

        profileViewModel.userDetails.observe(viewLifecycleOwner, Observer { result ->
            swipe_profile.isRefreshing = false
            if (result is Result.Success) {
                populateData(result.data)
            }
        })

        homeViewModel.posts.observe(viewLifecycleOwner, Observer {
            if (homeViewModel.mPostList.isEmpty()) {
                view_empty_profile_posts.visible()
            } else {
                view_empty_profile_posts.gone()
            }
            swipe_profile.isRefreshing = false
            isLoading = false
            key = it.key
            mPostsAdapter.notifyDataSetChanged()
        })

        homeViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
            swipe_profile.isRefreshing = false
            isLoading = false
            showShortToast(it)
        })

        layout_profile_followers_container.setOnClickListener {
            navigateToUsersList(ActionType.Followers)
        }

        layout_profile_followings_container.setOnClickListener {
            navigateToUsersList(ActionType.Followings)
        }

    }

    override fun onDestroy() {
        mProfileEvents?.dispose()
        postEvents?.dispose()
        super.onDestroy()
    }

    private fun navigateToUsersList(actionType: ActionType) {
        findNavController().navigate(
            ProfileFragmentDirections.actionNavigationProfileToUsersListFragment(
                id = mUserId,
                actionType = actionType
            )
        )
    }

    private fun initSelf() {
        layout_profile_follow_unfollow_frame.apply {
            layoutParams.width = screenSize.widthPixels / 2
            setOnClickListener {
                mUserDetails?.let {
                    findNavController().navigate(
                        ProfileFragmentDirections.actionNavigationProfileToProfileEditFragment(it)
                    )
                }
            }
        }

        showEdit()
    }


    private fun initOther(userDetails: UserDetails) {
        layout_profile_follow_unfollow_frame.apply {
            layoutParams.width = screenSize.widthPixels / 2
            setOnClickListener {
                val text = follow_unfollow_tv.text
                if (text == getString(R.string.follow)) {
                    layout_profile_follow_unfollow_frame.invisible()
                    follow_unfollow_progress.visible()
                    profileViewModel.followUser(userDetails.userId)
                } else if (text == getString(R.string.following)) {
                    showUnFollowAlertDialog()
                }
            }
        }

        profileViewModel.followUser.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                PostEvents.sendEvent(context = requireContext(), event = PostEventTypes.FOLLOW)
                UserEvents.sendEvent(
                    context = requireContext(),
                    userId = it.data.userId,
                    type = UserEventType.FOLLOW
                )
                mUserDetails = it.data
                populateFollowersAndFollowings(it.data)
            } else {
                mUserDetails?.let { userDetails ->
                    populateFollowersAndFollowings(userDetails)
                }
                requireActivity().showShortToast("Failed to follow user")
            }
        })

        profileViewModel.unFollowUser.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                mUserDetails = it.data
                PostEvents.sendEvent(context = requireContext(), event = PostEventTypes.UN_FOLLOW)
                UserEvents.sendEvent(
                    context = requireContext(),
                    userId = it.data.userId,
                    type = UserEventType.UN_FOLLOW
                )
                populateFollowersAndFollowings(it.data)
            } else {
                mUserDetails?.let { userDetails ->
                    populateFollowersAndFollowings(userDetails)
                }
                requireActivity().showShortToast("Failed to unfollow user")
            }
        })

        profileViewModel.confirmUnfollow.observe(viewLifecycleOwner, EventObserver {
            layout_profile_follow_unfollow_frame.invisible()
            follow_unfollow_progress.visible()
            profileViewModel.unFollowUser(userDetails.userId)
        })

    }

    private fun showFollow() {
        layout_profile_follow_unfollow_frame.visible()
        follow_unfollow_progress.gone()
        layout_profile_follow_unfollow_frame.setBackgroundResource(R.drawable.dr_follow_bg)
        follow_unfollow_tv.text = getString(R.string.follow)
        follow_unfollow_tv.setTextColor(ContextCompat.getColor(context!!, R.color.white))
    }

    private fun showFollowing() {
        follow_unfollow_progress.gone()
        layout_profile_follow_unfollow_frame.visible()
        layout_profile_follow_unfollow_frame.setBackgroundResource(R.drawable.dr_unfollow_bg)
        follow_unfollow_tv.text = getString(R.string.following)
        follow_unfollow_tv.setTextColor(ContextCompat.getColor(context!!, R.color.black))
    }

    private fun showEdit() {
        showFollowing()
        follow_unfollow_tv.text = getString(R.string.edit)
    }

    private fun populateData(userDetails: UserDetails) {

        mUserDetails = userDetails

        loadImage(userDetails.photoUrl)

        layout_profile_full_name_tv.text = userDetails.name

        layout_profile_posts_count_tv.text =
            if (userDetails.postsCount > 0) userDetails.postsCount.toString() else "0"
        layout_profile_posts_tv.text =
            resources.getQuantityString(R.plurals.number_of_posts, userDetails.postsCount.toInt())

        populateFollowersAndFollowings(userDetails)

        if (isSelf) {
            initSelf()
        } else {
            initOther(userDetails)
        }

    }

    private fun populateFollowersAndFollowings(userDetails: UserDetails) {
        layout_profile_followers_count_tv.text = userDetails.followersCount.toString()
        layout_profile_followers_tv.text =
            resources.getQuantityString(
                R.plurals.number_of_followers,
                userDetails.followersCount.toInt()
            )

        layout_profile_followings_count_tv.text = userDetails.followingsCount.toString()
        layout_profile_followings_tv.text =
            resources.getQuantityString(
                R.plurals.number_of_followings,
                userDetails.followingsCount.toInt()
            )

        if (userDetails.followersCount <= 0L) {
            layout_profile_followers_container.disable()
        } else {
            layout_profile_followers_container.enable()
        }

        if (userDetails.followingsCount <= 0L) {
            layout_profile_followings_container.disable()
        } else {
            layout_profile_followings_container.enable()
        }

        if (userDetails.isFollowedBySelf) {
            showFollowing()
        } else {
            showFollow()
        }

    }

    private fun loadImage(photoUrl: String?) {
        card_view.layoutParams.width = screenSize.widthPixels / 3
        layout_profile_bg_view.layoutParams.width = screenSize.widthPixels / 3

        layout_profile_bg_view.gone()
        layout_profile_iv.loadImageWithCallback(photoUrl ?: "", centerCrop = true,
            onSuccess = {
//                layout_profile_bg_view.visible()
            },
            onFailed = {
                layout_profile_bg_view.gone()
                layout_profile_iv.setImageDrawable(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_account_circle_grey
                    )
                )
            })
    }

    private fun showUnFollowAlertDialog() {
        val model = UnFollowDialogModel(
            userId = mUserId, username = mUserDetails?.username ?: "",
            isFrom = TAG, profileUrl = mUserDetails?.photoUrl
        )
        val dialog = UnFollowDialog.newInstance(model)
        dialog.setListener(object : UnFollowDialog.UnFollowDialogListener {
            override fun onUnFollow(userId: String) {
                profileViewModel.confirmUnfollow()
            }
        })
        dialog.show(requireActivity().supportFragmentManager, TAG)
    }


    override fun doubleClick() {
        fragment_profile_scroll_view.smoothScrollTo(0, 0)
    }

    private val mListener = object : ProfilePostClickListener {
        override fun onPostClick(postData: PostData, postImageView: ImageView, position: Int) {
            val action =
                ProfileFragmentDirections.actionNavigationProfileToPostViewFragment(position)
            findNavController().navigate(action)
        }
    }

}