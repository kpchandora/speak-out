package com.speakout.posts.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.speakout.R
import com.speakout.extensions.addFragment
import com.speakout.extensions.addViewObserver
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.showShortToast
import com.speakout.posts.tags.TagsFragment
import com.speakout.ui.BaseActivity
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

class CreateNewPostActivity : BaseActivity() {

    private val createPostData = PostData()
    private val mCreatePostViewModel: CreatePostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_post)

        create_post_bg_iv.addViewObserver {
            getScreenSize().let {
                create_post_bg_iv.layoutParams.height = it.widthPixels
                create_post_bg_iv.requestLayout()
            }
        }
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
                backStackTag = TagsFragment.TAG,
                fragment = TagsFragment.newInstance()
            )
        }

        observeLiveData()

    }

    private fun observeLiveData() {
        mCreatePostViewModel.tags.observe(this, Observer {
            createPostData.tags = it
            showProgress()
            ImageUtils.convertToBitmap(create_post_container_layout)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bitmap ->
                    bitmap?.let {
                        val postId = UUID.randomUUID().toString()
                        createPostData.postId = postId
                        mCreatePostViewModel.uploadImage(Pair(bitmap, postId))
                    } ?: kotlin.run {
                        hideProgress()
                        showShortToast("Failed to upload post")
                    }
                }) { t ->
                    hideProgress()
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

            } ?: kotlin.run {
                hideProgress()
                showShortToast("Failed to upload post")
            }
        })

        mCreatePostViewModel.postObserver.observe(this, Observer {
            if (it) {
                showShortToast("Post uploaded successfully")
                finish()
            } else {
                hideProgress()
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
