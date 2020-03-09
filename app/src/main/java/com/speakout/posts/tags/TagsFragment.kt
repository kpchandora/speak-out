package com.speakout.posts.tags


import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.visible
import com.speakout.posts.create.CreatePostViewModel
import kotlinx.android.synthetic.main.fragment_post_tags.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class TagsFragment : Fragment() {

    companion object {

        const val TAG = "TagsFragment"

        fun newInstance(bundle: Bundle? = null) = TagsFragment().apply {
            arguments = bundle
        }
    }

    private val mCreatePostViewModel: CreatePostViewModel by activityViewModels()
    private val mSelectedTags = hashMapOf<String, Tag>()
    private val mAdapter = TagsRecyclerViewAdapter()
    private val mSelectedTagsAdapter = SelectedTagsRecyclerViewAdapter()
    private val addTagQueue = LinkedList<Tag>()
    private val tagsViewModel: TagViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_post_tags_progress.visible()
        mAdapter.setHasStableIds(true)
        mAdapter.setListener(tagsListener)

        mSelectedTagsAdapter.setHasStableIds(true)
        mSelectedTagsAdapter.setListener(selectedTagsListener)

        tags_rv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        selected_tags_rv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mSelectedTagsAdapter
        }

        observeViewModels()


        tag_done_fab.setOnClickListener {
            mCreatePostViewModel.tags.value = mSelectedTags.keys.toList()
        }

        Handler().postDelayed({
            mAdapter.isLoading.set(true)
            tagsViewModel.searchTags("")
        }, 500)

        tag_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mAdapter.isLoading.set(true)
                tagsViewModel.searchTags(newText ?: "")
                return true
            }

        })

    }

    private fun observeViewModels() {
        tagsViewModel.tags.observe(viewLifecycleOwner, Observer {
            fragment_post_tags_progress.gone()

            if (tag_search_view.query?.isNotEmpty() == true && it.isEmpty()) {
                mAdapter.setData(
                    listOf(
                        Tag(
                            tag = tag_search_view.query.toString(),
                            id = System.nanoTime(),
                            used = null
                        )
                    )
                )
            } else {
                mAdapter.setData(it)
            }
            mAdapter.isLoading.set(false)
        })

        tagsViewModel.addTag.observe(viewLifecycleOwner, Observer {
            if (addTagQueue.isNotEmpty()) {
                addTagQueue.poll()?.let {
                    it.uploading = null
                    mAdapter.tagAdded(tag = it)
                }
            }
        })

    }


    private val tagsListener = object : OnTagClickListener {
        override fun onTagClick(tag: Tag) {
            if (mSelectedTags.contains(tag.tag)) {
                mSelectedTags.remove(tag.tag)
                mSelectedTagsAdapter.removeTag(tag)
            } else {
                mSelectedTags[tag.tag] = tag
                mSelectedTagsAdapter.addTag(tag)
            }
            if (mSelectedTags.isEmpty()) {
                selected_tags_rv.gone()
            } else {
                selected_tags_rv.visible()
                selected_tags_rv.scrollToPosition(0)
            }
        }

        override fun onAddNewTag(tag: Tag) {
            super.onAddNewTag(tag)
            val newTag = tag.copy(used = 0)
            addTagQueue.add(newTag)
            tagsViewModel.addTag(newTag)
        }

    }

    private val selectedTagsListener = object : OnTagClickListener {
        override fun onTagClick(tag: Tag) {
            mSelectedTags.remove(tag.tag)
            if (mSelectedTags.isEmpty()) {
                selected_tags_rv.gone()
            }
            mAdapter.removeTag(tag)
        }
    }

}
