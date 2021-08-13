package com.botnerd.btpermissiontester

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.botnerd.btpermissiontester.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

  private var bluetoothPermissionChangeCallback: ((Boolean, permissions: Array<String>) -> Unit)? = null

  val uiCallback = object : MainActivityUiCallback {

    override fun hasBluetoothPermissions(): Boolean {
      return BLUETOOTH_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
      }
    }

    override fun checkBluetoothPermission(permissionChangeCallback: (enabled: Boolean, permissions: Array<String>) -> Unit) {
      bluetoothPermissionChangeCallback = permissionChangeCallback
      permissionChangeCallback(hasBluetoothPermissions(), BLUETOOTH_PERMISSIONS)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.container, MainFragment.newInstance())
        .commitNow()
    }
  }

  companion object {
    private val BLUETOOTH_PERMISSIONS by lazy {
      arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
    }
  }

}