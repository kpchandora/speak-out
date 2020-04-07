package com.speakout.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.gone
import com.speakout.extensions.loadImageWithCallback
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
import com.speakout.ui.home.HomeFragmentDirections
import com.speakout.users.ActionType
import kotlinx.android.synthetic.main.item_home_post_layout.*
import kotlinx.android.synthetic.main.item_home_post_layout.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


class PostViewFragment : Fragment() {

    private val safeArgs: PostViewFragmentArgs by navArgs()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("hashCode: $profileViewModel")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screenSize = requireActivity().getScreenSize()
        item_home_post_image_iv.layoutParams.height = screenSize.widthPixels
        safeArgs.postData?.apply {
            item_home_post_profile_bg_iv.gone()
            item_home_post_profile_iv.loadImageWithCallback(userImageUrl,
                makeRound = true,
                onSuccess = {
                    item_home_post_profile_bg_iv.visible()
                },
                onFailed = {
                    item_home_post_profile_iv.setImageDrawable(
                        ContextCompat.getDrawable(
                            view.context,
                            R.drawable.ic_account_circle_grey
                        )
                    )
                    item_home_post_profile_bg_iv.gone()
                })
            item_home_post_name_tv.text = username
            item_home_post_time_tv.text = timeStamp

            item_home_post_load_fail_tv.setOnClickListener {
                loadPost(postImageUrl)
            }

            item_home_post_like_count_tv.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToUsersListFragment(
                        userId = userId,
                        actionType = ActionType.Likes
                    )
                )
            }

            loadPost(postImageUrl)
            setLikes(this)
        }
    }

    private fun setLikes(post: PostData) {
        if (post.likesCount < 1) {
            item_home_post_like_count_tv.gone()
        } else {
            item_home_post_like_count_tv.visible()
            item_home_post_like_count_tv.text = post.likesCount.toString()
        }
    }

    @SuppressLint("CheckResult")
    private fun loadPost(url: String) {
        item_home_post_image_iv.loadImageWithCallback(url,
            onSuccess = {
                item_home_post_load_fail_tv.gone()
            },
            onFailed = {
                item_home_post_load_fail_tv.visible()
            })
    }

}
