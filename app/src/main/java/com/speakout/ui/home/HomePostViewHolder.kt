package com.speakout.ui.home

import android.animation.ValueAnimator
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.speakout.R
import com.speakout.extensions.loadImage
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_home_post_layout.view.*
import timber.log.Timber
import kotlin.random.Random

class HomePostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(post: PostData) {
        view.apply {
            item_home_post_profile_iv.loadImage(null, R.drawable.ic_profile_placeholder)
            item_home_post_like_count_tv.text = Random.nextInt(100, 500).toString()


            Glide.with(view)
                .load(post.postImageUrl)
                .thumbnail(.1f)
                .into(item_home_post_image_iv)

            item_home_post_time_tv.text = post.timeStamp
            item_home_post_name_tv.text = post.postId.substring(0, 10)

        }
    }

}
