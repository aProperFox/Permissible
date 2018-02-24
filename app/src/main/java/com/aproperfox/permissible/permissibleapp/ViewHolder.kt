package com.aproperfox.permissible.permissibleapp

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.permission_item.view.*

/**
 * Created by aProperFox on 2/11/2018.
 */
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  fun bind(state: PermissionViewState) {
    itemView.permissionName.text = state.name.substringAfterLast('.', "")
    itemView.permissionState.apply {
      text = state.status
      setTextColor(state.statusColor)
    }
  }
}