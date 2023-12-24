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
import com.speakoutall.databinding.FragmentProfileBinding
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
    private var _binding: FragmentProfileBinding? = null

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
        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        _binding = binding
        setUpWithAppBarConfiguration(binding.root)?.let {
            _binding?.toolbarContainer?.run {
                if (isSelf) {
                    ivSettings.visible()
                    toolbarTitle.text = AppPreference.getUserUniqueName()
                } else {
                    toolbarTitle.text = safeArgs.username
                    ivSettings.gone()
                }
            }
        }
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        safeArgs.transitionTag?.let {
            _binding?.profileLayoutContainer?.layoutProfileIv?.transitionName = it
            loadImage(safeArgs.profileUrl)
        }

        _binding?.toolbarContainer?.ivSettings?.setOnClickListener {
            val action =
                ProfileFragmentDirections.actionNavigationProfileToProfileOptionsBottomSheetFragment()
            findNavController().navigate(action)
        }

        _binding?.run {
            swipeProfile.isRefreshing = true
            swipeProfile.setOnRefreshListener {
                profileLayoutContainer.viewEmptyProfilePosts.gone()
                key = 0
                homeViewModel.mPostList.clear()
                mPostsAdapter.notifyDataSetChanged()
                homeViewModel.getProfilePosts(mUserId, key)
                profileViewModel.getUser(mUserId)
            }
            profileLayoutContainer.profilePostRv.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, 3)
                adapter = mPostsAdapter
            }
            profileLayoutContainer.profilePostRv.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
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
        }

        mPostsAdapter.mListener = mListener

        profileViewModel.userDetails.observe(viewLifecycleOwner, Observer { result ->
            if (result is Result.Success) {
                populateData(result.data)
            }
        })

        homeViewModel.posts.observe(viewLifecycleOwner, Observer {
            _binding?.swipeProfile?.isRefreshing = false
            isLoading = false
            key = it.key
            if (homeViewModel.mPostList.isEmpty()) {
                _binding?.profileLayoutContainer?.viewEmptyProfilePosts?.visible()
            } else {
                _binding?.profileLayoutContainer?.viewEmptyProfilePosts?.gone()
            }
            mPostsAdapter.notifyDataSetChanged()
        })

        homeViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
            _binding?.swipeProfile?.isRefreshing = false
            isLoading = false
            showShortToast(it)
        })

        _binding?.profileLayoutContainer?.layoutProfileFollowersContainer?.setOnClickListener {
            navigateToUsersList(ActionType.Followers)
        }

        _binding?.profileLayoutContainer?.layoutProfileFollowingsContainer?.setOnClickListener {
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
        _binding?.profileLayoutContainer?.layoutProfileFollowUnfollowFrame?.run {
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
        _binding?.profileLayoutContainer?.run {
            layoutProfileFollowUnfollowFrame.run {
                layoutParams.width = screenSize.widthPixels / 2
                setOnClickListener {
                    val text = followUnfollowTv.text
                    if (text == getString(R.string.follow)) {
                        layoutProfileFollowUnfollowFrame.invisible()
                        followUnfollowProgress.visible()
                        profileViewModel.followUser(userDetails.userId)
                    } else if (text == getString(R.string.following)) {
                        showUnFollowAlertDialog()
                    }
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
                profileViewModel.getUser(mUserId)
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
                profileViewModel.getUser(mUserId)
            } else {
                mUserDetails?.let { userDetails ->
                    populateFollowersAndFollowings(userDetails)
                }
                requireActivity().showShortToast("Failed to unfollow user")
            }
        })

        profileViewModel.confirmUnfollow.observe(viewLifecycleOwner, EventObserver {
            _binding?.profileLayoutContainer?.layoutProfileFollowUnfollowFrame?.invisible()
            _binding?.profileLayoutContainer?.followUnfollowProgress?.visible()
            profileViewModel.unFollowUser(userDetails.userId)
        })

    }

    private fun showFollow() {
        _binding?.profileLayoutContainer?.run {
            layoutProfileFollowUnfollowFrame.visible()
            layoutProfileFollowUnfollowFrame.setBackgroundResource(R.drawable.dr_follow_bg)
            followUnfollowProgress.gone()
            followUnfollowTv.text = getString(R.string.follow)
            followUnfollowTv.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.colorFollowBgText
                )
            )
        }
    }

    private fun showFollowing() {
        _binding?.profileLayoutContainer?.run {
            layoutProfileFollowUnfollowFrame.visible()
            layoutProfileFollowUnfollowFrame.setBackgroundResource(R.drawable.dr_unfollow_bg)
            followUnfollowProgress.gone()
            followUnfollowTv.text = getString(R.string.following)
            followUnfollowTv.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.colorFollowingBgText
                )
            )
        }
    }

    private fun showEdit() {
        showFollowing()
        _binding?.profileLayoutContainer?.followUnfollowTv?.text = getString(R.string.edit)
    }

    private fun populateData(userDetails: UserDetails) {
        mUserDetails = userDetails

        loadImage(userDetails.photoUrl)

        _binding?.profileLayoutContainer?.run {
            layoutProfileFullNameTv.text = userDetails.name
            layoutProfilePostsCountTv.text =
                if (userDetails.postsCount > 0) userDetails.postsCount.toString() else "0"
            layoutProfilePostsTv.text = resources.getQuantityString(
                R.plurals.number_of_posts,
                userDetails.postsCount.toInt()
            )
        }

        populateFollowersAndFollowings(userDetails)

        if (isSelf) {
            initSelf()
        } else {
            initOther(userDetails)
        }

    }

    private fun populateFollowersAndFollowings(userDetails: UserDetails) {
        _binding?.profileLayoutContainer?.run {
            layoutProfileFollowersCountTv.text = userDetails.followersCount.toString()
            layoutProfileFollowersTv.text = resources.getQuantityString(
                R.plurals.number_of_followers,
                userDetails.followersCount.toInt()
            )
            layoutProfileFollowingsCountTv.text = userDetails.followingsCount.toString()
            layoutProfileFollowingsTv.text = resources.getQuantityString(
                R.plurals.number_of_followings,
                userDetails.followingsCount.toInt()
            )
            if (userDetails.followersCount <= 0L) {
                layoutProfileFollowersContainer.disable()
            } else {
                layoutProfileFollowersContainer.enable()
            }

            if (userDetails.followingsCount <= 0L) {
                layoutProfileFollowingsContainer.disable()
            } else {
                layoutProfileFollowingsContainer.enable()
            }
        }

        if (userDetails.isFollowedBySelf) {
            showFollowing()
        } else {
            showFollow()
        }

    }

    private fun loadImage(photoUrl: String?) {
        _binding?.profileLayoutContainer?.run {
            cardView.layoutParams.width = screenSize.widthPixels / 3
            layoutProfileBgView.layoutParams.width = screenSize.widthPixels / 3
            layoutProfileBgView.gone()
            layoutProfileIv.loadImageWithCallback(photoUrl ?: "", centerCrop = true,
                onSuccess = {
                },
                onFailed = {
                    layoutProfileBgView.gone()
                    layoutProfileIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.ic_account_circle_grey
                        )
                    )
                })
        }
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
        _binding?.fragmentProfileScrollView?.smoothScrollTo(0, 0)
    }

    private val mListener = object : ProfilePostClickListener {
        override fun onPostClick(postData: PostData, postImageView: ImageView, position: Int) {
            val action =
                ProfileFragmentDirections.actionNavigationProfileToPostViewFragment(position)
            findNavController().navigate(action)
        }
    }

}