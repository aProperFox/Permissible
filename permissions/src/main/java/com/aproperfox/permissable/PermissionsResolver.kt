package com.aproperfox.permissable

/**
 * Created by aProperFox on 2/11/2018.
 */
sealed class PermissionsResolver {
  class Grouped(handler: (PermissionState) -> Unit) : PermissionsResolver()
  class Independent(
      onAccept: () -> Unit,
      onDeny: () -> Unit,
      onDenyPermanently: () -> Unit
  ) : PermissionsResolver()
}