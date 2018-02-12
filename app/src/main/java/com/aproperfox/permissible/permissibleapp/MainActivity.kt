package com.aproperfox.permissible.permissibleapp

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import com.aproperfox.permissible.PermissionState
import com.aproperfox.permissible.PermissionsService
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by aProperFox on 2/11/2018.
 */
class MainActivity : Activity(), ServiceConnection {

  private val adapter = Adapter()
  private lateinit var service: PermissionsService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    bindService(Intent(this, PermissionsService::class.java), this, Context.BIND_AUTO_CREATE)
  }

  private val stateChangedListener: (Map<String, PermissionState>) -> Unit = {
    val viewState = it.entries
        .map {
          PermissionViewState(it.key, it.value.name,
              when (it.value) {
                PermissionState.Allowed -> Color.GREEN
                PermissionState.Denied -> Color.YELLOW
                PermissionState.DeniedPermanently -> Color.RED
                PermissionState.Unasked -> Color.GRAY
              })
        }
    adapter.items = viewState
  }

  override fun onServiceDisconnected(name: ComponentName) {
    service.removeListener(stateChangedListener)
  }

  override fun onServiceConnected(name: ComponentName, service: IBinder) {
    if (service is PermissionsService.PermissionBinder) {
      this.service = service.getService()
      this.service.addListener(stateChangedListener)
    }
  }
}