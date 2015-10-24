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
import com.afollestad.materialcab.MaterialCab;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends AppCompatActivity implements
        MainAdapter.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener, MaterialCab.Callback {

    private DragSelectRecyclerView mList;
    private MainAdapter mAdapter;
    private MaterialCab mCab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        // Setup adapter and callbacks
        mAdapter = new MainAdapter(this);
        mAdapter.setSelectionListener(this);
        // Restore selected indices after Activity recreation
        mAdapter.restoreInstanceState(savedInstanceState);

        // Setup the RecyclerView
        mList = (DragSelectRecyclerView) findViewById(R.id.list);
        mList.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_width)));
        mList.setAdapter(mAdapter);

        mCab = MaterialCab.restoreState(savedInstanceState, this, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
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
        mAdapter.clearSelected();
        return true;
    }
}