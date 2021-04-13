package com.speakoutall.posts.view

import com.speakoutall.posts.create.PostData

interface OnPostOptionsClickListener {
    fun onCopy(post: PostData)
    fun onDelete(post: PostData)
    fun onSave(post: PostData)
}