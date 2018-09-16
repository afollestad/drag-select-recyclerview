/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.dragselectrecyclerview

import androidx.annotation.CheckResult

/** @author Aidan Follestad (afollestad) */
interface DragSelectReceiver {

  @CheckResult fun getItemCount(): Int

  fun setSelected(
    index: Int,
    selected: Boolean
  )

  @CheckResult fun isSelected(index: Int): Boolean

  @CheckResult fun isIndexSelectable(index: Int): Boolean
}
