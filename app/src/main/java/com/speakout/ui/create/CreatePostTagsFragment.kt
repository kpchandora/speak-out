package com.speakout.ui.create


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.speakout.R

/**
 * A simple [Fragment] subclass.
 */
class CreatePostTagsFragment : Fragment() {

    companion object {
        fun newInstance(bundle: Bundle?) = CreatePostTagsFragment().apply {
            arguments = bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post_tags, container, false)
    }


}
