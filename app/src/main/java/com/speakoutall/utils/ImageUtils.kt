package com.speakoutall.utils

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
import android.provider.MediaStore
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.speakoutall.common.Result
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.Exception

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
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return Single.just(null)
    }

    suspend fun uploadImageFromFile(imageFile: File): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val file = Uri.fromFile(imageFile)
                val ref = FirebaseUtils.getProfilePictureStorageRef().child(imageFile.name)
                val result = ref.putFile(file).continueWithTask { task ->
                    if (!task.isSuccessful || task.isCanceled) {
                        return@continueWithTask null
                    }
                    ref.downloadUrl
                }.await()
                if (result != null) {
                    Result.Success(result.toString())
                } else {
                    Result.Error(Exception("Failed to upload image"), null)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Result.Error(Exception("Failed to upload image"), null)
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
                            it.onSuccess(saveImage(resource, context))
                        }.start()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Timber.d("onLoadCleared")
                    }
                })
        }
    }

    fun saveImageToDevice(view: View, context: Context): Single<Boolean> {
        return convertToBitmap(view).flatMap {
            Single.just(saveImage(it, context))
        }
    }

    private fun saveImage(bitmap: Bitmap, context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val folderPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SpeakOut"
            )
            val imagePath = File(folderPath, "IMG${System.currentTimeMillis()}.jpg")

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
                    FirebaseCrashlytics.getInstance().recordException(e)
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