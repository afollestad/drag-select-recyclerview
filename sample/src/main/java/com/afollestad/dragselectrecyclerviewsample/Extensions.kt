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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.view.View
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

internal fun Activity.prefs(key: String) = getSharedPreferences(key, MODE_PRIVATE)

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
  key: String,
  default: T
) = getInt(key, -1).let { if (it >= 0) enumValues<T>()[it] else default }

fun <T : Enum<T>> PrefsEditor.putEnum(
  key: String,
  value: T?
) = putInt(key, value?.ordinal ?: -1)!!

@SuppressLint("ApplySharedPref")
internal fun SharedPreferences.edit(exec: PrefsEditor.() -> Unit) {
  val editor = this.edit()
  editor.exec()
  editor.commit()
}
