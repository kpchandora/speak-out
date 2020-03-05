package com.speakout.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import io.reactivex.Single
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

    fun getBitmapFromUri(uri: String) {

    }

}