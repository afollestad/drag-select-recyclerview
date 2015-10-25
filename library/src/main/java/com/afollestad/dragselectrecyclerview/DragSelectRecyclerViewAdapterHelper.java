package com.afollestad.dragselectrecyclerview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Helper providing drag selection functionality for {@link android.support.v7.widget.RecyclerView.Adapter}
 */
public class DragSelectRecyclerViewAdapterHelper implements DragSelectRecyclerViewAdapter {

    private static final String BUNDLE_KEY_SELECTED_INDICES = "selected_indices";

    private final RecyclerView.Adapter mAdapter;
    private final ArrayList<Integer> mSelectedIndices = new ArrayList<>();
    private DragSelectionListener mSelectionListener;
    private int mLastCount = -1;

    public DragSelectRecyclerViewAdapterHelper(@NonNull RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void setSelectionListener(@NonNull DragSelectionListener selectionListener) {
        mSelectionListener = selectionListener;
    }

    @Override
    public void saveInstanceState(@NonNull Bundle out) {
        out.putIntegerArrayList(BUNDLE_KEY_SELECTED_INDICES, mSelectedIndices);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle in) {
        final ArrayList<Integer> selectedIndices = in.getIntegerArrayList(BUNDLE_KEY_SELECTED_INDICES);
        if (selectedIndices != null) {
            mSelectedIndices.addAll(selectedIndices);
        }
    }

    @Override
    public final void setSelected(int index, boolean selected) {
        if (selected) {
            addToSelected(index);
        } else if (mSelectedIndices.contains(index)) {
            mSelectedIndices.remove(index);
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public final boolean toggleSelected(int index) {
        boolean selectedNow = false;
        if (mSelectedIndices.contains(index)) {
            mSelectedIndices.remove((Integer) index);
        } else {
            mSelectedIndices.add(index);
            selectedNow = true;
        }
        mAdapter.notifyItemChanged(index);
        fireSelectionListener();
        return selectedNow;
    }

    private void fireSelectionListener() {
        if (mLastCount == mSelectedIndices.size())
            return;
        mLastCount = mSelectedIndices.size();
        if (mSelectionListener != null)
            mSelectionListener.onDragSelectionChanged(mLastCount);
    }

    @Override
    public final void selectRange(int from, int to, int min, int max) {
        if (from == to) {
            // Finger is back on the initial item, unselect everything else
            removeAllFromSelected(from, min, max);
        } else if (to < from) {
            // When selecting from one to previous items
            addAllToSelected(to, from);
            if (min > -1 && min < to) {
                // Unselect items that were selected during this drag but no longer are
                removeAllFromSelected(from, min, to - 1);
            }
            if (max > -1) {
                removeAllFromSelected(from + 1, max);
            }
        } else {
            // When selecting from one to next items
            addAllToSelected(from, to);
            if (max > -1 && max > to) {
                // Unselect items that were selected during this drag but no longer are
                removeAllFromSelected(from, to + 1, max);
            }
            if (min > -1) {
                removeAllFromSelected(min, from - 1);
            }
        }
        fireSelectionListener();
    }

    private void addAllToSelected(int from, int to) {
        for (int pos = from; pos <= to; pos++) {
            addToSelected(pos);
        }
    }

    private void addToSelected(int pos) {
        if (!mSelectedIndices.contains(pos)) {
            mSelectedIndices.add(pos);
            mAdapter.notifyItemChanged(pos);
        }
    }

    private void removeAllFromSelected(int ignore, int min, int max) {
        for (int pos = min; pos <= max; pos++) {
            if (pos == ignore) {
                continue;
            }
            removeFromSelected(pos);
        }
    }

    private void removeAllFromSelected(int min, int max) {
        for (int pos = min; pos <= max; pos++) {
            removeFromSelected(pos);
        }
    }

    private void removeFromSelected(int pos) {
        if (mSelectedIndices.contains(pos)) {
            mSelectedIndices.remove((Integer) pos);
            mAdapter.notifyItemChanged(pos);
        }
    }

    @Override
    public final void clearSelected() {
        mSelectedIndices.clear();
        mAdapter.notifyDataSetChanged();
        fireSelectionListener();
    }

    @Override
    public final int getSelectedCount() {
        return mSelectedIndices.size();
    }

    @Override
    public final Integer[] getSelectedIndices() {
        return mSelectedIndices.toArray(new Integer[mSelectedIndices.size()]);
    }

    @Override
    public final boolean isIndexSelected(int index) {
        return mSelectedIndices.contains(index);
    }
}