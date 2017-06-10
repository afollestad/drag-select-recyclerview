package com.afollestad.dragselectrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/** @author Aidan Follestad (afollestad) */
@SuppressWarnings("unused")
public class DragSelectRecyclerView extends RecyclerView {

  @SuppressWarnings("WeakerAccess")
  public interface FingerListener {
    void onDragSelectFingerAction(boolean fingerDown);
  }

  private static final int AUTO_SCROLL_DELAY = 25;

  public DragSelectRecyclerView(Context context) {
    super(context);
    init(context, null);
  }

  public DragSelectRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public DragSelectRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void LOG(String message, Object... args) {
    if (!debugEnabled) {
      return;
    }
    if (args != null) {
      message = String.format(message, args);
    }
    if (message.equals(lastDebugMsg)) {
      return;
    }
    lastDebugMsg = message;
    Log.d("DragSelectRecyclerView", message);
  }

  private int lastDraggedIndex = -1;
  private IDragSelectAdapter adapter;
  private int initialSelection;
  private boolean dragSelectActive;
  private int minReached;
  private int maxReached;

  private int hotspotHeight;
  private int hotspotOffsetTop;
  private int hotspotOffsetBottom;

  private int hotspotTopBoundStart;
  private int hotspotTopBoundEnd;
  private int hotspotBottomBoundStart;
  private int hotspotBottomBoundEnd;
  private int autoScrollVelocity;

  private FingerListener fingerListener;

  private void init(Context context, AttributeSet attrs) {
    autoScrollHandler = new Handler();
    final int defaultHotspotHeight =
        context.getResources().getDimensionPixelSize(R.dimen.dsrv_defaultHotspotHeight);

    if (attrs != null) {
      TypedArray a =
          context
              .getTheme()
              .obtainStyledAttributes(attrs, R.styleable.DragSelectRecyclerView, 0, 0);
      try {
        boolean autoScrollEnabled =
            a.getBoolean(R.styleable.DragSelectRecyclerView_dsrv_autoScrollEnabled, true);
        if (!autoScrollEnabled) {
          hotspotHeight = -1;
          hotspotOffsetTop = -1;
          hotspotOffsetBottom = -1;
          LOG("Auto-scroll disabled");
        } else {
          hotspotHeight =
              a.getDimensionPixelSize(
                  R.styleable.DragSelectRecyclerView_dsrv_autoScrollHotspotHeight,
                  defaultHotspotHeight);
          hotspotOffsetTop =
              a.getDimensionPixelSize(
                  R.styleable.DragSelectRecyclerView_dsrv_autoScrollHotspot_offsetTop, 0);
          hotspotOffsetBottom =
              a.getDimensionPixelSize(
                  R.styleable.DragSelectRecyclerView_dsrv_autoScrollHotspot_offsetBottom, 0);
          LOG("Hotspot height = %d", hotspotHeight);
        }
      } finally {
        a.recycle();
      }
    } else {
      hotspotHeight = defaultHotspotHeight;
      LOG("Hotspot height = %d", hotspotHeight);
    }
  }

