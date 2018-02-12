package com.aproperfox.permissible.permissibleapp

import android.app.Application
import android.util.Log
import com.aproperfox.permissible.PermissionsService

/**
 * Created by aProperFox on 2/11/2018.
 */
class PermissibleTestApp : Application() {

  override fun onCreate() {
    super.onCreate()
    val name = startService(PermissionsService.newIntent(this))
    Log.d("App", "Started service: $name")
  }

}