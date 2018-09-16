/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.dragselectrecyclerviewsample

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.afollestad.dragselectrecyclerview.Mode.PATH
import com.afollestad.dragselectrecyclerview.Mode.RANGE
import com.afollestad.materialcab.MaterialCab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.activity_main.main_toolbar

/** @author Aidan Follestad (afollestad) */
class MainActivity : AppCompatActivity(), MainAdapter.Listener {

  companion object {
    const val KEY_PREFS = "drag-select-sample"
    const val KEY_SELECTION_MODE = "selection-mode"
  }

  private lateinit var adapter: MainAdapter
  private lateinit var touchListener: DragSelectTouchListener
  private lateinit var prefs: SharedPreferences

  @SuppressLint("InlinedApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(main_toolbar)
    prefs = prefs(KEY_PREFS)

    // Setup adapter and touch listener
    adapter = MainAdapter(this)
    touchListener = DragSelectTouchListener.create(this, adapter) {
      this.mode = prefs.getEnum(KEY_SELECTION_MODE, RANGE)
    }

    // Setup the RecyclerView
    list.layoutManager = GridLayoutManager(this, integer(R.integer.grid_width))
    list.adapter = adapter
    list.addOnItemTouchListener(touchListener)

    MaterialCab.tryRestore(this, savedInstanceState)
    setLightNavBarCompat()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)

    val mode = prefs.getEnum(KEY_SELECTION_MODE, RANGE)
    when (mode) {
      RANGE -> menu.findItem(R.id.range_selection)
          .isChecked = true
      PATH -> menu.findItem(R.id.path_selection)
          .isChecked = true
    }

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.range_selection -> {
        prefs.edit { putEnum(KEY_SELECTION_MODE, RANGE) }
        touchListener.mode = RANGE
        invalidateOptionsMenu()
      }
      R.id.path_selection -> {
        prefs.edit { putEnum(KEY_SELECTION_MODE, PATH) }
        touchListener.mode = PATH
        invalidateOptionsMenu()
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    MaterialCab.saveState(outState)
  }

  override fun onClick(index: Int) {
    adapter.toggleSelected(index)
  }

  override fun onLongClick(index: Int) {
    touchListener.setIsActive(true, index)
  }

  override fun onSelectionChanged(count: Int) {
    if (count > 0) {
      MaterialCab.attach(this, R.id.cab_stub) {
        menuRes = R.menu.cab
        closeDrawableRes = R.drawable.ic_close
        titleColor = Color.BLACK
        title = getString(R.string.cab_title_x, count)

        onSelection {
          if (it.itemId == R.id.done) {
            val sb = StringBuilder()
            for ((traverse, index) in adapter.selectedIndices.withIndex()) {
              if (traverse > 0) sb.append(", ")
              sb.append(adapter[index])
            }
            Toast.makeText(
                this@MainActivity,
                "Selected letters (${adapter.selectedIndices.size}): $sb",
                Toast.LENGTH_LONG
            )
                .show()
            adapter.clearSelected()
            true
          } else {
            false
          }
        }

        onDestroy {
          adapter.clearSelected()
          true
        }
      }
    } else {
      MaterialCab.destroy()
    }
  }

  override fun onBackPressed() {
    if (!MaterialCab.destroy()) {
      super.onBackPressed()
    }
  }
}
