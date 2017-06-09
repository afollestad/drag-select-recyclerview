package com.afollestad.dragselectrecyclerview;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;

/** @author Aidan Follestad (afollestad) */
public abstract class DragSelectRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {

  public interface SelectionListener {
    void onDragSelectionChanged(int count);
  }

  private ArrayList<Integer> selectedIndices;
  private SelectionListener selectionListener;
  private int lastCount = -1;
  private int maxSelectionCount = -1;

  private void fireSelectionListener() {
    if (lastCount == selectedIndices.size()) return;
    lastCount = selectedIndices.size();
    if (selectionListener != null) selectionListener.onDragSelectionChanged(lastCount);
  }

  protected DragSelectRecyclerViewAdapter() {
    selectedIndices = new ArrayList<>();
  }

  public void setMaxSelectionCount(int maxSelectionCount) {
    this.maxSelectionCount = maxSelectionCount;
  }

  public void setSelectionListener(SelectionListener selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void saveInstanceState(Bundle out) {
    saveInstanceState("selected_indices", out);
  }

  public void saveInstanceState(String key, Bundle out) {
    out.putSerializable(key, selectedIndices);
  }

  public void restoreInstanceState(Bundle in) {
    restoreInstanceState("selected_indices", in);
  }

  public void restoreInstanceState(String key, Bundle in) {
    if (in != null && in.containsKey(key)) {
      //noinspection unchecked
      selectedIndices = (ArrayList<Integer>) in.getSerializable(key);
      if (selectedIndices == null) selectedIndices = new ArrayList<>();
      else fireSelectionListener();
    }
  }

  public final void setSelected(int index, boolean selected) {
    if (!isIndexSelectable(index)) selected = false;
    if (selected) {
      if (!selectedIndices.contains(index)
          && (maxSelectionCount == -1 || selectedIndices.size() < maxSelectionCount)) {
        selectedIndices.add(index);
        notifyItemChanged(index);
      }
    } else if (selectedIndices.contains(index)) {
      selectedIndices.remove((Integer) index);
      notifyItemChanged(index);
    }
    fireSelectionListener();
  }

  public final boolean toggleSelected(int index) {
    boolean selectedNow = false;
    if (isIndexSelectable(index)) {
      if (selectedIndices.contains(index)) {
        selectedIndices.remove((Integer) index);
      } else if (maxSelectionCount == -1 || selectedIndices.size() < maxSelectionCount) {
        selectedIndices.add(index);
        selectedNow = true;
      }
      notifyItemChanged(index);
    }
    fireSelectionListener();
    return selectedNow;
  }

  protected boolean isIndexSelectable(int index) {
    return true;
  }

  @CallSuper
  @Override
  public void onBindViewHolder(VH holder, int position) {
    holder.itemView.setTag(holder);
  }

  public final void selectRange(int from, int to, int min, int max) {
    if (from == to) {
      // Finger is back on the initial item, unselect everything else
      for (int i = min; i <= max; i++) {
        if (i == from) {
          continue;
        }
        setSelected(i, false);
      }
      fireSelectionListener();
      return;
    }

    if (to < from) {
      // When selecting from one to previous items
      for (int i = to; i <= from; i++) setSelected(i, true);
      if (min > -1 && min < to) {
        // Unselect items that were selected during this drag but no longer are
        for (int i = min; i < to; i++) {
          if (i == from) {
            continue;
          }
          setSelected(i, false);
        }
      }
      if (max > -1) {
        for (int i = from + 1; i <= max; i++) setSelected(i, false);
      }
    } else {
      // When selecting from one to next items
      for (int i = from; i <= to; i++) setSelected(i, true);
      if (max > -1 && max > to) {
        // Unselect items that were selected during this drag but no longer are
        for (int i = to + 1; i <= max; i++) {
          if (i == from) {
            continue;
          }
          setSelected(i, false);
        }
      }
      if (min > -1) {
        for (int i = min; i < from; i++) setSelected(i, false);
      }
    }
    fireSelectionListener();
  }

  public final void selectAll() {
    int max = getItemCount();
    selectedIndices.clear();
    for (int i = 0; i < max; i++) {
      if (isIndexSelectable(i)) {
        selectedIndices.add(i);
      }
    }
    notifyDataSetChanged();
    fireSelectionListener();
  }

  public final void clearSelected() {
    selectedIndices.clear();
    notifyDataSetChanged();
    fireSelectionListener();
  }

  public final int getSelectedCount() {
    return selectedIndices.size();
  }

  public final Integer[] getSelectedIndices() {
    return selectedIndices.toArray(new Integer[selectedIndices.size()]);
  }

  public final boolean isIndexSelected(int index) {
    return selectedIndices.contains(index);
  }
}
