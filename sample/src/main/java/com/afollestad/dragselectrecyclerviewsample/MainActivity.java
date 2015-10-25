package com.afollestad.dragselectrecyclerviewsample;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.dragselectrecyclerview.DragSelectionListener;
import com.afollestad.materialcab.MaterialCab;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends AppCompatActivity implements
        MainAdapter.ClickListener, DragSelectionListener, MaterialCab.Callback {

    private MaterialCab mCab;

    private DragSelectRecyclerViewAdapter mDragSelectRecyclerViewAdapter;
    private DragSelectRecyclerView mDragSelectRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        // Setup adapter and callbacks
        final MainAdapter adapter = new MainAdapter(this);
        adapter.setSelectionListener(this);
        mDragSelectRecyclerViewAdapter = adapter;

        // Setup the RecyclerView
        final MainRecyclerView list = (MainRecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_width)));
        list.setAdapter(adapter);
        mDragSelectRecyclerView = list;

        if (savedInstanceState != null) {
            // Restore selected indices after Activity recreation
            adapter.restoreInstanceState(savedInstanceState);

            mCab = MaterialCab.restoreState(savedInstanceState, this, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        // Save selected indices
        mDragSelectRecyclerViewAdapter.saveInstanceState(outState);
        if (mCab != null) mCab.saveState(outState);
    }

    @Override
    public void onClick(int index) {
        mDragSelectRecyclerViewAdapter.toggleSelected(index);
    }

    @Override
    public void onLongClick(int index) {
        mDragSelectRecyclerView.setDragSelectActive(true, index);
    }

    @Override
    public void onDragSelectionChanged(int count) {
        if (count > 0) {
            if (mCab == null)
                mCab = new MaterialCab(this, R.id.cab_stub).start(this);
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
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mDragSelectRecyclerViewAdapter.clearSelected();
        return true;
    }
}