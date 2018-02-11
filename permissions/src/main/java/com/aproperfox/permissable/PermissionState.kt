package com.aproperfox.permissable

/**
 * Created by tolso on 1/22/2018.
 */
sealed class PermissionState(val permission: String) {
  class Allowed(permission: String): PermissionState(permission)
  class Denied(permission: String): PermissionState(permission)
  class DeniedPermanently(permission: String): PermissionState(permission)
  class Unasked(permission: String): PermissionState(permission)
}