  public void setFingerListener(@Nullable FingerListener listener) {
    this.fingerListener = listener;
  }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    super.onMeasure(widthSpec, heightSpec);
    if (hotspotHeight > -1) {
      hotspotTopBoundStart = hotspotOffsetTop;
      hotspotTopBoundEnd = hotspotOffsetTop + hotspotHeight;
      hotspotBottomBoundStart = (getMeasuredHeight() - hotspotHeight) - hotspotOffsetBottom;
      hotspotBottomBoundEnd = getMeasuredHeight() - hotspotOffsetBottom;
      LOG("RecyclerView height = %d", getMeasuredHeight());
      LOG("Hotspot top bound = %d to %d", hotspotTopBoundStart, hotspotTopBoundStart);
      LOG("Hotspot bottom bound = %d to %d", hotspotBottomBoundStart, hotspotBottomBoundEnd);
    }
  }

  public boolean setDragSelectActive(boolean active, int initialSelection) {
    if (active && dragSelectActive) {
      LOG("Drag selection is already active.");
      return false;
    }
    lastDraggedIndex = -1;
    minReached = -1;
    maxReached = -1;
    if (!adapter.isIndexSelectable(initialSelection)) {
      dragSelectActive = false;
      this.initialSelection = -1;
      lastDraggedIndex = -1;
      LOG("Index %d is not selectable.", initialSelection);
      return false;
    }
    adapter.setSelected(initialSelection, true);
    dragSelectActive = active;
    this.initialSelection = initialSelection;
    lastDraggedIndex = initialSelection;
    if (fingerListener != null) {
      fingerListener.onDragSelectFingerAction(true);
    }
    LOG("Drag selection initialized, starting at index %d.", initialSelection);
    return true;
  }

  @Override
  public void setAdapter(Adapter adapter) {
    if (!(adapter instanceof IDragSelectAdapter)) {
      throw new IllegalArgumentException("Adapter must be implement IDragSelectAdapter.");
    }
    this.adapter = (IDragSelectAdapter) adapter;
    super.setAdapter(adapter);
  }

  private boolean inTopHotspot;
  private boolean inBottomHotspot;

  private Handler autoScrollHandler;
  private Runnable autoScrollRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (autoScrollHandler == null) {
            return;
          }
          if (inTopHotspot) {
            scrollBy(0, -autoScrollVelocity);
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
          } else if (inBottomHotspot) {
            scrollBy(0, autoScrollVelocity);
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
          }
        }
      };

  private int getItemPosition(MotionEvent e) {
    final View v = findChildViewUnder(e.getX(), e.getY());
    if (v == null) {
      return NO_POSITION;
    }
    return getChildAdapterPosition(v);
  }

  private RectF topBoundRect;
  private RectF bottomBoundRect;
  private Paint debugPaint;
  private boolean debugEnabled = false;
  private String lastDebugMsg;

  public final void enableDebug() {
    debugEnabled = true;
    invalidate();
  }

  @Override
  public void onDraw(Canvas c) {
    super.onDraw(c);

    if (debugEnabled) {
      if (debugPaint == null) {
        debugPaint = new Paint();
        debugPaint.setColor(Color.BLACK);
        debugPaint.setAntiAlias(true);
        debugPaint.setStyle(Paint.Style.FILL);
        topBoundRect = new RectF(0, hotspotTopBoundStart, getMeasuredWidth(), hotspotTopBoundEnd);
        bottomBoundRect =
            new RectF(0, hotspotBottomBoundStart, getMeasuredWidth(), hotspotBottomBoundEnd);
      }
      c.drawRect(topBoundRect, debugPaint);
      c.drawRect(bottomBoundRect, debugPaint);
    }
  }

  private void selectRange(int from, int to, int min, int max) {
    if (from == to) {
      // Finger is back on the initial item, unselect everything else
      for (int i = min; i <= max; i++) {
        if (i == from) {
          continue;
        }
        adapter.setSelected(i, false);
      }
      return;
    }

    if (to < from) {
      // When selecting from one to previous items
      for (int i = to; i <= from; i++) {
        adapter.setSelected(i, true);
      }
      if (min > -1 && min < to) {
        // Unselect items that were selected during this drag but no longer are
        for (int i = min; i < to; i++) {
          if (i == from) {
            continue;
          }
          adapter.setSelected(i, false);
        }
      }
      if (max > -1) {
        for (int i = from + 1; i <= max; i++) {
          adapter.setSelected(i, false);
        }
      }
    } else {
      // When selecting from one to next items
      for (int i = from; i <= to; i++) {
        adapter.setSelected(i, true);
      }
      if (max > -1 && max > to) {
        // Unselect items that were selected during this drag but no longer are
        for (int i = to + 1; i <= max; i++) {
          if (i == from) {
            continue;
          }
          adapter.setSelected(i, false);
        }
      }
      if (min > -1) {
        for (int i = min; i < from; i++) {
          adapter.setSelected(i, false);
        }
      }
    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent e) {
    if (adapter == null) {
      LOG("No IDragSelectAdapter has been set.");
      return super.dispatchTouchEvent(e);
    }
    if (adapter.getItemCount() == 0) {
      LOG("Adapter reported 0 item count.");
      return super.dispatchTouchEvent(e);
    }
    if (dragSelectActive) {
      LOG("Drag selection is active");
      final int itemPosition = getItemPosition(e);
      if (e.getAction() == MotionEvent.ACTION_UP) {
        dragSelectActive = false;
        inTopHotspot = false;
        inBottomHotspot = false;
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        if (fingerListener != null) {
          fingerListener.onDragSelectFingerAction(false);
        }
        return true;
      } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
        // Check for auto-scroll hotspot
        if (hotspotHeight > -1) {
          if (e.getY() >= hotspotTopBoundStart && e.getY() <= hotspotTopBoundEnd) {
            inBottomHotspot = false;
            if (!inTopHotspot) {
              inTopHotspot = true;
              LOG("Now in TOP hotspot");
              autoScrollHandler.removeCallbacks(autoScrollRunnable);
              autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            }

            final float simulatedFactor = hotspotTopBoundEnd - hotspotTopBoundStart;
            final float simulatedY = e.getY() - hotspotTopBoundStart;
            autoScrollVelocity = (int) (simulatedFactor - simulatedY) / 2;

            LOG("Auto scroll velocity = %d", autoScrollVelocity);
          } else if (e.getY() >= hotspotBottomBoundStart && e.getY() <= hotspotBottomBoundEnd) {
            inTopHotspot = false;
            if (!inBottomHotspot) {
              inBottomHotspot = true;
              LOG("Now in BOTTOM hotspot");
              autoScrollHandler.removeCallbacks(autoScrollRunnable);
              autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            }

            final float simulatedY = e.getY() + hotspotBottomBoundEnd;
            final float simulatedFactor = hotspotBottomBoundStart + hotspotBottomBoundEnd;
            autoScrollVelocity = (int) (simulatedY - simulatedFactor) / 2;

            LOG("Auto scroll velocity = %d", autoScrollVelocity);
          } else if (inTopHotspot || inBottomHotspot) {
            LOG("Left the hotspot");
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            inTopHotspot = false;
            inBottomHotspot = false;
          }
        }

        // Drag selection logic
        if (itemPosition != NO_POSITION && lastDraggedIndex != itemPosition) {
          lastDraggedIndex = itemPosition;
          if (minReached == -1) {
            minReached = lastDraggedIndex;
          }
          if (maxReached == -1) {
            maxReached = lastDraggedIndex;
          }
          if (lastDraggedIndex > maxReached) {
            maxReached = lastDraggedIndex;
          }
          if (lastDraggedIndex < minReached) {
            minReached = lastDraggedIndex;
          }
          if (adapter != null) {
            selectRange(initialSelection, lastDraggedIndex, minReached, maxReached);
          }
          if (initialSelection == lastDraggedIndex) {
            minReached = lastDraggedIndex;
            maxReached = lastDraggedIndex;
          }
        }
        return true;
      }
    } else {
      LOG("Drag selection is not active.");
    }
    return super.dispatchTouchEvent(e);
  }
}
