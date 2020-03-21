package com.speakout.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.core.net.toFile
import io.reactivex.Single
import java.io.File
import java.lang.Exception

object ImageUtils {
    fun convertToBitmap(view: View): Single<Bitmap?> {
        try {
            val returnedBitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(returnedBitmap)
            val bgDrawable = view.background
            if (bgDrawable != null) {
                bgDrawable.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }

            view.draw(canvas)
            return Single.just(returnedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Single.just(null)
    }

    fun uploadImageFromFile(imageFile: File): Single<String?> {
        return Single.create {
            val file = Uri.fromFile(imageFile)
            val ref =
                FirebaseUtils.getProfilePictureStorageRef().child(imageFile.name)
            ref.putFile(file).continueWithTask { task ->
                if (!task.isSuccessful || task.isCanceled) {
                    it.onError(task.exception as Throwable)
                    return@continueWithTask null
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    it.onSuccess(task.result?.toString() ?: "")
                } else {
                    it.onError(Exception("Failed"))
                }
            }

        }
    }

}