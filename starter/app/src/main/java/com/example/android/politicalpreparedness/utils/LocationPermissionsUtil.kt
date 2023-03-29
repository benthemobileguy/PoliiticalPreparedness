package com.example.android.politicalpreparedness.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class LocationPermissionsUtil(private var listener: PermissionListener?) {

    interface PermissionListener {
        fun onGranted()
        fun onDenied()
    }

    private var resultLauncher: ActivityResultLauncher<String>? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }

    fun registerForResultAndRequestPermissions(fragment: Fragment) {
        if (isPermissionGranted(fragment.requireContext())) {
            listener?.onGranted()
        } else {
            requestPermission(fragment)
        }
    }

    fun registerForPermissionResults(fragment: Fragment) {
        registerForLocationResults(fragment)
    }

    fun requestPermissions() {
        launchLocationRequest()
    }

    fun unregister() {
        listener = null
    }

    private fun isPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
                context,
                PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(fragment: Fragment) {
        registerForLocationResults(fragment)
        launchLocationRequest()
    }

    private fun registerForLocationResults(fragment: Fragment) {
        val permissions = ActivityResultContracts.RequestPermission()
        resultLauncher = fragment.registerForActivityResult(permissions) {
            if (it) {
                listener?.onGranted()
            } else {
                listener?.onDenied()
            }
        }
    }

    private fun launchLocationRequest() {
        resultLauncher?.launch(PERMISSION, ActivityOptionsCompat.makeBasic())
    }
}
