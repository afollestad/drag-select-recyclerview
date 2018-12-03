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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import kotlinx.android.synthetic.main.griditem_main.view.colorSquare
import kotlinx.android.synthetic.main.griditem_main.view.label

/** @author Aidan Follestad (afollestad) */
class MainAdapter(private val callback: Listener?) :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>(),
    DragSelectReceiver {

  val selectedIndices: MutableList<Int> = mutableListOf()

  interface Listener {
    fun onClick(index: Int)

    fun onLongClick(index: Int)

    fun onSelectionChanged(count: Int)
  }

  operator fun get(index: Int) = ALPHABET[index]

  fun toggleSelected(index: Int) {
    if (selectedIndices.contains(index)) {
      selectedIndices.remove(index)
    } else {
      selectedIndices.add(index)
    }
    notifyItemChanged(index)
    callback?.onSelectionChanged(selectedIndices.size)
  }

  fun clearSelected() {
    if (selectedIndices.isEmpty()) {
      return
    }
    selectedIndices.clear()
    notifyDataSetChanged()
    callback?.onSelectionChanged(0)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): MainViewHolder {
    val v = LayoutInflater.from(parent.context)
        .inflate(R.layout.griditem_main, parent, false)
    return MainViewHolder(v, callback)
  }

  override fun onBindViewHolder(
    holder: MainViewHolder,
    position: Int
  ) {
    holder.itemView.label.text = this[position]

    val d: Drawable?
    val c = holder.itemView.context

    if (selectedIndices.contains(position)) {
      d = ColorDrawable(c.color(R.color.grid_foreground_selected))
      holder.itemView.label.setTextColor(
          c.color(R.color.grid_label_text_selected)
      )
    } else {
      d = null
      holder.itemView.label.setTextColor(c.color(R.color.grid_label_text_normal))
    }

    (holder.itemView.colorSquare as FrameLayout).foreground = d
    holder.itemView.colorSquare.setBackgroundColor(COLORS[position])
  }

  override fun setSelected(
    index: Int,
    selected: Boolean
  ) {
    Log.d("MainAdapter", "setSelected($index, $selected)")
    if (!selected) {
      selectedIndices.remove(index)
    } else if (!selectedIndices.contains(index)) {
      selectedIndices.add(index)
    }
    notifyItemChanged(index)
    callback?.onSelectionChanged(selectedIndices.size)
  }

  override fun isSelected(index: Int) = selectedIndices.contains(index)

  override fun isIndexSelectable(index: Int) = true

  override fun getItemCount() = ALPHABET.size

  class MainViewHolder(
    itemView: View,
    private val callback: Listener?
  ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    init {
      this.itemView.setOnClickListener(this)
      this.itemView.setOnLongClickListener(this)
    }

    override fun onClick(v: View) {
      callback?.onClick(adapterPosition)
    }

    override fun onLongClick(v: View): Boolean {
      callback?.onLongClick(adapterPosition)
      return true
    }
  }

  companion object {

    private val ALPHABET =
      "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split(" ")
          .dropLastWhile { it.isEmpty() }
          .toTypedArray()

    private val COLORS = intArrayOf(
        Color.parseColor("#F44336"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"),
        Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"), Color.parseColor("#2196F3"),
        Color.parseColor("#03A9F4"), Color.parseColor("#00BCD4"), Color.parseColor("#009688"),
        Color.parseColor("#4CAF50"), Color.parseColor("#8BC34A"), Color.parseColor("#CDDC39"),
        Color.parseColor("#FFEB3B"), Color.parseColor("#FFC107"), Color.parseColor("#FF9800"),
        Color.parseColor("#FF5722"), Color.parseColor("#795548"), Color.parseColor("#9E9E9E"),
        Color.parseColor("#607D8B"), Color.parseColor("#F44336"), Color.parseColor("#E91E63"),
        Color.parseColor("#9C27B0"), Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"),
        Color.parseColor("#2196F3"), Color.parseColor("#03A9F4")
    )
  }
}
