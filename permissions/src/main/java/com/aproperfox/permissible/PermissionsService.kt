package com.aproperfox.permissible

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log

/**
 * Created by aProperFox on 2/11/2018.
 */
class PermissionsService : Service() {

  companion object {
    private const val TAG = "PermissionsService"

    fun newIntent(context: Context) = Intent(context, PermissionsService::class.java)
  }

  init {
    Log.d(TAG, "STARTING")
  }

  private val binder: IBinder = PermissionBinder()

  inner class PermissionBinder : Binder() {
    fun getService(): PermissionsService =
        this@PermissionsService
  }

  private var permissionsState: Map<String, PermissionState> = emptyMap()
  private val listeners by lazy {
    mutableListOf<(Map<String, PermissionState>) -> Unit>()
  }

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "Package name: ${applicationContext.packageName}")
    val permissions = applicationContext.packageManager
        .getPackageInfo(
            applicationContext.packageName,
            PackageManager.GET_PERMISSIONS
        ).requestedPermissions

    startActivity(ShadowActivity.checkIntent(applicationContext, permissions))
  }

  override fun onBind(intent: Intent?): IBinder =
      binder

  @TargetApi(Build.VERSION_CODES.M)
  fun requestPermissions(permissions: Array<String>) {
    startActivity(ShadowActivity.requestIntent(applicationContext, permissions))
  }

  fun addListener(listener: (Map<String, PermissionState>) -> Unit) {
    listeners.add(listener)
    listener.invoke(permissionsState)
  }

  fun removeListener(listener: (Map<String, PermissionState>) -> Unit) {
    listeners.remove(listener)
  }

  internal fun setPermissionState(state: Map<String, PermissionState>) {
    val keys = state.keys.plus(permissionsState.keys)
    permissionsState = keys.map {
      it to (state[it] ?: permissionsState[it])!!
    }.toMap()
    listeners.forEach {
      it.invoke(permissionsState)
    }
  }
}