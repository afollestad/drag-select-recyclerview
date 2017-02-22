package com.afollestad.dragselectrecyclerviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialcab.MaterialCab;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends AppCompatActivity implements
        MainAdapter.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, MaterialCab.Callback {
    private boolean demoEmptyView = false; //toggle to demo empty view
    private DragSelectRecyclerView mList;
    private MainAdapter mAdapter;
    private MaterialCab mCab;
    private final static String[] ALPHABET = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split(" ");
    private final static String[] NULL_ARRAY = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        // Setup adapter and callbacks
        if (demoEmptyView) //optional feature to add empty view when input list is empty
            mAdapter = new MainAdapter(this, NULL_ARRAY);
        else
            mAdapter = new MainAdapter(this, ALPHABET);
        // Receives selection updates, recommended to set before restoreInstanceState() so initial reselection is received
        mAdapter.setSelectionListener(this);
        // Restore selected indices after Activity recreation
        mAdapter.restoreInstanceState(savedInstanceState);
        // Setup the RecyclerView
        mList = (DragSelectRecyclerView) findViewById(R.id.list);
        mList.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_width)));
        mList.setAdapter(mAdapter);

        if (demoEmptyView) {
            customizeEmtpyView();
        }
        mCab = MaterialCab.restoreState(savedInstanceState, this, this);
    }

    public void customizeEmtpyView() {
        //custom empty view to be insert when collection is empty
        RelativeLayout emptyViewLayout = (RelativeLayout) findViewById(R.id.empty_view);
        TextView mText = (TextView) findViewById(R.id.empty_view_txt);
        ImageView mImageView = (ImageView) findViewById(R.id.empty_view_img);
        mText.setText("This is empty view");
        mImageView.setImageResource(android.R.drawable.ic_dialog_dialer);
        mList.setEmptyView(emptyViewLayout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save selected indices
        mAdapter.saveInstanceState(outState);
        if (mCab != null) mCab.saveState(outState);
    }

    @Override
    public void onClick(int index) {
        mAdapter.toggleSelected(index);
    }

    @Override
    public void onLongClick(int index) {
        mList.setDragSelectActive(true, index);
    }

    @Override
    public void onDragSelectionChanged(int count) {
        if (count > 0) {
            if (mCab == null) {
                mCab = new MaterialCab(this, R.id.cab_stub)
                        .setMenu(R.menu.cab)
                        .setCloseDrawableRes(R.drawable.ic_close)
                        .start(this);
            }
            mCab.setTitleRes(R.string.cab_title_x, count);
        } else if (mCab != null && mCab.isActive()) {
            mCab.reset().finish();
            mCab = null;
        }
    }

    // Material CAB Callbacks

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            StringBuilder sb = new StringBuilder();
            int traverse = 0;
            for (Integer index : mAdapter.getSelectedIndices()) {
                if (traverse > 0) sb.append(", ");
                sb.append(mAdapter.getItem(index));
                traverse++;
            }
            Toast.makeText(this,
                    String.format("Selected letters (%d): %s", mAdapter.getSelectedCount(), sb.toString()),
                    Toast.LENGTH_LONG).show();
            mAdapter.clearSelected();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.getSelectedCount() > 0)
            mAdapter.clearSelected();
        else super.onBackPressed();
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mAdapter.clearSelected();
        return true;
    }
}