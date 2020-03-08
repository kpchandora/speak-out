package com.speakout.posts.create

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.speakout.R
import com.speakout.extensions.addFragment
import com.speakout.extensions.showShortToast
import com.speakout.posts.tags.PostTagsFragment
import com.speakout.ui.BottomDialogActivity
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.ImageUtils
import com.speakout.utils.NameUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_new_post.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class CreateNewPostActivity : AppCompatActivity() {

    private val createPostData = CreatePostData()
    private val mCreatePostViewModel: CreatePostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_post)


        create_post_bg_iv.setOnClickListener {
            startActivityForResult(
                Intent(this, BottomDialogActivity::class.java).putExtra(
                    BottomDialogActivity.CONTENT, create_post_content_tv.text
                ), NameUtils.IntentStrings.CreatePost.REQUEST_CODE
            )
        }


        create_post_next_btn.setOnClickListener {
            addFragment(
                container = R.id.create_post_container_main,
                backStackTag = PostTagsFragment.TAG,
                fragment = PostTagsFragment.newInstance()
            )
        }

        observeLiveData()

    }

    private fun observeLiveData() {
        mCreatePostViewModel.tags.observe(this, Observer {
            createPostData.tags = it
            ImageUtils.convertToBitmap(create_post_container_layout)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bitmap ->
                    bitmap?.let {
                        val postId = UUID.randomUUID().toString()
                        createPostData.postId = postId
                        mCreatePostViewModel.uploadImage(Pair(bitmap, postId))
                    } ?: showShortToast("Failed to upload post")
                }) { t ->
                    showShortToast("Failed to upload post: $t")
                }

        })

        mCreatePostViewModel.uploadImageObserver.observe(this, Observer {
            it?.let { url ->
                Timber.d("Image Url: $url")
                createPostData.apply {
                    postImageUrl = url
                    content = create_post_content_tv.text.toString()
                    timeStamp = DateFormat.getDateTimeInstance().format(Date())
                    userId = FirebaseUtils.userId() ?: ""
                }

                mCreatePostViewModel.uploadPost(createPostData)

            } ?: showShortToast("Failed to upload post")
        })

        mCreatePostViewModel.postObserver.observe(this, Observer {
            if (it) {
                showShortToast("Post uploaded successfully")
                finish()
            } else {
                showShortToast("Failed to upload post")
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NameUtils.IntentStrings.CreatePost.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let {
                    it.getString(BottomDialogActivity.CONTENT)?.let { content ->
                        create_post_content_tv.text = content
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
