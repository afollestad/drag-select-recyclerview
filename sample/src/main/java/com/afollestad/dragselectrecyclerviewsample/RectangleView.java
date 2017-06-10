package com.afollestad.dragselectrecyclerviewsample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/** @author Aidan Follestad (afollestad) */
public class RectangleView extends FrameLayout {

  public RectangleView(Context context) {
    super(context);
  }

  public RectangleView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RectangleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * 1.4f));
  }
}
