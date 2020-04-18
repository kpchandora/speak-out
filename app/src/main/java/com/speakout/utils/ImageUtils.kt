package com.speakout.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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

    fun saveImageToDevice(url: String, context: Context): Single<Boolean> {
        return Single.create {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Thread {
                            Timber.d("Main Thread: ${Looper.getMainLooper() == Looper.myLooper()}")
                            it.onSuccess(saveImage(resource, context))
                        }.start()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Timber.d("onLoadCleared")
                    }
                })
        }
    }

    private fun saveImage(bitmap: Bitmap, context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val folderPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SpeakOut"
            )
            val imagePath = File(folderPath, "IMG${System.currentTimeMillis()}.jpg")

            Timber.d("Saved Image Path: ${imagePath.path}")

            var isSuccess = true
            if (!folderPath.exists()) {
                isSuccess = folderPath.mkdir()
            }

            if (isSuccess) {
                try {
                    val outputStream = FileOutputStream(imagePath)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val uri = Uri.fromFile(File(imagePath.path))
                    mediaScanIntent.data = uri
                    context.sendBroadcast(mediaScanIntent)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let {
                contentResolver.openOutputStream(imageUri)?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    it.close()
                    return true
                }
            }
        }
        return false
    }

}