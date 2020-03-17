package com.speakout.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.extensions.loadImage
import com.speakout.ui.home.HomeViewModel
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.layout_profile.*
import timber.log.Timber
import kotlin.random.Random

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val home: HomeViewModel by activityViewModels()
    private val mPostsAdapter = ProfilePostsAdapter()
    private var mUserId = ""
    private var isSelf = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserId = arguments?.getString("user_id") ?: ""
        isSelf = mUserId == AppPreference.getUserId()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
        
        home.getPosts("")
        home.posts.observe(viewLifecycleOwner, Observer {
            mPostsAdapter.updateData(it)
        })
    }

    private fun initSelf() {
        profileViewModel.profileObserver.observe(viewLifecycleOwner, Observer {
            it?.apply {
                populateData(this)
                if (lastUpdated ?: -1 > AppPreference.getLastUpdatedTime()) {
                    AppPreference.updateDataChangeTimeStamp(lastSignInTimestamp ?: -1)
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
        profileViewModel.userDetails.observe(viewLifecycleOwner, Observer { userDetails ->
            userDetails?.let {
                populateData(it)
            }
        })
    }

    private fun populateData(userDetails: UserDetails) {
        layout_profile_iv.loadImage(
            userDetails.photoUrl,
            R.drawable.ic_profile_placeholder,
            true
        )

        layout_profile_full_name_tv.text = userDetails.name

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

        layout_profile_posts_count_tv.text = userDetails.postsCount.toString()
        layout_profile_posts_tv.text =
            resources.getQuantityString(R.plurals.number_of_posts, userDetails.postsCount.toInt())
    }

}