package com.aproperfox.permissible

import android.content.SharedPreferences

/**
* Created by aProperFox on 2/11/2018.
*/
data class PermissionAskedChecker(private val preferences: SharedPreferences) {

  companion object {
    private const val PERMISSIONS_PREF_KEY: String = "key_asked_permissions_set"
  }

  fun hasAskedPermission(permission: String): Boolean =
    stringSet().contains(permission)

  fun setAskedPermission(vararg permissions: String) {
    preferences.edit().apply {
      val tempSet = mutableSetOf<String>()
      permissions.forEach {
        tempSet.add(it)
      }
      putStringSet(PERMISSIONS_PREF_KEY, tempSet)
    }.apply()
  }

  private fun stringSet() = preferences.getStringSet(PERMISSIONS_PREF_KEY, emptySet<String>())
}