package com.speakout.ui.home

import android.annotation.SuppressLint
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.loadImageWithCallback
import com.speakout.extensions.showShortToast
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_post_layout.view.*

class HomePostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    var mEventListener: PostClickEventListener? = null
    var userId = ""

    fun bind(post: PostData) {
        view.apply {
            item_post_profile_bg_iv.gone()
            item_post_profile_iv.transitionName = post.postId
            item_post_profile_iv.loadImageWithCallback(post.userImageUrl,
                makeRound = true,
                onSuccess = {
                    item_post_profile_bg_iv.visible()
                },
                onFailed = {
                    item_post_profile_iv.setImageDrawable(
                        ContextCompat.getDrawable(
                            view.context,
                            R.drawable.ic_account_circle_grey
                        )
                    )
                    item_post_profile_bg_iv.gone()
                })

            item_post_name_tv.text = post.username

            setLikes(post)

            item_post_time_tv.text = post.timeStamp

            loadPost(post.postImageUrl)

            item_post_like_cb.isChecked = post.likesSet.contains(userId)

            item_post_like_cb.setOnClickListener {
                if (item_post_like_cb.isChecked) {
                    post.likesSet.add(userId)
                    mEventListener?.onLike(adapterPosition, post)
                } else {
                    post.likesSet.remove(userId)
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
