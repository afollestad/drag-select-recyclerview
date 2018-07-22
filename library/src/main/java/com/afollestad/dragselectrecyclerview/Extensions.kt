package com.afollestad.dragselectrecyclerview

import android.content.Context
import android.support.annotation.DimenRes
import android.support.annotation.Px
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent

@Px internal fun Context.dimen(@DimenRes res: Int): Int {
  return resources.getDimensionPixelSize(res)
}

typealias RecyclerViewAdapter<T> = RecyclerView.Adapter<T>

internal fun RecyclerViewAdapter<*>.isEmpty(): Boolean {
  return itemCount == 0
}

internal fun RecyclerView.getItemPosition(e: MotionEvent): Int {
  val v = findChildViewUnder(e.x, e.y) ?: return RecyclerView.NO_POSITION
  return getChildAdapterPosition(v)
}