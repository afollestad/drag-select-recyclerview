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
