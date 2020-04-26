package com.speakout.posts.tags


import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.setUpToolbar
import com.speakout.extensions.showShortToast
import com.speakout.extensions.visible
import com.speakout.posts.create.CreatePostViewModel
import com.speakout.posts.create.PostData
import com.speakout.ui.MainActivity
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.fragment_post_tags.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class TagsFragment : Fragment() {

    private val mCreatePostViewModel: CreatePostViewModel by navGraphViewModels(R.id.create_post_navigation)
    private val mSelectedTags = hashMapOf<Long, Tag>()
    private val mAdapter = TagsRecyclerViewAdapter()
    private val mSelectedTagsAdapter = SelectedTagsRecyclerViewAdapter()
    private val addTagQueue = LinkedList<Tag>()
    private val tagsViewModel: TagViewModel by viewModels()
    private val tagRegex = "^([a-z0-9]+\\b)(?!;)\$".toRegex()
    private val createPostData = PostData()
    private val safeArgs: TagsFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            setUpToolbar(view)?.let {
                it.title = "Tags"
            }
        }
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
            createPostData.tags = mSelectedTags.keys.map {
                it.toString()
            }
            uploadPost()
        }

        Handler().postDelayed({
            mAdapter.isLoading.set(true)
            tagsViewModel.searchTags("")
        }, 500)

        val searchEditText =
            tag_search_view.findViewById(androidx.appcompat.R.id.search_src_text) as EditText


        tag_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.toLowerCase(Locale.getDefault())?.let {
                    if (tagRegex.matches(it)) {
                        searchEditText.error = null
                        mAdapter.isLoading.set(true)
                        tagsViewModel.searchTags(newText.toLowerCase(Locale.getDefault()))
                    } else {
                        if (it.isEmpty()) {
                            tagsViewModel.searchTags("")
                            searchEditText.error = null
                        } else {
                            searchEditText.error = "Invalid tag name"
                        }
                    }
                }
                return true
            }

        })

    }

    private fun uploadPost() {
        mCreatePostViewModel.imageBitmap?.let { bitmap ->
            (requireActivity() as MainActivity).showProgress()
            val postId = UUID.randomUUID().toString()
            createPostData.postId = postId
            mCreatePostViewModel.uploadImage(Pair(bitmap, postId))
        } ?: kotlin.run {
            showShortToast("Failed to upload post")
        }

        mCreatePostViewModel.uploadImageObserver.observe(viewLifecycleOwner, Observer {
            it?.let { url ->
                val pref = AppPreference

                Timber.d("Image Url: $url")
                createPostData.apply {
                    postImageUrl = url
                    content = safeArgs.postContent
                    timeStamp = DateFormat.getDateTimeInstance().format(Date())
                    userId = pref.getUserId()
                    userImageUrl = pref.getPhotoUrl()
                    username = pref.getUserUniqueName()
                    timeStampLong = System.currentTimeMillis()
                }

                mCreatePostViewModel.uploadPost(createPostData)

            } ?: kotlin.run {
                (requireActivity() as MainActivity).hideProgress()
                showShortToast("Failed to upload post")
            }
        })

        mCreatePostViewModel.postObserver.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).hideProgress()
            if (it) {
                showShortToast("Post uploaded successfully")
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "isSuccess",
                    true
                )
                findNavController().navigateUp()
            } else {
                showShortToast("Failed to upload post")
            }
        })

    }

    private fun observeViewModels() {
        tagsViewModel.tags.observe(viewLifecycleOwner, Observer {
            fragment_post_tags_progress.gone()

            val query = tag_search_view.query
            if (query?.isNotEmpty() == true && it.isEmpty()) {
                if (tagRegex.matches(query)) {
                    mAdapter.setData(
                        listOf(
                            Tag(tag = query.toString(), id = System.nanoTime(), used = null)
                        )
                    )
                }
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
            if (mSelectedTags.contains(tag.id)) {
                mSelectedTags.remove(tag.id)
                mSelectedTagsAdapter.removeTag(tag)
            } else {
                mSelectedTags[tag.id] = tag
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
            mSelectedTags.remove(tag.id)
            if (mSelectedTags.isEmpty()) {
                selected_tags_rv.gone()
            }
            mAdapter.removeTag(tag)
        }
    }

}
