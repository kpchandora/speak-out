package com.speakout.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FirebaseFirestore
import com.speakout.R
import com.speakout.utils.FirebaseUtils
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
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
        homeViewModel.demoTextObserver.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val db = FirebaseFirestore.getInstance()
        text_home.setOnClickListener {

//            val user = hashMapOf(
//                "first" to "Ada",
//                "last" to "Lovelace",
//                "born" to 1815
//            )
//            db.collection("users")
//                .add(user)
//                .addOnSuccessListener { documentReference ->
//                    Timber.d("DocumentSnapshot added with ID: ${documentReference.id}")
//                }
//                .addOnFailureListener { e ->
//                    Timber.e("Error adding document: $e")
//                }

        }
    }

}