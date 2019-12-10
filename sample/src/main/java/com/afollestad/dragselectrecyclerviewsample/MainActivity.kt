/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.dragselectrecyclerviewsample

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.afollestad.dragselectrecyclerview.Mode
import com.afollestad.dragselectrecyclerview.Mode.PATH
import com.afollestad.dragselectrecyclerview.Mode.RANGE
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.afollestad.recyclical.datasource.emptySelectableDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.viewholder.isSelected
import com.afollestad.recyclical.withItem
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.rxjava.observe
import com.afollestad.rxkprefs.rxkPrefs
import io.reactivex.disposables.SerialDisposable

/** @author Aidan Follestad (afollestad) */
class MainActivity : AppCompatActivity() {
  private val list by lazy { findViewById<RecyclerView>(R.id.list) }
  private val dataSource = emptySelectableDataSource().apply {
    onSelectionChange { invalidateCab() }
  }
  private val selectionModePref: Pref<Mode> by lazy {
    rxkPrefs(this).enum(
        KEY_SELECTION_MODE,
        RANGE,
        { Mode.valueOf(it) },
        { it.name }
    )
  }

  private lateinit var touchListener: DragSelectTouchListener

  private var activeCab: AttachedCab? = null
  private var selectionModeDisposable = SerialDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(findViewById<Toolbar>(R.id.main_toolbar))

    // Setup adapter and touch listener
    touchListener = DragSelectTouchListener.create(
        this,
        dataSource.asDragSelectReceiver()
    ) {
      this.mode = selectionModePref.get()
    }

    selectionModeDisposable.set(
        selectionModePref.observe()
            .filter { it != touchListener.mode }
            .subscribe {
              touchListener.mode = it
              invalidateOptionsMenu()
            }
    )

    dataSource.set(
        ALPHABET
            .dropLastWhile { it.isEmpty() }
            .map(::MainItem)
    )

    list.setup {
      withLayoutManager(GridLayoutManager(this@MainActivity, integer(R.integer.grid_width)))
      withDataSource(dataSource)

      withItem<MainItem, MainViewHolder>(R.layout.griditem_main) {
        onBind(::MainViewHolder) { index, item ->
          label.text = item.letter
          colorSquare.setBackgroundColor(COLORS[index])

          val context = itemView.context
          var foreground: Drawable? = null
          if (isSelected()) {
            foreground = ColorDrawable(context.color(R.color.grid_foreground_selected))
            label.setTextColor(context.color(R.color.grid_label_text_selected))
          } else {
            label.setTextColor(context.color(R.color.grid_label_text_normal))
          }
          colorSquare.foreground = foreground
        }
        onClick { toggleSelection() }
        onLongClick { touchListener.setIsActive(true, it) }
      }
    }
    list.addOnItemTouchListener(touchListener)

    setLightNavBarCompat()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    when (selectionModePref.get()) {
      RANGE -> menu.findItem(R.id.range_selection)
          .isChecked = true
      PATH -> menu.findItem(R.id.path_selection)
          .isChecked = true
    }
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.range_selection -> selectionModePref.set(RANGE)
      R.id.path_selection -> selectionModePref.set(PATH)
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (!activeCab.destroy()) {
      super.onBackPressed()
    }
  }

  override fun onDestroy() {
    selectionModeDisposable.dispose()
    super.onDestroy()
  }

  private fun invalidateCab() {
    if (dataSource.hasSelection()) {
      val count = dataSource.getSelectionCount()
      if (activeCab.isActive()) {
        activeCab?.title(literal = getString(R.string.cab_title_x, count))
      } else {
        activeCab = createCab(R.id.cab_stub) {
          menu(R.menu.cab)
          closeDrawable(R.drawable.ic_close)
          titleColor(literal = Color.BLACK)
          title(literal = getString(R.string.cab_title_x, count))

          onSelection {
            if (it.itemId == R.id.done) {
              val selectionString = (0 until dataSource.size())
                  .filter { index -> dataSource.isSelectedAt(index) }
                  .joinToString()
              toast("Selected letters: $selectionString")
              dataSource.deselectAll()
              true
            } else {
              false
            }
          }

          onDestroy {
            dataSource.deselectAll()
            true
          }
        }
      }
    } else {
      activeCab.destroy()
    }
  }
}

const val KEY_SELECTION_MODE = "selection-mode"

val ALPHABET = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z"
    .split(" ")
val COLORS = intArrayOf(
    Color.parseColor("#F44336"), Color.parseColor("#E91E63"),
    Color.parseColor("#9C27B0"), Color.parseColor("#673AB7"),
    Color.parseColor("#3F51B5"), Color.parseColor("#2196F3"),
    Color.parseColor("#03A9F4"), Color.parseColor("#00BCD4"),
    Color.parseColor("#009688"), Color.parseColor("#4CAF50"),
    Color.parseColor("#8BC34A"), Color.parseColor("#CDDC39"),
    Color.parseColor("#FFEB3B"), Color.parseColor("#FFC107"),
    Color.parseColor("#FF9800"), Color.parseColor("#FF5722"),
    Color.parseColor("#795548"), Color.parseColor("#9E9E9E"),
    Color.parseColor("#607D8B"), Color.parseColor("#F44336"),
    Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"),
    Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"),
    Color.parseColor("#2196F3"), Color.parseColor("#03A9F4")
)
