package com.afollestad.dragselectrecyclerview;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class DragSelectRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public interface SelectionListener {
        void onDragSelectionChanged(int count);
    }

    private ArrayList<Integer> mSelectedIndices;
    private SelectionListener mSelectionListener;
    private int mLastCount = -1;

    private void fireSelectionListener() {
        if (mLastCount == mSelectedIndices.size())
            return;
        mLastCount = mSelectedIndices.size();
        if (mSelectionListener != null)
            mSelectionListener.onDragSelectionChanged(mLastCount);
    }

    protected DragSelectRecyclerViewAdapter() {
        mSelectedIndices = new ArrayList<>();
    }

    public void setSelectionListener(SelectionListener selectionListener) {
        this.mSelectionListener = selectionListener;
    }

    public void saveInstanceState(Bundle out) {
        out.putSerializable("selected_indices", mSelectedIndices);
    }

    public void restoreInstanceState(Bundle in) {
        if (in != null && in.containsKey("selected_indices")) {
            //noinspection unchecked
            mSelectedIndices = (ArrayList<Integer>) in.getSerializable("selected_indices");
            if (mSelectedIndices == null) mSelectedIndices = new ArrayList<>();
            else fireSelectionListener();
        }
    }

    public final void setSelected(int index, boolean selected) {
        if (selected) {
            if (!mSelectedIndices.contains(index)) {
                mSelectedIndices.add(index);
                notifyItemChanged(index);
            }
        } else if (mSelectedIndices.contains(index)) {
            mSelectedIndices.remove(index);
            notifyItemChanged(index);
        }
        fireSelectionListener();
    }

    public final boolean toggleSelected(int index) {
        boolean selectedNow = false;
        if (mSelectedIndices.contains(index)) {
            mSelectedIndices.remove((Integer) index);
        } else {
            mSelectedIndices.add(index);
            selectedNow = true;
        }
        notifyItemChanged(index);
        fireSelectionListener();
        return selectedNow;
    }

    public final void selectRange(int from, int to, int min, int max) {
        if (from == to) {
            // Finger is back on the initial item, unselect everything else
            for (int i = min; i <= max; i++) {
                if (i == from) continue;
                if (mSelectedIndices.contains(i)) {
                    mSelectedIndices.remove((Integer) i);
                    notifyItemChanged(i);
                }
            }
            fireSelectionListener();
            return;
        }

        if (to < from) {
            // When selecting from one to previous items
            for (int i = to; i <= from; i++) {
                if (!mSelectedIndices.contains(i)) {
                    mSelectedIndices.add(i);
                    notifyItemChanged(i);
                }
            }
            if (min > -1 && min < to) {
                // Unselect items that were selected during this drag but no longer are
                for (int i = min; i < to; i++) {
                    if (i == from) continue;
                    if (mSelectedIndices.contains(i)) {
                        mSelectedIndices.remove((Integer) i);
                        notifyItemChanged(i);
                    }
                }
            }
            if (max > -1) {
                for (int i = from + 1; i <= max; i++) {
                    if (mSelectedIndices.contains(i)) {
                        mSelectedIndices.remove((Integer) i);
                        notifyItemChanged(i);
                    }
                }
            }
        } else {
            // When selecting from one to next items
            for (int i = from; i <= to; i++) {
                if (!mSelectedIndices.contains(i)) {
                    mSelectedIndices.add(i);
                    notifyItemChanged(i);
                }
            }
            if (max > -1 && max > to) {
                // Unselect items that were selected during this drag but no longer are
                for (int i = to + 1; i <= max; i++) {
                    if (i == from) continue;
                    if (mSelectedIndices.contains(i)) {
                        mSelectedIndices.remove((Integer) i);
                        notifyItemChanged(i);
                    }
                }
            }
            if (min > -1) {
                for (int i = min; i < from; i++) {
                    if (mSelectedIndices.contains(i)) {
                        mSelectedIndices.remove((Integer) i);
                        notifyItemChanged(i);
                    }
                }
            }
        }
        fireSelectionListener();
    }

    public final void clearSelected() {
        mSelectedIndices.clear();
        notifyDataSetChanged();
        fireSelectionListener();
    }

    public final int getSelectedCount() {
        return mSelectedIndices.size();
    }

    public final Integer[] getSelectedIndices() {
        return mSelectedIndices.toArray(new Integer[mSelectedIndices.size()]);
    }

    public final boolean isIndexSelected(int index) {
        return mSelectedIndices.contains(index);
    }
}