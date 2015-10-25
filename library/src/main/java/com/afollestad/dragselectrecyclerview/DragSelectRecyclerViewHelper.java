package com.afollestad.dragselectrecyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Helper providing drag selection functionality to {@link RecyclerView}.
 */
public class DragSelectRecyclerViewHelper implements View.OnTouchListener, DragSelectRecyclerView {

    private final RecyclerView mRecyclerView;

    private int mLastDraggedIndex = -1;
    private int mInitialSelection;
    private boolean mDragSelectActive;
    private int mMinReached;
    private int mMaxReached;

    public DragSelectRecyclerViewHelper(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setOnTouchListener(this);
    }

    @Override
    public void setDragSelectActive(boolean active, int initialSelection) {
        getDragAdapter().setSelected(initialSelection, true);
        mLastDraggedIndex = -1;
        mDragSelectActive = active;
        mInitialSelection = initialSelection;
        mLastDraggedIndex = initialSelection;
        mMinReached = -1;
        mMaxReached = -1;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mDragSelectActive) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            mDragSelectActive = false;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final View view = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if (view == null || mLastDraggedIndex == (Integer) view.getTag()) {
                return true;
            }
            mLastDraggedIndex = (Integer) view.getTag();
            if (mMinReached == -1) {
                mMinReached = mLastDraggedIndex;
            }
            if (mMaxReached == -1) {
                mMaxReached = mLastDraggedIndex;
            }
            if (mLastDraggedIndex > mMaxReached) {
                mMaxReached = mLastDraggedIndex;
            }
            if (mLastDraggedIndex < mMinReached) {
                mMinReached = mLastDraggedIndex;
            }
            getDragAdapter().selectRange(mInitialSelection, mLastDraggedIndex, mMinReached, mMaxReached);
            if (mInitialSelection == mLastDraggedIndex) {
                mMinReached = mLastDraggedIndex;
                mMaxReached = mLastDraggedIndex;
            }
            return true;
        }
        return false;
    }

    @NonNull
    private DragSelectRecyclerViewAdapter getDragAdapter() {
        if (mRecyclerView.getAdapter() == null
                || !(mRecyclerView.getAdapter() instanceof DragSelectRecyclerViewAdapter)) {
            throw new IllegalStateException(
                    "Supplied RecyclerView doesn't have a DragSelectRecyclerViewAdapter.");
        }
        return (DragSelectRecyclerViewAdapter) mRecyclerView.getAdapter();
    }
}
