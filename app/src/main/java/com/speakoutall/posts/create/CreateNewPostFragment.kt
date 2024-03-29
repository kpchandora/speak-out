package com.speakoutall.posts.create

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
import com.speakoutall.R
import com.speakoutall.databinding.FragmentCreateNewPostBinding
import com.speakoutall.extensions.*
import com.speakoutall.ui.BottomDialogActivity
import com.speakoutall.utils.ImageUtils
import com.speakoutall.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CreateNewPostFragment : Fragment() {

    private val mCreatePostViewModel: CreatePostViewModel by navGraphViewModels(R.id.create_post_navigation)
    private var postContent: String = ""
    private var binding: FragmentCreateNewPostBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateNewPostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(view)
        binding?.run {
            toolbarContainer.toolbarTitle.text = getString(R.string.title_new_post)

            createPostContainerLayout.addViewObserver {
                activity?.let {
                    it.getScreenSize().let {
                        createPostContainerLayout.layoutParams.height = it.widthPixels
                        createPostContainerLayout.requestLayout()
                    }
                }
            }

            createPostContentTv.text = postContent

            createPostNextBtn.isEnabled = postContent.length > 10

            createPostContainerLayout.setOnClickListener {
                startActivityForResult(
                    Intent(requireContext(), BottomDialogActivity::class.java).putExtra(
                        BottomDialogActivity.CONTENT, createPostContentTv.text.toString()
                    ), Constants.IntentStrings.CreatePost.REQUEST_CODE
                )
                requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
            }

            createPostNextBtn.setOnClickListener {
                navigateToTags()
            }
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
        binding?.run {
            ImageUtils.convertToBitmap(createPostContainerLayout)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bitmap ->
                    bitmap?.let {
                        mCreatePostViewModel.imageBitmap = it
                        postContent = createPostContentTv.text.toString()
                        findNavController().navigate(
                            CreateNewPostFragmentDirections.actionCreateNewPostFragmentToTagsFragment(
                                createPostContentTv.text.toString()
                            )
                        )
                    } ?: kotlin.run {
                        showShortToast("Failed to upload post")
                    }
                }) { t ->
                    showShortToast("Failed to upload post: $t")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.IntentStrings.CreatePost.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let {
                    it.getString(BottomDialogActivity.CONTENT)?.let { content ->
                        binding?.createPostContentTv?.text = content
                        binding?.createPostNextBtn?.isEnabled = content.length > 10
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
