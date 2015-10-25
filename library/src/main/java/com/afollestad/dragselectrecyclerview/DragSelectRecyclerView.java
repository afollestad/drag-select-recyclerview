package com.afollestad.dragselectrecyclerview;

/**
 * Implemented by {@link android.support.v7.widget.RecyclerView} that supports drag selection.
 */
public interface DragSelectRecyclerView {

    /**
     * Sets state of drag selection.
     *
     * @param active           True if drag selection is active.
     * @param initialSelection Position of item that started drag selection.
     */
    void setDragSelectActive(boolean active, int initialSelection);
}
