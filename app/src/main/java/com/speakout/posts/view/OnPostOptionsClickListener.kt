package com.speakout.posts.view

import com.speakout.posts.create.PostData

interface OnPostOptionsClickListener {
    fun onCopy(post: PostData)
    fun onDelete(post: PostData)
    fun onSave(post: PostData)
}