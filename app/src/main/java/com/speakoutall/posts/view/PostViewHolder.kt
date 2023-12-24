package com.speakoutall.posts.view

import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.databinding.ItemPostLayoutBinding
import com.speakoutall.extensions.*
import com.speakoutall.posts.create.PostData

class PostViewHolder(
    val binding: ItemPostLayoutBinding,
    private val mEventListener: PostClickEventListener?
) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.run {
            itemPostLikeCb.setOnClickListener {
                val post = root.tag as PostData
                if (itemPostLikeCb.isChecked) {
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

            itemBookmarkCb.setOnClickListener {
                val post = root.tag as PostData
                if (itemBookmarkCb.isChecked) {
                    post.isBookmarkedBySelf = true
                    mEventListener?.onBookmarkAdd(post)
                } else {
                    post.isBookmarkedBySelf = false
                    mEventListener?.onBookmarkRemove(postId = post.postId)
                }
            }

            itemPostLayoutMenuTv.setOnClickListener {
                val post = root.tag as PostData
                mEventListener?.onMenuClick(post, postContainer)
            }

//            item_post_load_fail_tv.setOnClickListener {
//                val post = tag as PostData
//                loadPost(post.postImageUrl)
//            }

            itemPostNamesLayout.setOnClickListener {
                val post = root.tag as PostData
                mEventListener?.onProfileClick(post, itemPostProfileIv)
            }

            itemPostProfileIv.setOnClickListener {
                val post = root.tag as PostData
                mEventListener?.onProfileClick(post, itemPostProfileIv)
            }

            itemPostLikeCountTv.setOnClickListener {
                val post = root.tag as PostData
                mEventListener?.onLikedUsersClick(post)
            }
        }
    }

    fun bind(post: PostData) {
        binding.apply {
            root.tag = post
            itemPostProfileIv.transitionName = post.postId

            itemPostProfileIv.loadImage(
                url = post.photoUrl,
                placeholder = R.drawable.ic_account_circle_grey,
                makeRound = true
            )
            itemPostNameTv.text = post.username

            setLikes(post)

            postContentTv.text = post.content

            itemPostTimeTv.text = post.timeStamp.toElapsedTime()

//            loadPost(post.postImageUrl)

            itemPostLikeCb.isChecked = post.isLikedBySelf
            itemBookmarkCb.isChecked = post.isBookmarkedBySelf
        }
    }

    private fun setLikes(post: PostData) {
        if (post.likesCount < 1) {
            binding.itemPostLikeCountTv.gone()
        } else {
            binding.itemPostLikeCountTv.visible()
            binding.itemPostLikeCountTv.text = post.likesCount.toString()
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
