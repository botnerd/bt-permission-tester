package com.botnerd.btpermissiontester.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.botnerd.btpermissiontester.MainActivity
import com.botnerd.btpermissiontester.MainActivityUiCallback
import com.botnerd.btpermissiontester.R
import com.botnerd.btpermissiontester.databinding.MainFragmentBinding
import com.botnerd.example.sdk.SimpleBluetoothService
import java.lang.StringBuilder

class MainFragment : Fragment() {

  companion object {
    fun newInstance() = MainFragment()
  }

  private var activityUiCallback : MainActivityUiCallback? = null

  private lateinit var viewModel: MainViewModel
  private lateinit var binding: MainFragmentBinding

  private var dashboardService: SimpleBluetoothService? = null

  // Code to manage Service lifecycle.
  private val serviceConnection = object : ServiceConnection {

    override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
      dashboardService = (service as? SimpleBluetoothService.LocalBinder)?.getService()
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
      dashboardService = null
    }
  }


  private val uiCallback = object : MainViewModel.Callback {
    override fun onRequestPermissionsClicked() {
      activityUiCallback?.checkBluetoothPermission { enabled, permissions ->
        StringBuilder().apply {
          if (enabled) {
            append(getString(R.string.permissions_granted))
          } else {
            append(getString(R.string.permissions_denied))
          }
          permissions.forEach {
            append("\n$it")
          }
          viewModel.permissionText.postValue(toString())
        }
      }
    }

    override fun onStartAdvertising() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        dashboardService?.startAdvertising()
      }
    }

    override fun onStopAdvertising() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        dashboardService?.stopAdvertising()
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    binding = MainFragmentBinding.inflate(inflater).apply {
      lifecycleOwner = this@MainFragment
    }
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    viewModel.callback = uiCallback
    binding.data = viewModel
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is MainActivity) {
      activityUiCallback = context.uiCallback
    }
  }

  override fun onStart() {
    super.onStart()
    doBindService()
  }

  override fun onStop() {
    super.onStop()
    doUnbindService()
  }

  override fun onDetach() {
    super.onDetach()
    activityUiCallback = null
  }

  private fun doBindService() {
    if (null == dashboardService) {
      context?.bindService(
        Intent(context, SimpleBluetoothService::class.java),
        serviceConnection,
        AppCompatActivity.BIND_AUTO_CREATE
      )
    }
  }

  private fun doUnbindService() {
    if (null != dashboardService) {
      context?.unbindService(serviceConnection)
      dashboardService = null
    }
  }


}