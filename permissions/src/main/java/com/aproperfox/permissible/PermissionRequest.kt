package com.aproperfox.permissible

/**
 * Created by aProperFox on 2/11/2018.
 */
data class PermissionRequest(val permission: String,
                             val resolver: PermissionsResolver)