package com.speakout.posts.view

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.*
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_post_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class PostViewHolder(val view: View, private val simpleDateFormat: SimpleDateFormat) :
    RecyclerView.ViewHolder(view) {

    var mEventListener: PostClickEventListener? = null

    fun bind(post: PostData) {
        view.apply {
            item_post_profile_iv.transitionName = post.postId

            item_post_profile_iv.loadImage(
                url = post.photoUrl,
                placeholder = R.drawable.ic_account_circle_grey,
                makeRound = true
            )
            item_post_name_tv.text = post.username

            setLikes(post)

            item_post_time_tv.text = post.timeStamp.toElapsedTime()

            loadPost(post.postImageUrl)

            item_post_like_cb.isChecked = post.isLikedBySelf

            item_post_like_cb.setOnClickListener {
                if (item_post_like_cb.isChecked) {
                    post.isLikedBySelf = true
                    post.likesCount++
                    mEventListener?.onLike(adapterPosition, post)
                } else {
                    post.isLikedBySelf = false
                    post.likesCount--
                    mEventListener?.onDislike(adapterPosition, post)
                }
                setLikes(post)
            }

            item_post_layout_menu_tv.setOnClickListener {
                mEventListener?.onMenuClick(post, adapterPosition)
            }

            item_post_load_fail_tv.setOnClickListener {
                loadPost(post.postImageUrl)
            }

            item_post_names_layout.setOnClickListener {
                mEventListener?.onProfileClick(post, item_post_profile_iv)
            }

            item_post_profile_iv.setOnClickListener {
                mEventListener?.onProfileClick(post, item_post_profile_iv)
            }

            item_post_like_count_tv.setOnClickListener {
                mEventListener?.onLikedUsersClick(post)
            }
        }
    }

    private fun setLikes(post: PostData) {
        if (post.likesCount < 1) {
            view.item_post_like_count_tv.gone()
        } else {
            view.item_post_like_count_tv.visible()
            view.item_post_like_count_tv.text = post.likesCount.toString()
        }
    }

    @SuppressLint("CheckResult")
    private fun loadPost(url: String) {
        view.item_post_image_iv.loadImageWithCallback(url,
            onSuccess = {
                view.item_post_load_fail_tv.gone()
            },
            onFailed = {
                view.item_post_load_fail_tv.visible()
            })
    }


}
