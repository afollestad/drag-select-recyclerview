/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.dragselectrecyclerviewsample

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

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
