package com.speakout.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.speakout.R
import com.speakout.extensions.loadImage
import com.speakout.ui.home.HomeViewModel
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.layout_profile.*
import kotlin.random.Random

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

        layout_profile_iv.loadImage(
            AppPreference.getPhotoUrl(),
            R.drawable.ic_profile_placeholder,
            true
        )

        layout_profile_full_name_tv.text = AppPreference.getUserDisplayName()

        layout_profile_followers_count_tv.text = Random.nextInt(100, 400).toString()
        layout_profile_followers_tv.text =
            resources.getQuantityString(R.plurals.number_of_followers, 0)

        layout_profile_followings_count_tv.text = Random.nextInt(100, 200).toString()
        layout_profile_followings_tv.text =
            resources.getQuantityString(R.plurals.number_of_followings, 2)

        home.getPosts("")
        home.posts.observe(viewLifecycleOwner, Observer {
            layout_profile_posts_count_tv.text = it.size.toString()
            layout_profile_posts_tv.text =
                resources.getQuantityString(R.plurals.number_of_posts, it.size)
            mPostsAdapter.updateData(it)
        })
    }

}