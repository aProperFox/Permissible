package com.aproperfox.permissible

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.aproperfox.permissible.PermissionState.*

/**
 * Created by aProperFox on 1/22/2018.
 */
class ShadowActivity : Activity() {

  companion object {
    private const val TAG = "ShadowActivity"
    private const val PERMISSIONS_KEY = "key_permissions_requested"
    private const val REQUEST_PERMISSION: Int = 93627
    private const val IS_REQUESTING_KEY = "key_is_requesting"
    private const val PREF_SUFFIX = "permissions_checker"

    @TargetApi(Build.VERSION_CODES.M)
    fun requestIntent(context: Context, permissions: Array<String>): Intent =
        Intent(context, ShadowActivity::class.java)
            .putExtra(PERMISSIONS_KEY, permissions)
            .putExtra(IS_REQUESTING_KEY, true)

    @TargetApi(Build.VERSION_CODES.M)
    fun checkIntent(context: Context, permissions: Array<String>): Intent =
        Intent(context, ShadowActivity::class.java)
            .putExtra(PERMISSIONS_KEY, permissions)
            .putExtra(IS_REQUESTING_KEY, false)
  }

  lateinit var checker: PermissionAskedChecker

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    checker = PermissionAskedChecker(
        getSharedPreferences("${applicationContext.packageName}-$PREF_SUFFIX", Context.MODE_PRIVATE)
    )
    savedInstanceState?.let {
      with(it, {
        val permissions = getStringArray(PERMISSIONS_KEY)
        if (getBoolean(IS_REQUESTING_KEY)) {
          requestPermissions(permissions)
        } else {
          val splitPerms = permissions.partition { checker.hasAskedPermission(it) }
          val permissionStates = getPermissionStates(splitPerms.first.toTypedArray()) +
              splitPerms.second.map {
                it to Unasked
              }
          object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
              Log.d(TAG, "Service disconnected: $name")
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
              Log.d(TAG, "Service connected: $name. Service: $service")
              if (service is PermissionsService.PermissionBinder) {
                service.getService()
                    .setPermissionState(permissionStates)
                finish()
              }
            }
          }
        }
      })
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun getPermissionStates(permissions: Array<String>): Map<String, PermissionState> =
      permissionsToStates(permissions, permissions.map { checkSelfPermission(it) }.toIntArray())

  @TargetApi(Build.VERSION_CODES.M)
  private fun requestPermissions(permissions: Array<String>) {
    requestPermissions(permissions, REQUEST_PERMISSION)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_PERMISSION) {
      checker.setAskedPermission(*permissions)
      val states = permissionsToStates(permissions, grantResults)
      object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
          Log.d(TAG, "Service disconnected: $name")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
          Log.d(TAG, "Service connected: $name. Service: $service")
          if (service is PermissionsService.PermissionBinder) {
            service.getService()
                .setPermissionState(states)
            finish()
          }
        }
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun permissionsToStates(permissions: Array<out String>, grantResults: IntArray): Map<String, PermissionState> =
      permissions.zip(grantResults.asIterable(), { perm, result ->
        val shouldResolve = shouldShowRequestPermissionRationale(perm)
        perm to when {
          result == PERMISSION_GRANTED -> Allowed
          shouldResolve -> DeniedPermanently
          else -> Denied
        }
      }).toMap()
}