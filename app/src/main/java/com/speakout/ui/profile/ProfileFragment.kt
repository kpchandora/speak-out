package com.speakout.ui.profile

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.*
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.layout_profile.*

class ProfileFragment : Fragment(), UnFollowDialog.OnUnFollowClickListener {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val mPostsAdapter = ProfilePostsAdapter()
    private var mUserId = ""
    private var isSelf = false
    private lateinit var screenSize: DisplayMetrics
    private var mUserDetails: UserDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserId = arguments?.getString("user_id") ?: ""
        isSelf = mUserId == AppPreference.getUserId()
        profileViewModel.getPosts(mUserId)
        profileViewModel.addFFObserver(mUserId)
        if (isSelf) {
            profileViewModel.addProfileObserver()
        } else {
            profileViewModel.isFollowing(mUserId)
            profileViewModel.getUser(mUserId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        screenSize = activity!!.getScreenSize()

        if (isSelf) {
            initSelf()
        } else {
            initOther()
        }

        profile_post_rv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mPostsAdapter
        }


        profileViewModel.posts.observe(viewLifecycleOwner, Observer {
            mPostsAdapter.updateData(it)
        })

        profileViewModel.followersFollowingsObserver.observe(viewLifecycleOwner, Observer {
            layout_profile_followers_count_tv.text = it?.followersCount?.toString() ?: "0"
            layout_profile_followers_tv.text =
                resources.getQuantityString(
                    R.plurals.number_of_followers,
                    it?.followersCount?.toInt() ?: 0
                )

            layout_profile_followings_count_tv.text = it?.followingsCount?.toString() ?: "0"
            layout_profile_followings_tv.text =
                resources.getQuantityString(
                    R.plurals.number_of_followings,
                    it?.followingsCount?.toInt() ?: 0
                )
        })

    }

    private fun initSelf() {

        layout_profile_follow_unfollow_frame.apply {
            layoutParams.width = screenSize.widthPixels / 2
            setOnClickListener {
                activity!!.openActivity(ProfileEditActivity::class.java)
//                showUnFollowAlertDialog()
            }
        }

        showEdit()

        profileViewModel.profileObserver.observe(viewLifecycleOwner, Observer {
            it?.apply {
                populateData(this)
                if (lastUpdated ?: -1 > AppPreference.getLastUpdatedTime()) {
                    AppPreference.updateDataChangeTimeStamp(lastUpdated ?: -1)
                    AppPreference.saveUserDetails(this)
                }
            } ?: kotlin.run {
                AppPreference.apply {
                    val userDetails = UserDetails(
                        name = getUserDisplayName(),
                        username = getUserUniqueName(),
                        photoUrl = getPhotoUrl()
                    )
                    populateData(userDetails)
                }
            }
        })
    }


    private fun initOther() {
        layout_profile_follow_unfollow_frame.apply {
            layoutParams.width = screenSize.widthPixels / 2
            setBackgroundResource(R.drawable.dr_follow_bg)

            setOnClickListener {
                val text = follow_unfollow_tv.text
                if (text == getString(R.string.follow)) {
                    showFollowing()
                    profileViewModel.followUser(UserMiniDetails(userId = mUserId))
                } else if (text == getString(R.string.following)) {
                    showUnFollowAlertDialog()
                }
            }
        }

        follow_unfollow_tv.text = getString(R.string.follow)
        follow_unfollow_tv.setTextColor(ContextCompat.getColor(context!!, R.color.white))

        profileViewModel.userDetails.observe(viewLifecycleOwner, Observer { userDetails ->
            userDetails?.let {
                populateData(it)
            }
        })

        profileViewModel.followUser.observe(viewLifecycleOwner, Observer {
            if (!it) {
                showFollow()
                activity!!.showShortToast("Failed to follow user")
            }
        })

        profileViewModel.unFollowUser.observe(viewLifecycleOwner, Observer {
            if (!it) {
                showFollowing()
                activity!!.showShortToast("Failed to remove user")
            }
        })

        profileViewModel.isFollowing.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    showFollowing()
                } else {
                    showFollow()
                }
            } ?: activity!!.showShortToast("Failed to load data")

        })
    }

    private fun showFollow() {
        layout_profile_follow_unfollow_frame.visible()
        layout_profile_follow_unfollow_frame.setBackgroundResource(R.drawable.dr_follow_bg)
        follow_unfollow_tv.text = getString(R.string.follow)
        follow_unfollow_tv.setTextColor(ContextCompat.getColor(context!!, R.color.white))
    }

    private fun showFollowing() {
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
        layout_profile_iv.layoutParams.width = screenSize.widthPixels / 3
        layout_profile_bg_view.layoutParams.width = screenSize.widthPixels / 3
        mUserDetails = userDetails
        layout_profile_bg_view.gone()
        layout_profile_iv.loadImageWithCallback(userDetails.photoUrl ?: "", makeRound = true,
            onSuccess = {
                layout_profile_bg_view.visible()
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


        layout_profile_full_name_tv.text = userDetails.name

        layout_profile_posts_count_tv.text = userDetails.postsCount.toString()
        layout_profile_posts_tv.text =
            resources.getQuantityString(R.plurals.number_of_posts, userDetails.postsCount.toInt())
    }

    private fun showUnFollowAlertDialog() {
        val bundle = Bundle().also {
            it.putParcelable(UnFollowDialog.USER_DETAILS, mUserDetails)
        }
        val dialog = UnFollowDialog.newInstance(bundle)
        dialog.setListener(this)
        dialog.show(requireActivity().supportFragmentManager, "")

    }

    override fun onUnFollow(userId: String) {
        showFollow()
        profileViewModel.unFollowUser(UserMiniDetails(userId = userId))
    }

}