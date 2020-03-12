package com.speakout.ui.home

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.speakout.R
import com.speakout.extensions.addViewObserver
import com.speakout.utils.FirebaseUtils
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import java.util.*
import kotlin.random.Random

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

        fragment_home_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter
        }

////        lottie_anim.addViewObserver {
////            lottie_anim.scale = 0.4f
////            lottie_anim.requestLayout()
////        }
//        lottie_anim.setOnClickListener {
//            val valueAnimation = ValueAnimator.ofFloat(0f, 1f)
//                .setDuration(700)
//            valueAnimation.addUpdateListener {
//                lottie_anim.progress = it.animatedValue as Float
//            }
////            lottie_anim.playAnimation()
//
//            Timber.d("Is Animating: ${lottie_anim.isAnimating}")
//
////            if (lottie_anim.progress == 0f) {
//                valueAnimation.start()
////            } else {
////                lottie_anim.progress = 0f
////            }
//        }

        observeViewModels()
        mHomeViewModel.getPosts("")

    }

    private fun observeViewModels() {
        mHomeViewModel.posts.observe(viewLifecycleOwner, Observer {
            mPostsAdapter.updatePosts(it)
        })
    }

}