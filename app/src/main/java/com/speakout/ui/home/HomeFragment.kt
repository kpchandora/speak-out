package com.speakout.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.speakout.R
import com.speakout.utils.FirebaseUtils
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.random.Random

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.demoTextObserver.observe(this, Observer {
            textView.text = it
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        text_home.setOnClickListener {
//            val list = mutableListOf<String>()
//            for (i in 0..(Random.nextInt(1, 2))) {
//                list.add(UUID.randomUUID().toString())
//            }
//            list.forEach {
//                var ref = FirebaseUtils.getReference().child("list").push()
//
//                for (i in 0..28) {
//                    ref = ref.child("$i")
//                }
//
//                ref.push().setValue(it)
//            }
//        }
    }

}