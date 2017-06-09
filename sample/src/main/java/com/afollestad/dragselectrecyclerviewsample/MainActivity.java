package com.afollestad.dragselectrecyclerviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.materialcab.MaterialCab;

/** @author Aidan Follestad (afollestad) */
public class MainActivity extends AppCompatActivity
    implements MainAdapter.ClickListener,
        DragSelectRecyclerViewAdapter.SelectionListener,
        MaterialCab.Callback {

  private DragSelectRecyclerView listView;
  private MainAdapter adapter;
  private MaterialCab cab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

    // Setup adapter and callbacks
    adapter = new MainAdapter(this);
    // Receives selection updates, recommended to set before restoreInstanceState() so initial reselection is received
    adapter.setSelectionListener(this);
    // Restore selected indices after Activity recreation
    adapter.restoreInstanceState(savedInstanceState);

    // Setup the RecyclerView
    listView = (DragSelectRecyclerView) findViewById(R.id.list);
    listView.setLayoutManager(
        new GridLayoutManager(this, getResources().getInteger(R.integer.grid_width)));
    listView.setAdapter(adapter);

    cab = MaterialCab.restoreState(savedInstanceState, this, this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // Save selected indices
    adapter.saveInstanceState(outState);
    if (cab != null) cab.saveState(outState);
  }

  @Override
  public void onClick(int index) {
    adapter.toggleSelected(index);
  }

  @Override
  public void onLongClick(int index) {
    listView.setDragSelectActive(true, index);
  }

  @Override
  public void onDragSelectionChanged(int count) {
    if (count > 0) {
      if (cab == null) {
        cab =
            new MaterialCab(this, R.id.cab_stub)
                .setMenu(R.menu.cab)
                .setCloseDrawableRes(R.drawable.ic_close)
                .start(this);
      }
      cab.setTitleRes(R.string.cab_title_x, count);
    } else if (cab != null && cab.isActive()) {
      cab.reset().finish();
      cab = null;
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
      for (Integer index : adapter.getSelectedIndices()) {
        if (traverse > 0) sb.append(", ");
        sb.append(adapter.getItem(index));
        traverse++;
      }
      Toast.makeText(
              this,
              String.format("Selected letters (%d): %s", adapter.getSelectedCount(), sb.toString()),
              Toast.LENGTH_LONG)
          .show();
      adapter.clearSelected();
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    if (adapter.getSelectedCount() > 0) {
      adapter.clearSelected();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCabFinished(MaterialCab cab) {
    adapter.clearSelected();
    return true;
  }
}
