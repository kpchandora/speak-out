package com.speakout.ui.home

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.loadImageWithCallback
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_home_post_layout.view.*
import timber.log.Timber

class HomePostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    var mEventListener: PostClickEventListener? = null
    var userId = ""

    fun bind(post: PostData) {
        view.apply {
            item_home_post_profile_bg_iv.gone()
            item_home_post_profile_iv.transitionName = post.postId
            item_home_post_profile_iv.loadImageWithCallback(post.userImageUrl,
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

            item_home_post_name_tv.text = post.username

            setLikes(post)

            item_home_post_time_tv.text = post.timeStamp

            loadPost(post.postImageUrl)

            item_home_post_like_cb.isChecked = post.likesSet.contains(userId)

            item_home_post_like_cb.setOnClickListener {
                if (item_home_post_like_cb.isChecked) {
                    post.likesSet.add(userId)
                    mEventListener?.onLike(adapterPosition, post)
                } else {
                    post.likesSet.remove(userId)
                    mEventListener?.onDislike(adapterPosition, post)
                }
                setLikes(post)
            }

            item_home_post_load_fail_tv.setOnClickListener {
                loadPost(post.postImageUrl)
            }

            item_home_post_details_ll.setOnClickListener {
                mEventListener?.onProfileClick(post, item_home_post_profile_iv)
            }

            item_home_post_like_count_tv.setOnClickListener {
                mEventListener?.onLikedUsersClick(post)
            }
        }
    }

    private fun setLikes(post: PostData) {
        if (post.likesCount < 1) {
            view.item_home_post_like_count_tv.gone()
        } else {
            view.item_home_post_like_count_tv.visible()
            view.item_home_post_like_count_tv.text = post.likesCount.toString()
        }
    }

    @SuppressLint("CheckResult")
    private fun loadPost(url: String) {
        view.item_home_post_image_iv.loadImageWithCallback(url,
            onSuccess = {
                view.item_home_post_load_fail_tv.gone()
            }, onFailed = {
                view.item_home_post_load_fail_tv.visible()
            })
    }

}
