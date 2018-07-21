package com.afollestad.dragselectrecyclerviewsample;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.materialcab.MaterialCab;

/** @author Aidan Follestad (afollestad) */
public class MainActivity extends AppCompatActivity
    implements MainAdapter.Listener, MaterialCab.Callback {

  private DragSelectRecyclerView listView;
  private MainAdapter adapter;
  private MaterialCab cab;

  @SuppressLint("InlinedApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

    // Setup adapter and callbacks
    adapter = new MainAdapter(this);

    // Setup the RecyclerView
    listView = (DragSelectRecyclerView) findViewById(R.id.list);
    listView.setLayoutManager(
        new GridLayoutManager(this, getResources().getInteger(R.integer.grid_width)));
    listView.setAdapter(adapter);

    cab = MaterialCab.restoreState(savedInstanceState, this, this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int flags = getWindow().getDecorView().getSystemUiVisibility();
      flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
      getWindow().getDecorView().setSystemUiVisibility(flags);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (cab != null) {
      cab.saveState(outState);
    }
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
  public void onSelectionChanged(int count) {
    if (count > 0) {
      if (cab == null) {
        cab =
            new MaterialCab(this, R.id.cab_stub)
                .setMenu(R.menu.cab)
                .setCloseDrawableRes(R.drawable.ic_close)
                .start(this);
        cab.getToolbar().setTitleTextColor(Color.BLACK);
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

  @SuppressLint("DefaultLocale")
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
              String.format(
                  "Selected letters (%d): %s", adapter.getSelectedIndices().size(), sb.toString()),
              Toast.LENGTH_LONG)
          .show();
      adapter.clearSelected();
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    if (!adapter.getSelectedIndices().isEmpty()) {
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
