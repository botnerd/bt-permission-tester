package com.botnerd.example.sdk

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.RestrictTo

@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.targetSdkVersion(): Int {
  return try {
    packageManager.getApplicationInfo(packageName, 0).targetSdkVersion
  } catch (_: Exception) {
    0
  }
}