/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.dragselectrecyclerviewsample

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

typealias PrefsEditor = SharedPreferences.Editor

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

private var toast: Toast? = null

internal fun Context.toast(message: String) {
  toast?.cancel()
  toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
      .apply { show() }
}
