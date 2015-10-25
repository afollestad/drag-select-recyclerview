package com.afollestad.dragselectrecyclerview;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Implemented by {@link android.support.v7.widget.RecyclerView.Adapter} that supports drag selection.
 */
public interface DragSelectRecyclerViewAdapter {

    /**
     * Sets a listener that's notified of selection changes, used in the section above.
     */
    void setSelectionListener(@NonNull DragSelectionListener selectionListener);

    /**
     * Used in section above, saves selected indices to Bundle.
     */
    void saveInstanceState(@NonNull Bundle out);

    /**
     * Used in section above, restores selected indices from Bundle.
     */
    void restoreInstanceState(@NonNull Bundle in);

    /**
     * Sets an index as selected (true) or unselected (false).
     */
    void setSelected(int index, boolean selected);

    /**
     * If an index is selected, unselect it. Otherwise select it. Returns new selection state.
     */
    boolean toggleSelected(int index);

    /**
     * For internal usage.
     */
    void selectRange(int from, int to, int min, int max);

    /**
     * Clears all selected indices.
     */
    void clearSelected();

    /**
     * Gets the number of selected indices.
     */
    int getSelectedCount();

    /**
     * Gets all selected indices.
     */
    Integer[] getSelectedIndices();

    /**
     * Checks if an index is selected, useful in adapter subclass.
     */
    boolean isIndexSelected(int index);
}
