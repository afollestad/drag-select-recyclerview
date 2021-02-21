# Drag Select Recycler View

[ ![Maven Central](https://img.shields.io/maven-central/v/com.afollestad/drag-select-recyclerview?style=flat&label=Maven+Central) ](https://repo1.maven.org/maven2/com/afollestad/drag-select-recyclerview)
[![Android CI](https://github.com/afollestad/drag-select-recyclerview/workflows/Android%20CI/badge.svg)](https://github.com/afollestad/drag-select-recyclerview/actions?query=workflow%3A%22Android+CI%22)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

This library allows you to implement Google Photos style multi-selection in your apps! You start
by long pressing an item in your list, then you drag your finger without letting go to select more.

![Range Mode GIF](https://github.com/afollestad/drag-select-recyclerview/raw/master/art/range.gif)

# Sample

You can [download a sample APK](https://github.com/afollestad/drag-select-recyclerview/raw/master/sample/sample.apk).

---

# Gradle Dependency

The Gradle dependency is available via [jCenter](https://bintray.com/drummer-aidan/maven/material-camera/view).
jCenter is the default Maven repository used by Android Studio.

## Dependency

Add the following to your module's `build.gradle` file:

```Gradle
dependencies {

  implementation 'com.afollestad:drag-select-recyclerview:2.4.0'
}
```

---

# Introduction

`DragSelectTouchListener` is the main class of this library.

This library will handle drag interception and auto scroll logic - if you drag to the top of the RecyclerView,
the list will scroll up, and vice versa.

---

# DragSelectTouchListener

### Basics

`DragSelectTouchListener` attaches to your RecyclerViews. It intercepts touch events
when it's active, and reports to a receiver which handles updating UI

```kotlin
val receiver: DragSelectReceiver = // ...
val touchListener = DragSelectTouchListener.create(context, receiver)
```

### Configuration

There are a few things that you can configure, mainly around auto scroll.

```kotlin
DragSelectTouchListener.create(context, adapter) {
  // Configure the auto-scroll hotspot
  hotspotHeight = resources.getDimensionPixelSize(R.dimen.default_56dp)
  hotspotOffsetTop = 0 // default
  hotspotOffsetBottom = 0 // default
  
  // Listen for auto scroll start/end
  autoScrollListener = { isScrolling -> } 

  // Or instead of the above...
  disableAutoScroll()
  
  // The drag selection mode, RANGE is the default
  mode = RANGE
}
```

The auto-scroll hotspot is a invisible section at the top and bottom of your
RecyclerView, when your finger is in one of those sections, auto scroll is
triggered and the list will move up or down until you lift your finger.

If you use `PATH` as the mode instead of `RANGE`, the behavior is a bit different:

![Path Mode GIF](https://github.com/afollestad/drag-select-recyclerview/raw/master/art/path.gif)

Compare it to the GIF at the top.

---

# Interaction

A receiver looks like this:

```kotlin
class MyReceiver : DragSelectReceiver {

  override fun setSelected(index: Int, selected: Boolean) {
    // do something to mark this index as selected/unselected
    if(selected && !selectedIndices.contains(index)) {
      selectedIndices.add(index)
    } else if(!selected) {
      selectedIndices.remove(index)
    }
  }
  
  override fun isSelected(index: Int): Boolean {
    // return true if this index is currently selected
    return selectedItems.contains(index)
  }
  
  override fun isIndexSelectable(index: Int): Boolean {
    // if you return false, this index can't be used with setIsActive()
    return true
  }

  override fun getItemCount(): Int {
    // return size of your data set
    return 0
  }
}
```

In the sample project, our adapter is also our receiver.

To start drag selection, you use `setIsActive`, which should be triggered
from user input such as a long press on a list item.

```kotlin
val recyclerView: RecyclerView = // ...
val receiver: DragSelectReceiver = // ...

val touchListener = DragSelectTouchListener.create(context, receiver)
recyclerView.addOnItemTouchListener(touchListener) // important!!

// true for active = true, 0 is the initial selected index
touchListener.setIsActive(true, 0)
````
