package com.speakoutall.posts.tags

interface OnTagClickListener {
    fun onTagClick(tag: Tag)

    fun onAddNewTag(tag: Tag){}

}