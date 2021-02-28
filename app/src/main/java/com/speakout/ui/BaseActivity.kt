package com.speakout.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.speakout.custom.CustomProgressDialog
import com.speakout.events.RxBus
import com.speakout.extensions.showShortToast
import com.speakout.utils.FirebaseUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: CustomProgressDialog
    private val mCompositeDisposable = CompositeDisposable()
    private lateinit var mConnectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        mConnectivityManager.registerDefaultNetworkCallback(connectivityCallback)

        mProgressDialog = CustomProgressDialog(this)
        mCompositeDisposable += RxBus.listen(Any::class.java).subscribe {
            showShortToast(it.toString())
        }
    }

    fun showProgress() {
        mProgressDialog.show()
    }

    fun hideProgress() {
        mProgressDialog.dismiss()
    }

    fun currentUser() = FirebaseUtils.currentUser()

    fun userId() = FirebaseUtils.userId()

    fun signOut() = FirebaseUtils.signOut()

    override fun onDestroy() {
        mConnectivityManager.unregisterNetworkCallback(connectivityCallback)
        mCompositeDisposable.clear()
        super.onDestroy()
    }

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) or
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            ) {
                //Connected
            } else {
                //No Connected
            }
        }

        override fun onLost(network: Network) {
            //No Connected
        }
    }

}