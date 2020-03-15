package com.speakout.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import com.speakout.R
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import java.lang.Exception
import java.util.*

class HomeFragment : Fragment() {

    private val mHomeViewModel: HomeViewModel by activityViewModels()
    private val mPostsAdapter = HomePostRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPostsAdapter.mEventListener = mPostEventsListener
        fragment_home_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter
        }

        observeViewModels()
        mHomeViewModel.getPosts("")

    }

    override fun onDestroyView() {
        mPostsAdapter.mEventListener = null
        super.onDestroyView()
    }

    private fun observeViewModels() {
        mHomeViewModel.posts.observe(viewLifecycleOwner, Observer {
            mPostsAdapter.updatePosts(it)
        })

        mHomeViewModel.likePost.observe(viewLifecycleOwner, Observer { data: PostData? ->
            data?.let {

            } ?: kotlin.run {

            }
        })

        mHomeViewModel.dislike.observe(viewLifecycleOwner, Observer {

        })

    }

    private val mPostEventsListener = object : PostClickEventListener {
        override fun onLike(position: Int, postData: PostData) {
            mHomeViewModel.likePost(postData)
        }

        override fun onDislike(position: Int, postData: PostData) {
            mHomeViewModel.dislike(postData)
        }
    }

}