package com.botnerd.btpermissiontester

interface MainActivityUiCallback {
    fun hasBluetoothPermissions(): Boolean
    fun checkBluetoothPermission(permissionChangeCallback: (enabled: Boolean, permissions: Array<String>) -> Unit)
}