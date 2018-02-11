package com.aproperfox.permissable

import android.app.Activity
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.support.v4.content.PermissionChecker
import com.aproperfox.permissable.PermissionState.*
import java.security.Permission

/**
 * Created by tolso on 1/22/2018.
 */
class ShadowActivity : Activity() {

  companion object {
    private const val REQUEST_PERMISSION: Int = 93627
  }

  fun requestPermissions(permissions: Array<String>) {
    requestPermissions(permissions, REQUEST_PERMISSION)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_PERMISSION) {
      val states = permissions.zip(grantResults.asIterable(), { perm, result ->
        val shouldResolve = shouldShowRequestPermissionRationale(perm)
        when {
          result == PERMISSION_GRANTED -> Allowed(perm)
          shouldResolve -> DeniedPermanently(perm)
          else -> Denied(perm)
        }
      })
    }
  }
}