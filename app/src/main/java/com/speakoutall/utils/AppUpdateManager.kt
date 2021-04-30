package com.speakoutall.utils

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.speakoutall.BuildConfig
import com.speakoutall.ui.MainActivity

class AppUpdateManager(private val activity: Activity) {

    fun checkAndUpdate() {
        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseDatabase.getInstance().getReference("app")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        FirebaseCrashlytics.getInstance().recordException(error.toException())
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val version = snapshot.child("appVersion").value.toString().toInt()
                            val updateType =
                                snapshot.child("updateType").value?.toString()?.toInt() ?: 0
                            checkUpdateAndUpdate(version, updateType)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }
    }

    private fun checkUpdateAndUpdate(version: Int, updateType: Int) {
        if (version > BuildConfig.VERSION_CODE) {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener {

                if (it.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                    return@addOnSuccessListener
                }

                if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        MainActivity.APP_UPDATE_REQUEST_CODE
                    )
                    return@addOnSuccessListener
                }

                val listener = InstallStateUpdatedListener { installState ->
                    if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                        appUpdateManager.completeUpdate()
                    }
                }
                appUpdateManager.registerListener(listener)
                if (updateType == AppUpdateType.FLEXIBLE) {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        MainActivity.APP_UPDATE_REQUEST_CODE
                    )
                } else {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        MainActivity.APP_UPDATE_REQUEST_CODE
                    )
                }
            }
        }
    }
}