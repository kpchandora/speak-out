package com.speakout.ui.profile

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.speakout.R
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import com.speakout.extensions.openActivity
import com.speakout.ui.home.HomeViewModel
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.layout_profile.*
import timber.log.Timber

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val home: HomeViewModel by activityViewModels()
    private val mPostsAdapter = ProfilePostsAdapter()
    private var mUserId = ""
    private var isSelf = false
    private lateinit var screenSize: DisplayMetrics

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

        screenSize = activity!!.getScreenSize()

        if (isSelf) {
            initSelf()
        } else {
            initOther()
        }

        profileViewModel.followUser.observe(viewLifecycleOwner, Observer {
            Timber.d("Follow User: $it")
        })

//        layout_profile_follow_unfollow_btn.setOnClickListener {
//            profileViewModel.followUser(UserMiniDetails(userId = "DPi4YJlKRdasfa4gaj6BndFesSg1"))
//        }

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
        layout_profile_follow_unfollow_btn.apply {
            layoutParams.width = screenSize.widthPixels / 2
            setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
            setTextColor(ContextCompat.getColor(context!!, R.color.black))
            text = resources.getString(R.string.edit)
            setOnClickListener {
                activity!!.openActivity(ProfileEditActivity::class.java)
            }
        }
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
        layout_profile_follow_unfollow_btn.apply {
            layoutParams.width = screenSize.widthPixels / 2
            setBackgroundColor(ContextCompat.getColor(context!!, R.color.indigo_500))
            setTextColor(ContextCompat.getColor(context!!, R.color.white))
        }
        profileViewModel.getUser(mUserId)
        profileViewModel.userDetails.observe(viewLifecycleOwner, Observer { userDetails ->
            userDetails?.let {
                populateData(it)
            }
        })
    }

    private fun populateData(userDetails: UserDetails) {
        layout_profile_iv.layoutParams.width = screenSize.widthPixels / 3
        layout_profile_bg_view.layoutParams.width = screenSize.widthPixels / 3
//        layout_profile_iv.loadImage(
//            userDetails.photoUrl,
//            R.drawable.ic_profile_placeholder,
//            true
//        )

        Glide.with(this).asBitmap().load(userDetails.photoUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Timber.d("Image Bitmap: $resource")
                    layout_profile_iv.setImageBitmap(resource)
                }

            })


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