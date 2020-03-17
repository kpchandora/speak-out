package com.speakout.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakout.R
import com.speakout.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.layout_profile.*

class ProfileFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val home: HomeViewModel by activityViewModels()
    private val mPostsAdapter = ProfilePostsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profile_post_rv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mPostsAdapter
        }

        home.getPosts("")
        home.posts.observe(viewLifecycleOwner, Observer {
            mPostsAdapter.updateData(it)
        })

    }

}