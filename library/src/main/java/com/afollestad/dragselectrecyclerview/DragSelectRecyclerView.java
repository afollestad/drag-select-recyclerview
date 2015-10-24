package com.afollestad.dragselectrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DragSelectRecyclerView extends RecyclerView {

    public DragSelectRecyclerView(Context context) {
        super(context);
    }

    public DragSelectRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragSelectRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private int mLastDraggedIndex = -1;
    private DragSelectRecyclerViewAdapter<?> mAdapter;
    private int mInitialSelection;
    private boolean mDragSelectActive;
    private int mMinReached;
    private int mMaxReached;

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mDragSelectActive) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                mDragSelectActive = false;
                return true;
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
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