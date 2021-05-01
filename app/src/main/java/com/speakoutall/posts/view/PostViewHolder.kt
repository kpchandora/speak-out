package com.speakoutall.posts.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.extensions.*
import com.speakoutall.posts.create.PostData
import kotlinx.android.synthetic.main.item_post_layout.view.*

class PostViewHolder(val view: View, private val mEventListener: PostClickEventListener?) :
    RecyclerView.ViewHolder(view) {

    init {
        view.apply {
            item_post_like_cb.setOnClickListener {
                val post = tag as PostData
                if (item_post_like_cb.isChecked) {
                    post.isLikedBySelf = true
                    post.likesCount++
                    mEventListener?.onLike(adapterPosition, post)
                } else {
                    post.isLikedBySelf = false
                    post.likesCount--
                    mEventListener?.onRemoveLike(adapterPosition, post)
                }
                setLikes(post)
            }

            item_bookmark_cb.setOnClickListener {
                val post = tag as PostData
                if (item_bookmark_cb.isChecked) {
                    lottie_bookmark.visible()
                    lottie_bookmark.playAnimation()
                    post.isBookmarkedBySelf = true
                    mEventListener?.onBookmarkAdd(post)
                } else {
                    lottie_bookmark.gone()
                    post.isBookmarkedBySelf = false
                    mEventListener?.onBookmarkRemove(postId = post.postId)
                }
            }

            item_post_layout_menu_tv.setOnClickListener {
                val post = tag as PostData
                mEventListener?.onMenuClick(post, post_container)
            }

//            item_post_load_fail_tv.setOnClickListener {
//                val post = tag as PostData
//                loadPost(post.postImageUrl)
//            }

            item_post_names_layout.setOnClickListener {
                val post = tag as PostData
                mEventListener?.onProfileClick(post, item_post_profile_iv)
            }

            item_post_profile_iv.setOnClickListener {
                val post = tag as PostData
                mEventListener?.onProfileClick(post, item_post_profile_iv)
            }

            item_post_like_count_tv.setOnClickListener {
                val post = tag as PostData
                mEventListener?.onLikedUsersClick(post)
            }
        }
    }

    fun bind(post: PostData) {
        view.apply {
            tag = post
            item_post_profile_iv.transitionName = post.postId

            item_post_profile_iv.loadImage(
                url = post.photoUrl,
                placeholder = R.drawable.ic_account_circle_grey,
                makeRound = true
            )
            item_post_name_tv.text = post.username

            setLikes(post)

            post_content_tv.text = post.content

            item_post_time_tv.text = post.timeStamp.toElapsedTime()

//            loadPost(post.postImageUrl)

            item_post_like_cb.isChecked = post.isLikedBySelf
            item_bookmark_cb.isChecked = post.isBookmarkedBySelf
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

//    @SuppressLint("CheckResult")
//    private fun loadPost(url: String) {
//        view.item_post_image_iv.loadImageWithCallback(url,
//            onSuccess = {
//                view.item_post_load_fail_tv.gone()
//            },
//            onFailed = {
//                view.item_post_load_fail_tv.visible()
//            })
//    }


}
