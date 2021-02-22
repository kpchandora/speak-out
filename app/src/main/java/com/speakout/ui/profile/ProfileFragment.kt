package com.speakout.ui.profile

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
import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.UserDetails
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.events.*
import com.speakout.extensions.*
import com.speakout.posts.PostsRepository
import com.speakout.posts.create.PostData
import com.speakout.ui.MainActivity
import com.speakout.ui.home.HomeViewModel
import com.speakout.users.ActionType
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_profile.*
import timber.log.Timber

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
    private var hasMoreData = true
    private var nextPageNumber = 1

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
                        nextPageNumber = 1
                        homeViewModel.getProfilePosts(mUserId, nextPageNumber)
                    }
                }
                ProfileEventTypes.DIALOG_UN_FOLLOW -> {
                    if (userId == mUserId) {
                        profileViewModel.confirmUnfollow()
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
        homeViewModel.getProfilePosts(mUserId, nextPageNumber)
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
            it.title = safeArgs.username
//            view.toolbar_title_tv.visible()
//            view.toolbar_title_tv.text = safeArgs.username
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        safeArgs.transitionTag?.let {
            layout_profile_iv.transitionName = it
            loadImage(safeArgs.profileUrl)
        }

        mPostsAdapter.mListener = mListener
        profile_post_rv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mPostsAdapter
        }

        profile_post_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || !hasMoreData) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            homeViewModel.getFeed(nextPageNumber)
                            isLoading = true
                        }
                    }
                }
            }
        })

        profileViewModel.userDetails.observe(viewLifecycleOwner, Observer { result ->
            if (result is Result.Success) {
                populateData(result.data)
            }
        })

        homeViewModel.posts.observe(viewLifecycleOwner, Observer {
            isLoading = false
            hasMoreData = it.posts.size == HomeViewModel.PROFILE_POSTS_COUNT
            nextPageNumber = it.pageNumber + 1
            mPostsAdapter.notifyDataSetChanged()
        })

        homeViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
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
        findNavController().navigate(
            ProfileFragmentDirections.actionNavigationProfileToUnFollowDialog(
                profileUrl = mUserDetails?.photoUrl,
                username = mUserDetails?.username ?: "",
                userId = mUserId,
                isFrom = TAG
            )
        )
    }


    override fun doubleClick() {
        fragment_profile_scroll_view.smoothScrollTo(0, 0)
    }

    private val mListener = object : ProfilePostClickListener {
        override fun onPostClick(postData: PostData, postImageView: ImageView, position: Int) {
            val action =
                ProfileFragmentDirections.actionNavigationProfileToPostViewFragment(position)

//            val extras = FragmentNavigatorExtras(
//                postImageView to postData.postId
//            )
//            findNavController().navigate(action, extras)
            findNavController().navigate(action)
        }
    }

}