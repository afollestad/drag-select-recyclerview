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
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.afollestad.dragselectrecyclerview

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.afollestad.dragselectrecyclerview.Mode.PATH
import com.afollestad.dragselectrecyclerview.Mode.RANGE

enum class Mode {
  RANGE,
  PATH
}

typealias AutoScrollListener = (scrolling: Boolean) -> Unit

/** @author Aidan Follestad (afollestad) */
class DragSelectTouchListener private constructor(
  context: Context,
  private val receiver: DragSelectReceiver
) : RecyclerView.OnItemTouchListener {

  private val autoScrollHandler = Handler()
  private val autoScrollRunnable = object : Runnable {
    override fun run() {
      if (inTopHotspot) {
        recyclerView?.scrollBy(0, -autoScrollVelocity)
        autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY.toLong())
      } else if (inBottomHotspot) {
        recyclerView?.scrollBy(0, autoScrollVelocity)
        autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY.toLong())
      }
    }
  }

  var hotspotHeight: Int = context.dimen(R.dimen.dsrv_defaultHotspotHeight)
  var hotspotOffsetTop: Int = 0
  var hotspotOffsetBottom: Int = 0
  var autoScrollListener: AutoScrollListener? = null

  var mode: Mode = RANGE
    set(mode) {
      field = mode
      // Shouldn't maintain an active state through mode changes
      setIsActive(false, -1)
    }

  fun disableAutoScroll() {
    hotspotHeight = -1
    hotspotOffsetTop = -1
    hotspotOffsetBottom = -1
  }

  private var recyclerView: RecyclerView? = null

  private var lastDraggedIndex = -1
  private var initialSelection: Int = 0
  private var dragSelectActive: Boolean = false
  private var minReached: Int = 0
  private var maxReached: Int = 0

  private var hotspotTopBoundStart: Int = 0
  private var hotspotTopBoundEnd: Int = 0
  private var hotspotBottomBoundStart: Int = 0
  private var hotspotBottomBoundEnd: Int = 0
  private var inTopHotspot: Boolean = false
  private var inBottomHotspot: Boolean = false

  private var autoScrollVelocity: Int = 0
  private var isAutoScrolling: Boolean = false

  companion object {

    private const val AUTO_SCROLL_DELAY = 25
    private const val DEBUG_MODE = false

    @Suppress("ConstantConditionIf")
    private fun log(msg: String) {
      if (!DEBUG_MODE) return
      Log.d("DragSelectTL", msg)
    }

    fun create(
      context: Context,
      receiver: DragSelectReceiver,
      config: (DragSelectTouchListener.() -> Unit)? = null
    ): DragSelectTouchListener {
      val listener = DragSelectTouchListener(
          context = context,
          receiver = receiver
      )
      if (config != null) {
        listener.config()
      }
      return listener
    }
  }

  private fun notifyAutoScrollListener(scrolling: Boolean) {
    if (this.isAutoScrolling == scrolling) return
    log(if (scrolling) "Auto scrolling is active" else "Auto scrolling is inactive")
    this.isAutoScrolling = scrolling
    this.autoScrollListener?.invoke(scrolling)
  }

  /**
   * Initializes drag selection.
   *
   * @param active True if we are starting drag selection, false to terminate it.
   * @param initialSelection The index of the item which was pressed while starting drag selection.
   */
  fun setIsActive(
    active: Boolean,
    initialSelection: Int
  ): Boolean {
    if (active && dragSelectActive) {
      log("Drag selection is already active.")
      return false
    }

    this.lastDraggedIndex = -1
    this.minReached = -1
    this.maxReached = -1
    this.autoScrollHandler.removeCallbacks(autoScrollRunnable)
    this.notifyAutoScrollListener(false)
    this.inTopHotspot = false
    this.inBottomHotspot = false

    if (!active) {
      // Don't do any of the initialization below since we are terminating
      this.initialSelection = -1
      return false
    }

    if (!receiver.isIndexSelectable(initialSelection)) {
      this.dragSelectActive = false
      this.initialSelection = -1
      log("Index $initialSelection is not selectable.")
      return false
    }

    receiver.setSelected(
        index = initialSelection,
        selected = true
    )
    this.dragSelectActive = active
    this.initialSelection = initialSelection
    this.lastDraggedIndex = initialSelection

    log("Drag selection initialized, starting at index $initialSelection.")
    return true
  }

  @RestrictTo(Scope.LIBRARY_GROUP)
  override fun onInterceptTouchEvent(
    view: RecyclerView,
    event: MotionEvent
  ): Boolean {
    val adapterIsEmpty = view.adapter?.isEmpty() ?: true
    val result = dragSelectActive && !adapterIsEmpty

    if (result) {
      recyclerView = view
      log("RecyclerView height = ${view.measuredHeight}")

      if (hotspotHeight > -1) {
        hotspotTopBoundStart = hotspotOffsetTop
        hotspotTopBoundEnd = hotspotOffsetTop + hotspotHeight
        hotspotBottomBoundStart = view.measuredHeight - hotspotHeight - hotspotOffsetBottom
        hotspotBottomBoundEnd = view.measuredHeight - hotspotOffsetBottom
        log("Hotspot top bound = $hotspotTopBoundStart to $hotspotTopBoundEnd")
        log("Hotspot bottom bound = $hotspotBottomBoundStart to $hotspotBottomBoundEnd")
      }
    }

    return result
  }

  @RestrictTo(Scope.LIBRARY_GROUP)
  override fun onTouchEvent(
    view: RecyclerView,
    event: MotionEvent
  ) {
    val action = event.action
    val itemPosition = view.getItemPosition(event)
    val y = event.y

    when (action) {
      ACTION_UP -> {
        dragSelectActive = false
        inTopHotspot = false
        inBottomHotspot = false
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        this.notifyAutoScrollListener(false)
        return
      }
      ACTION_MOVE -> {
        if (hotspotHeight > -1) {
          // Check for auto-scroll hotspot
          if (y >= hotspotTopBoundStart && y <= hotspotTopBoundEnd) {
            inBottomHotspot = false
            if (!inTopHotspot) {
              inTopHotspot = true
              log("Now in TOP hotspot")
              autoScrollHandler.removeCallbacks(autoScrollRunnable)
              autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY.toLong())
              this.notifyAutoScrollListener(true)
            }
            val simulatedFactor = (hotspotTopBoundEnd - hotspotTopBoundStart).toFloat()
            val simulatedY = y - hotspotTopBoundStart
            autoScrollVelocity = (simulatedFactor - simulatedY).toInt() / 2
            log("Auto scroll velocity = $autoScrollVelocity")
          } else if (y >= hotspotBottomBoundStart && y <= hotspotBottomBoundEnd) {
            inTopHotspot = false
            if (!inBottomHotspot) {
              inBottomHotspot = true
              log("Now in BOTTOM hotspot")
              autoScrollHandler.removeCallbacks(autoScrollRunnable)
              autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY.toLong())
              this.notifyAutoScrollListener(true)
            }
            val simulatedY = y + hotspotBottomBoundEnd
            val simulatedFactor = (hotspotBottomBoundStart + hotspotBottomBoundEnd).toFloat()
            autoScrollVelocity = (simulatedY - simulatedFactor).toInt() / 2
            log("Auto scroll velocity = $autoScrollVelocity")
          } else if (inTopHotspot || inBottomHotspot) {
            log("Left the hotspot")
            autoScrollHandler.removeCallbacks(autoScrollRunnable)
            this.notifyAutoScrollListener(false)
            inTopHotspot = false
            inBottomHotspot = false
          }
        }

        // Drag selection logic
        if (mode == PATH && itemPosition != NO_POSITION) {
          // Non-default mode, we select exactly what the user touches over
          if (lastDraggedIndex == itemPosition) return
          lastDraggedIndex = itemPosition
          receiver.setSelected(
              index = lastDraggedIndex,
              selected = !receiver.isSelected(lastDraggedIndex)
          )
          return
        }

        if (mode == RANGE &&
            itemPosition != NO_POSITION &&
            lastDraggedIndex != itemPosition
        ) {
          lastDraggedIndex = itemPosition
          if (minReached == -1) minReached = lastDraggedIndex
          if (maxReached == -1) maxReached = lastDraggedIndex
          if (lastDraggedIndex > maxReached) maxReached = lastDraggedIndex
          if (lastDraggedIndex < minReached) minReached = lastDraggedIndex
          selectRange(
              from = initialSelection,
              to = lastDraggedIndex,
              min = minReached,
              max = maxReached
          )
          if (initialSelection == lastDraggedIndex) {
            minReached = lastDraggedIndex
            maxReached = lastDraggedIndex
          }
        }
        return
      }
    }
  }

  @RestrictTo(Scope.LIBRARY_GROUP)
  override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) = Unit

  private fun selectRange(
    from: Int,
    to: Int,
    min: Int,
    max: Int
  ) = with(receiver) {
    if (from == to) {
      // Finger is back on the initial item, unselect everything else
      for (i in min..max) {
        if (i == from) {
          continue
        }
        setSelected(i, false)
      }
      return
    }

    if (to < from) {
      // When selecting from one to previous items
      for (i in to..from) {
        setSelected(i, true)
      }
      if (min > -1 && min < to) {
        // Unselect items that were selected during this drag but no longer are
        for (i in min until to) {
          if (i == from) {
            continue
          }
          setSelected(i, false)
        }
      }
      if (max > -1) {
        for (i in from + 1..max) {
          setSelected(i, false)
        }
      }
    } else {
      // When selecting from one to next items
      for (i in from..to) {
        setSelected(i, true)
      }
      if (max > -1 && max > to) {
        // Unselect items that were selected during this drag but no longer are
        for (i in to + 1..max) {
          if (i == from) {
            continue
          }
          setSelected(i, false)
        }
      }
      if (min > -1) {
        for (i in min until from) {
          setSelected(i, false)
        }
      }
    }
  }
}
