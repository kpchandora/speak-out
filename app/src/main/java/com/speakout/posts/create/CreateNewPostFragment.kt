package com.speakout.posts.create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.speakout.R
import com.speakout.extensions.*
import com.speakout.ui.BottomDialogActivity
import com.speakout.utils.ImageUtils
import com.speakout.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_new_post.*

class CreateNewPostFragment : Fragment() {

    private val mCreatePostViewModel: CreatePostViewModel by navGraphViewModels(R.id.create_post_navigation)
    private var postContent: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_new_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(view)
        create_post_container_layout.addViewObserver {
            activity?.let {
                it.getScreenSize().let {
                    create_post_container_layout.layoutParams.height = it.widthPixels
                    create_post_container_layout.requestLayout()
                }
            }
        }

        create_post_content_tv.text = postContent

        create_post_container_layout.setOnClickListener {
            startActivityForResult(
                Intent(requireContext(), BottomDialogActivity::class.java).putExtra(
                    BottomDialogActivity.CONTENT, create_post_content_tv.text.toString()
                ), Constants.IntentStrings.CreatePost.REQUEST_CODE
            )
        }

        create_post_next_btn.setOnClickListener {
            navigateToTags()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("isSuccess")
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigateUp()
                }
            }
    }

    @SuppressLint("CheckResult")
    private fun navigateToTags() {
        ImageUtils.convertToBitmap(create_post_container_layout)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bitmap ->
                bitmap?.let {
                    mCreatePostViewModel.imageBitmap = it
                    postContent = create_post_content_tv.text.toString()
                    findNavController().navigate(
                        CreateNewPostFragmentDirections.actionCreateNewPostFragmentToTagsFragment(
                            create_post_content_tv.text.toString()
                        )
                    )
                } ?: kotlin.run {
                    showShortToast("Failed to upload post")
                }
            }) { t ->
                showShortToast("Failed to upload post: $t")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.IntentStrings.CreatePost.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let {
                    it.getString(BottomDialogActivity.CONTENT)?.let { content ->
                        create_post_content_tv.text = content
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
