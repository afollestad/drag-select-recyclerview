package com.afollestad.dragselectrecyclerviewsample

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.IntegerRes
import android.support.annotation.Px
import android.support.v4.content.ContextCompat
import android.view.View

@Px internal fun Context.dimen(@DimenRes res: Int): Int {
  return resources.getDimensionPixelSize(res)
}

@ColorInt internal fun Context.color(@ColorRes res: Int): Int {
  return ContextCompat.getColor(this, res)
}

internal fun Context.integer(@IntegerRes res: Int): Int {
  return resources.getInteger(res)
}

internal fun Activity.setLightNavBarCompat() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    var flags = window.decorView.systemUiVisibility
    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    window.decorView.systemUiVisibility = flags
  }
}