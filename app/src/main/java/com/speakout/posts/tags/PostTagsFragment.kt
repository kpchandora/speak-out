package com.speakout.posts.tags


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels

import com.speakout.R
import com.speakout.posts.create.CreatePostData
import com.speakout.posts.create.CreatePostViewModel
import kotlinx.android.synthetic.main.fragment_post_tags.*

/**
 * A simple [Fragment] subclass.
 */
class PostTagsFragment : Fragment() {

    companion object {

        const val TAG = "PostTagsFragment"
        const val POST_DATA_KEY = "post_data_key"

        fun newInstance(bundle: Bundle? = null) = PostTagsFragment().apply {
            arguments = bundle
        }
    }

    private val mCreatePostViewModel: CreatePostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tag_done_fab.setOnClickListener {
            mCreatePostViewModel.tags.value = listOf("firstpost", "motivation")
        }
    }
}
