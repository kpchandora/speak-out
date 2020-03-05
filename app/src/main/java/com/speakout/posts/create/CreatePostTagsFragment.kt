package com.speakout.posts.create


import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

import com.speakout.R
import com.speakout.utils.ImageUtils
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_new_post.*
import kotlinx.android.synthetic.main.fragment_create_post_tags.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class CreatePostTagsFragment : Fragment() {

    companion object {

        const val TAG = "CreatePostTagsFragment"
        const val POST_DATA_KEY = "post_data_key"

        fun newInstance(bundle: Bundle? = null) = CreatePostTagsFragment().apply {
            arguments = bundle
        }
    }

    private var createPostData: CreatePostData? = null
    private val mCreatePostViewModel: CreatePostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tag_done_fab.setOnClickListener {
            mCreatePostViewModel.tags.value = listOf("firstpost", "motivation")
        }
    }
}
