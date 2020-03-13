package com.speakout.ui.home

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.loadImage
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_home_post_layout.view.*
import timber.log.Timber
import kotlin.random.Random

class HomePostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(post: PostData) {
        view.apply {
            item_home_post_profile_iv.loadImage(
                url = post.userImageUrl,
                placeholder = R.drawable.ic_profile_placeholder,
                makeRound = true
            )
            item_home_post_like_count_tv.text = post.likesCount.toString()
            item_home_post_name_tv.text = post.username


            item_home_post_time_tv.text = post.timeStamp

            loadPost(post.postImageUrl)

            item_home_post_load_fail_tv.setOnClickListener {
                loadPost(post.postImageUrl)
            }

        }
    }

    @SuppressLint("CheckResult")
    private fun loadPost(url: String) {
        Glide.with(view)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    view.item_home_post_load_fail_tv.visible()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    view.item_home_post_load_fail_tv.gone()
                    return false
                }
            })
            .thumbnail(.1f)
            .into(view.item_home_post_image_iv)
    }

}
