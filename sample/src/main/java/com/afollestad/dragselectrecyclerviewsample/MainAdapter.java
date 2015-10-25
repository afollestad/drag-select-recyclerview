package com.afollestad.dragselectrecyclerviewsample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainAdapter extends DragSelectRecyclerViewAdapter<MainAdapter.MainViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    private final static String[] ALPHABET = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split(" ");
    private final static int[] COLORS = new int[]{
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

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int index = (int) v.getTag();
            if (mCallback != null) mCallback.onClick(index);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() != null) {
            int index = (int) v.getTag();
            if (mCallback != null) mCallback.onLongClick(index);
            return true;
        }
        return false;
    }

    public interface ClickListener {
        void onClick(int index);

        void onLongClick(int index);
    }

    private final ClickListener mCallback;

    protected MainAdapter(ClickListener callback) {
        super();
        mCallback = callback;
    }

    public String getItem(int index) {
        return ALPHABET[index];
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.griditem_main, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        holder.label.setText(getItem(position));

        final Drawable d;
        final Context c = holder.itemView.getContext();

        if (isIndexSelected(position)) {
            d = new ColorDrawable(ContextCompat.getColor(c, R.color.grid_foreground_selected));
            holder.label.setTextColor(ContextCompat.getColor(c, R.color.grid_label_text_selected));
        } else {
            d = null;
            holder.label.setTextColor(ContextCompat.getColor(c, R.color.grid_label_text_normal));
        }

        //noinspection RedundantCast
        ((FrameLayout) holder.colorSquare).setForeground(d);
        holder.colorSquare.setBackgroundColor(COLORS[position]);

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public int getItemCount() {
        return ALPHABET.length;
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        public final TextView label;
        public final RectangleView colorSquare;

        public MainViewHolder(View itemView) {
            super(itemView);
            this.label = (TextView) itemView.findViewById(R.id.label);
            this.colorSquare = (RectangleView) itemView.findViewById(R.id.colorSquare);
        }
    }
}