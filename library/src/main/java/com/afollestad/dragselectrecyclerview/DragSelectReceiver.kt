/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.dragselectrecyclerview

/** @author Aidan Follestad (afollestad) */
interface DragSelectReceiver {

  fun getItemCount(): Int

  fun setSelected(
    index: Int,
    selected: Boolean
  )

  fun isIndexSelectable(index: Int): Boolean
}
