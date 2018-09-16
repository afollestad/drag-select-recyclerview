/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.dragselectrecyclerviewsample

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/** @author Aidan Follestad (afollestad) */
class RectangleView(
  context: Context,
  attrs: AttributeSet?
) : FrameLayout(context, attrs) {

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    setMeasuredDimension(measuredWidth, (measuredWidth * 1.4f).toInt())
  }
}
