package com.afollestad.dragselectrecyclerviewsample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewHelper;

/**
 * TODO:
 */
public class MainRecyclerView extends RecyclerView implements DragSelectRecyclerView {

    private final DragSelectRecyclerView mDragSelectHelper
            = new DragSelectRecyclerViewHelper(this);

    public MainRecyclerView(Context context) {
        super(context);
    }

    public MainRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setDragSelectActive(boolean active, int initialSelection) {
        mDragSelectHelper.setDragSelectActive(active, initialSelection);
    }
}
