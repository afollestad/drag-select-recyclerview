package com.afollestad.dragselectrecyclerview;

/**
 * Listener interface for receiving drag selection events.
 */
public interface DragSelectionListener {

    /**
     * Invoked when drag selection occurs.
     *
     * @param count Number of items selected in drag selection.
     */
    void onDragSelectionChanged(int count);
}
