package com.speakoutall.ui.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UnFollowDialogModel(
    val profileUrl: String?,
    val username: String,
    val userId: String,
    val isFrom: String
) : Parcelable