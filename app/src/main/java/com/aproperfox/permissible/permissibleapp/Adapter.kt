package com.aproperfox.permissible.permissibleapp

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Created by aProperFox on 2/11/2018.
 */
class Adapter : RecyclerView.Adapter<ViewHolder>() {
  var items: List<PermissionViewState> = emptyList()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      ViewHolder(View.inflate(parent.context, R.layout.permission_item, parent))

  override fun getItemCount(): Int = items.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

}