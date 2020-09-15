package com.speakout.posts.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.speakout.R
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.extensions.gone
import com.speakout.extensions.loadImageWithCallback
import com.speakout.extensions.visible
import com.speakout.ui.notifications.NotificationsViewModel
import kotlinx.android.synthetic.main.item_post_layout.*
import kotlinx.android.synthetic.main.item_post_layout.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class SinglePostViewFragment : Fragment() {

    private val safeArgs: SinglePostViewFragmentArgs by navArgs()
    private val notificationsViewModel: NotificationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationsViewModel.getPost(safeArgs.postId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_post_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationsViewModel.singlePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {

            }
        })
    }

}