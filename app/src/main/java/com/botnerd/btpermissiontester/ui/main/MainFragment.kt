package com.botnerd.btpermissiontester.ui.main

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.botnerd.btpermissiontester.MainActivity
import com.botnerd.btpermissiontester.MainActivityUiCallback
import com.botnerd.btpermissiontester.R
import com.botnerd.btpermissiontester.databinding.MainFragmentBinding
import java.lang.StringBuilder

class MainFragment : Fragment() {

  companion object {
    fun newInstance() = MainFragment()
  }

  private var activityUiCallback : MainActivityUiCallback? = null

  private lateinit var viewModel: MainViewModel
  private lateinit var binding: MainFragmentBinding

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

  override fun onDetach() {
    super.onDetach()
    activityUiCallback = null
  }


}