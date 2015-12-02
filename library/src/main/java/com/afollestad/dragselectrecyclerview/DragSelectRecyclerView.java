package com.afollestad.dragselectrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.afollestad.library.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DragSelectRecyclerView extends RecyclerView {

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

    private static void LOG(String message, Object... args) {
        if (args != null) {
            Log.d("DragSelectRecyculerView", String.format(message, args));
        } else {
            Log.d("DragSelectRecyculerView", message);
        }
    }

    private int mLastDraggedIndex = -1;
    private DragSelectRecyclerViewAdapter<?> mAdapter;
    private int mInitialSelection;
    private boolean mDragSelectActive;
    private int mMinReached;
    private int mMaxReached;

    private int mHotspotHeight;
    private int mHotspotTopBound;
    private int mHotspotBottomBound;
    private int mAutoScrollVelocity;

    private void init(Context context, AttributeSet attrs) {
        mAutoScrollHandler = new Handler();
        final int defaultHotspotHeight = context.getResources().getDimensionPixelSize(R.dimen.dsrv_defaultHotspotHeight);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragSelectRecyclerView, 0, 0);
            try {
                boolean autoScrollEnabled = a.getBoolean(R.styleable.DragSelectRecyclerView_dsrv_autoScrollEnabled, true);
                if (!autoScrollEnabled) {
                    mHotspotHeight = -1;
                    LOG("Auto-scroll disabled");
                } else {
                    mHotspotHeight = a.getDimensionPixelSize(
                            R.styleable.DragSelectRecyclerView_dsrv_autoScrollHotspotHeight, defaultHotspotHeight);
                    LOG("Hotspot height = %d", mHotspotHeight);
                }
            } finally {
                a.recycle();
            }
        } else {
            mHotspotHeight = defaultHotspotHeight;
            LOG("Hotspot height = %d", mHotspotHeight);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (mHotspotHeight > -1) {
            mHotspotTopBound = mHotspotHeight;
            mHotspotBottomBound = getMeasuredHeight() - mHotspotHeight;
            LOG("RecyclerView height = %d", getMeasuredHeight());
            LOG("Hotspot top bound = %d", mHotspotTopBound);
            LOG("Hotspot bottom bound = %d", mHotspotBottomBound);
        }
    }

    public void setDragSelectActive(boolean active, int initialSelection) {
        mAdapter.setSelected(initialSelection, true);
        mLastDraggedIndex = -1;
        mDragSelectActive = active;
        mInitialSelection = initialSelection;
        mLastDraggedIndex = initialSelection;
        mMinReached = -1;
        mMaxReached = -1;
    }

    /**
     * Use {@link #setAdapter(DragSelectRecyclerViewAdapter)} instead.
     */
    @Override
    @Deprecated
    public void setAdapter(Adapter adapter) {
        if (!(adapter instanceof DragSelectRecyclerViewAdapter<?>))
            throw new IllegalArgumentException("Adapter must be a DragSelectRecyclerViewAdapter.");
        setAdapter((DragSelectRecyclerViewAdapter<?>) adapter);
    }

    public void setAdapter(DragSelectRecyclerViewAdapter<?> adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
    }

    private boolean mInTopHotspot;
    private boolean mInBottomHotspot;

    private Handler mAutoScrollHandler;
    private Runnable mAutoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAutoScrollHandler == null)
                return;
            if (mInTopHotspot) {
                scrollBy(0, -mAutoScrollVelocity);
                mAutoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            } else if (mInBottomHotspot) {
                scrollBy(0, mAutoScrollVelocity);
                mAutoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mDragSelectActive) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                mDragSelectActive = false;
                mInTopHotspot = false;
                mInBottomHotspot = false;
                mAutoScrollHandler.removeCallbacks(mAutoScrollRunnable);
                return true;
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                if (mHotspotHeight > -1) {
                    if (e.getY() <= mHotspotTopBound) {
                        mInBottomHotspot = false;
                        if (!mInTopHotspot) {
                            mInTopHotspot = true;
                            LOG("Now in TOP hotspot");
                            mAutoScrollHandler.removeCallbacks(mAutoScrollRunnable);
                            mAutoScrollHandler.postDelayed(mAutoScrollRunnable, AUTO_SCROLL_DELAY);
                        }
                        mAutoScrollVelocity = (int) (mHotspotTopBound - e.getY()) / 2;
                        LOG("Auto scroll velocity = %d", mAutoScrollVelocity);
                    } else if (e.getY() >= mHotspotBottomBound) {
                        mInTopHotspot = false;
                        if (!mInBottomHotspot) {
                            mInBottomHotspot = true;
                            LOG("Now in BOTTOM hotspot");
                            mAutoScrollHandler.removeCallbacks(mAutoScrollRunnable);
                            mAutoScrollHandler.postDelayed(mAutoScrollRunnable, AUTO_SCROLL_DELAY);
                        }
                        mAutoScrollVelocity = (int) (e.getY() - mHotspotBottomBound) / 2;
                        LOG("Auto scroll velocity = %d", mAutoScrollVelocity);
                    } else if (mInTopHotspot || mInBottomHotspot) {
                        LOG("Left the hotspot");
                        mAutoScrollHandler.removeCallbacks(mAutoScrollRunnable);
                        mInTopHotspot = false;
                        mInBottomHotspot = false;
                    }
                }

                View v = findChildViewUnder(e.getX(), e.getY());
                if (v != null && mLastDraggedIndex != (Integer) v.getTag()) {
                    mLastDraggedIndex = (Integer) v.getTag();
                    if (mMinReached == -1) mMinReached = mLastDraggedIndex;
                    if (mMaxReached == -1) mMaxReached = mLastDraggedIndex;
                    if (mLastDraggedIndex > mMaxReached)
                        mMaxReached = mLastDraggedIndex;
                    if (mLastDraggedIndex < mMinReached)
                        mMinReached = mLastDraggedIndex;
                    if (mAdapter != null)
                        mAdapter.selectRange(mInitialSelection, mLastDraggedIndex, mMinReached, mMaxReached);
                    if (mInitialSelection == mLastDraggedIndex) {
                        mMinReached = mLastDraggedIndex;
                        mMaxReached = mLastDraggedIndex;
                    }
                }
                return true;
            }
        }
        return super.dispatchTouchEvent(e);
    }
}