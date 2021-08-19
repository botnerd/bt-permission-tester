package com.botnerd.btpermissiontester.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

  var callback: Callback? = null

  val permissionText = MutableLiveData("")

  fun onRequestPermissionsClicked() {
    callback?.onRequestPermissionsClicked()
  }

  fun onStartAdvertisingClicked() {
    callback?.onStartAdvertising()
  }

  fun onStopAdvertisingClicked() {
    callback?.onStopAdvertising()
  }

  interface Callback {
    fun onRequestPermissionsClicked()
    fun onStartAdvertising()
    fun onStopAdvertising()
  }
}