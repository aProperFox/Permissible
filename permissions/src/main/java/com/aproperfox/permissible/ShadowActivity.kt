package com.aproperfox.permissible

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale
import android.util.Log
import com.aproperfox.permissible.PermissionState.*
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by aProperFox on 1/22/2018.
 */
class ShadowActivity : Activity(), ServiceConnection {

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

  private val disposable = Disposables.disposed()
  private val permissionStateSubject = BehaviorSubject.create<Map<String, PermissionState>>()

  lateinit var checker: PermissionAskedChecker

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    checker = PermissionAskedChecker(
        applicationContext.getSharedPreferences("${applicationContext.packageName}-$PREF_SUFFIX", Context.MODE_PRIVATE)
    )
    bindService(PermissionsService.newIntent(this), this, Context.BIND_AUTO_CREATE)
    intent?.let {
      with(it, {
        val permissions = getStringArrayExtra(PERMISSIONS_KEY)
        Log.d(TAG, "requested: ${permissions.fold("", { first, second -> "$first, $second" })}")
        if (getBooleanExtra(IS_REQUESTING_KEY, false)) {
          requestPermissions(permissions)
        } else {
          val splitPerms = permissions.partition { checker.hasAskedPermission(it) }
          val permissionStates = getPermissionStates(splitPerms.first.toTypedArray()) +
              splitPerms.second.map { it to Unasked }
          permissionStateSubject.onNext(permissionStates)
        }
      })
    }
  }

  override fun onServiceDisconnected(name: ComponentName) {
    Log.d(TAG, "Service disconnected: $name")
    disposable.dispose()
  }

  override fun onServiceConnected(name: ComponentName, service: IBinder) {
    Log.d(TAG, "Service connected: $name. Service: $service")
    if (service is PermissionsService.PermissionBinder) {
      if (disposable.isDisposed) {
        permissionStateSubject
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe({
              Log.d(TAG, "State: ${
              it.entries.map { "${it.key}:${it.value}" }
                  .joinToString(",")
              }")
              service.getService()
                  .setPermissionState(it)
              finish()
            }, { it.printStackTrace() })
      }
    }
  }

  override fun finish() {
    super.finish()
    unbindService(this)
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun getPermissionStates(permissions: Array<String>): Map<String, PermissionState> =
      permissionsToStates(permissions, permissions.map(::checkSelfPermission).toIntArray())

  @TargetApi(Build.VERSION_CODES.M)
  private fun requestPermissions(permissions: Array<String>) {
    requestPermissions(permissions, REQUEST_PERMISSION)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_PERMISSION) {
      checker.setAskedPermission(*permissions)
      val states = permissionsToStates(permissions, grantResults)
      permissionStateSubject.onNext(states)
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun permissionsToStates(permissions: Array<out String>, grantResults: IntArray): Map<String, PermissionState> =
      permissions.zip(grantResults.asIterable(), { perm, result ->
        val shouldResolve = shouldShowRequestPermissionRationale(perm)
        perm to when {
          result == PERMISSION_GRANTED -> Allowed
          shouldResolve -> Denied
          else -> DeniedPermanently
        }
      }).toMap()
}