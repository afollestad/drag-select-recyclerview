package com.afollestad.dragselectrecyclerviewsample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.afollestad.dragselectrecyclerview.IDragSelectAdapter;
import java.util.ArrayList;
import java.util.List;

/** @author Aidan Follestad (afollestad) */
class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder>
    implements IDragSelectAdapter {

  private final List<Integer> selectedIndices;

  private static final String[] ALPHABET =
      "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split(" ");
  private static final int[] COLORS =
      new int[] {
        Color.parseColor("#F44336"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#673AB7"),
        Color.parseColor("#3F51B5"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#03A9F4"),
        Color.parseColor("#00BCD4"),
        Color.parseColor("#009688"),
        Color.parseColor("#4CAF50"),
        Color.parseColor("#8BC34A"),
        Color.parseColor("#CDDC39"),
        Color.parseColor("#FFEB3B"),
        Color.parseColor("#FFC107"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#FF5722"),
        Color.parseColor("#795548"),
        Color.parseColor("#9E9E9E"),
        Color.parseColor("#607D8B"),
        Color.parseColor("#F44336"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#673AB7"),
        Color.parseColor("#3F51B5"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#03A9F4")
      };

  interface Listener {
    void onClick(int index);

    void onLongClick(int index);

    void onSelectionChanged(int count);
  }

  private final Listener callback;

  MainAdapter(Listener callback) {
    super();
    this.selectedIndices = new ArrayList<>(16);
    this.callback = callback;
  }

  String getItem(int index) {
    return ALPHABET[index];
  }

  List<Integer> getSelectedIndices() {
    return selectedIndices;
  }

  void toggleSelected(int index) {
    if (selectedIndices.contains(index)) {
      selectedIndices.remove((Integer) index);
    } else {
      selectedIndices.add(index);
    }
    notifyItemChanged(index);
    if (callback != null) {
      callback.onSelectionChanged(selectedIndices.size());
    }
  }

  void clearSelected() {
    if (selectedIndices.isEmpty()) {
      return;
    }
    selectedIndices.clear();
    notifyDataSetChanged();
    if (callback != null) {
      callback.onSelectionChanged(0);
    }
  }

  @Override
  public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.griditem_main, parent, false);
    return new MainViewHolder(v, callback);
  }

  @Override
  public void onBindViewHolder(MainViewHolder holder, int position) {
    holder.label.setText(getItem(position));

    final Drawable d;
    final Context c = holder.itemView.getContext();

    if (selectedIndices.contains(position)) {
      d = new ColorDrawable(ContextCompat.getColor(c, R.color.grid_foreground_selected));
      holder.label.setTextColor(ContextCompat.getColor(c, R.color.grid_label_text_selected));
    } else {
      d = null;
      holder.label.setTextColor(ContextCompat.getColor(c, R.color.grid_label_text_normal));
    }

    //noinspection RedundantCast
    ((FrameLayout) holder.colorSquare).setForeground(d);
    holder.colorSquare.setBackgroundColor(COLORS[position]);
  }

  @Override
  public void setSelected(int index, boolean selected) {
    Log.d("MainAdapter", "setSelected(" + index + ", " + selected + ")");
    if (!selected) {
      selectedIndices.remove((Integer) index);
    } else if (!selectedIndices.contains(index)) {
      selectedIndices.add(index);
    }
    notifyItemChanged(index);
    if (callback != null) {
      callback.onSelectionChanged(selectedIndices.size());
    }
  }

  @Override
  public boolean isIndexSelectable(int index) {
    return true;
  }

  @Override
  public int getItemCount() {
    return ALPHABET.length;
  }

  static class MainViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener, View.OnLongClickListener {

    private final TextView label;
    final RectangleView colorSquare;
    private final Listener callback;

    MainViewHolder(View itemView, Listener callback) {
      super(itemView);
      this.callback = callback;
      this.label = itemView.findViewById(R.id.label);
      this.colorSquare = itemView.findViewById(R.id.colorSquare);
      this.itemView.setOnClickListener(this);
      this.itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
      if (callback != null) {
        callback.onClick(getAdapterPosition());
      }
    }

    @Override
    public boolean onLongClick(View v) {
      if (callback != null) {
        callback.onLongClick(getAdapterPosition());
      }
      return true;
    }
  }
}
