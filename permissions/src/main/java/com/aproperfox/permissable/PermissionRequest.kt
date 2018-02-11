package com.aproperfox.permissable

/**
 * Created by aProperFox on 2/11/2018.
 */
data class PermissionRequest(val permission: String,
                             val resolver: PermissionsResolver)