package com.botnerd.example.sdk

import android.Manifest
import android.app.*
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SimpleBluetoothService : Service() {

  var isActive = false

  private val advertiseCallback: AdvertiseCallback? by lazy {
    object : AdvertiseCallback() {
      override fun onStartFailure(errorCode: Int) {
        super.onStartFailure(errorCode)
        synchronized(isActive) {
          if (errorCode != ADVERTISE_FAILED_ALREADY_STARTED) {
            isActive = false
          }
        }
      }

      override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
        super.onStartSuccess(settingsInEffect)
      }
    }
  }

  private val _advSetCallback: AdvertisingSetCallback? by lazy {
    @RequiresApi(Build.VERSION_CODES.O)
    object : AdvertisingSetCallback() {
      override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet?, status: Int) {
        super.onAdvertisingDataSet(advertisingSet, status)
        synchronized(isActive) {
          if (status != ADVERTISE_SUCCESS) {
            isActive = false
          }
        }
      }

      override fun onAdvertisingSetStarted(
        advertisingSet: AdvertisingSet?,
        txPower: Int,
        status: Int
      ) {
        super.onAdvertisingSetStarted(advertisingSet, txPower, status)
        synchronized(isActive) {
          if (status != ADVERTISE_SUCCESS || status != ADVERTISE_FAILED_ALREADY_STARTED) {
            isActive = false
          }
        }
      }
    }
  }

  private val advSetCallback: AdvertisingSetCallback?
    @RequiresApi(Build.VERSION_CODES.O)
    get() {
      return _advSetCallback
    }


  private val binder: IBinder = LocalBinder()
  private val bluetoothManager: BluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
  }

  private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
    bluetoothManager.adapter
  }

  private val beaconManufacturerData: ByteArray?
    get() {
      return ByteArray(20)
    }

  inner class LocalBinder : Binder() {
    fun getService() : SimpleBluetoothService {
      return this@SimpleBluetoothService
    }
  }

  override fun onBind(intent: Intent): IBinder {
    return binder
  }

  fun startAdvertising() {
    synchronized(isActive) {
      if (!hasBluetoothPermission(this) ||
        !bluetoothAdapter.isEnabled ||
        !bluetoothAdapter.isMultipleAdvertisementSupported
      ) {
        return
      }

      val advData = AdvertiseData.Builder()
        .setIncludeTxPowerLevel(false)
        .addManufacturerData(0x0118, beaconManufacturerData?.copyOf())
        .build()
      val advScanResponse = AdvertiseData.Builder()
        .setIncludeDeviceName(false)
        .build()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val advSetParams = AdvertisingSetParameters.Builder()
          .setLegacyMode(true)
          .setIncludeTxPower(false)
          .setConnectable(false)
          .setScannable(true)
          .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MAX)
          .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
          .build()
        bluetoothAdapter.bluetoothLeAdvertiser?.startAdvertisingSet(
          advSetParams, advData,
          advScanResponse, null, null, 0, 0, advSetCallback
        )
      } else {
        val advSettings = AdvertiseSettings.Builder()
          .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
          .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
          .setConnectable(false)
          .build()
        bluetoothAdapter.bluetoothLeAdvertiser?.startAdvertising(
          advSettings, advData,
          advScanResponse, advertiseCallback
        )
      }
      isActive = true
    }
  }

  fun stopAdvertising() {
    synchronized(isActive) {
      if (isActive) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          bluetoothAdapter.bluetoothLeAdvertiser?.stopAdvertisingSet(advSetCallback)
        } else {
          bluetoothAdapter.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        }
        isActive = false
      }
    }
  }

  companion object {
    private fun bluetoothPermissions(context: Context): List<String> {
      return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            context.targetSdkVersion() >= Build.VERSION_CODES.S -> {
          listOf(Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
            context.targetSdkVersion() >= Build.VERSION_CODES.JELLY_BEAN_MR2 -> {
          listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }
        else -> listOf()
      }
    }

    private fun hasBluetoothPermission(context: Context): Boolean {
      val permissions = bluetoothPermissions(context)
      return permissions.isNotEmpty() && permissions.all {
        PermissionChecker.checkSelfPermission(context, it) == PermissionChecker.PERMISSION_GRANTED
      }
    }

  }
}