# Drag Select Recycler View

This library allows you to implement Google Photos style multi-selection in your apps! You start
by long pressing an item in your list, then you drag your finger without letting go to select more.

![Art](https://github.com/afollestad/drag-select-recyclerview/raw/master/art/showcase2.gif)

# Sample

You can [download a sample APK](https://github.com/afollestad/drag-select-recyclerview/raw/master/sample/sample.apk) or 
[view it on Google Play](https://play.google.com/store/apps/details?id=com.afollestad.dragselectrecyclerviewsample)!

<a href="https://play.google.com/store/apps/details?id=com.afollestad.dragselectrecyclerviewsample">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

---

# Gradle Dependency

[ ![jCenter](https://api.bintray.com/packages/drummer-aidan/maven/drag-select-recyclerview/images/download.svg) ](https://bintray.com/drummer-aidan/maven/drag-select-recyclerview/_latestVersion)
[![Build Status](https://travis-ci.org/afollestad/drag-select-recyclerview.svg)](https://travis-ci.org/afollestad/drag-select-recyclerview)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

The Gradle dependency is available via [jCenter](https://bintray.com/drummer-aidan/maven/material-camera/view).
jCenter is the default Maven repository used by Android Studio.

## Dependency

Add the following to your module's `build.gradle` file:

```Gradle
dependencies {
    // ... other dependencies
    implementation 'com.afollestad:drag-select-recyclerview:1.0.0'
}
```

---

# Tutorial

1. [Introduction](https://github.com/afollestad/drag-select-recyclerview#introduction)
2. [DragSelectRecyclerView](https://github.com/afollestad/drag-select-recyclerview#dragselectrecyclerview)
    1. How to create a DragSelectRecyclerView in your layout. How to set it up from code.
3. [Adapter Implementation](https://github.com/afollestad/drag-select-recyclerview#adapter-implementation)
    1. An example of how adapter's should be setup.
4. [User Activation, Initializing Drag Selection](https://github.com/afollestad/drag-select-recyclerview#user-activation-initializing-drag-selection)
    1. How drag selection mode is activated by a long press. How to maintain selected items through configuration changes, etc. 
6. [Auto Scroll](https://github.com/afollestad/drag-select-recyclerview#auto-scroll)
    1. By default, this library will auto-scroll up or down if the user holds their finger at the top or bottom of the list during selection mode.

---

# Introduction

`DragSelectRecyclerView` is the main class of this library.

This library will handle drag interception and auto scroll logic - if you drag to the top of the RecyclerView,
the list will scroll up, and vice versa.

---

# DragSelectRecyclerView

`DragSelectRecyclerView` replaces the regular `RecyclerView` in your layouts. It intercepts touch events
when you tell if selection mode is active, and automatically reports to your adapter.

```xml
<com.afollestad.dragselectrecyclerview.DragSelectRecyclerView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="vertical" />
```

Setup is basically the same as it would be for a regular `RecyclerView`. You just set a `LayoutManager`
and `RecyclerView.Adapter` to it:

```Java
DragSelectRecyclerView list = (DragSelectRecyclerView) findViewById(R.id.list);
list.setLayoutManager(new GridLayoutManager(this, 3));
list.setAdapter(adapter);
```

---

# Adapter Implementation

You use regular `RecyclerView.Adapter`'s with the `DragSelectRecyclerView`. However, it **has to 
implement the `IDragSelectAdapter` interface**:

```java
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder>
      implements IDragSelectAdapter {

  @Override
  public void setSelected(int index, boolean selected) {
    // 1. Make this index as selected in your implementation.
    // 2. Tell the RecyclerView.Adapter to render this item's changes.
    notifyItemChanged(index);
  }
  
  @Override
  public boolean isIndexSelectable(int index) {
    // Return false if you don't want this position to be selectable.
    // Useful for items like section headers.
    return true;
  }
 
  // The rest of your regular adapter overrides
}
```

**Checkout the sample project for an in-depth example.**

---

# User Activation, Initializing Drag Selection

The library won't start selection mode unless you tell it to. You want the user to be able to active it.
The click listener implementation setup in the adapter above will help with this.

```java
public class MainActivity extends AppCompatActivity implements
      MainAdapter.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener {

  private DragSelectRecyclerView listView;
  private MainAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Setup adapter and callbacks
    adapter = new MainAdapter(this);

    // Setup the recycler view
    listView = (DragSelectRecyclerView) findViewById(R.id.list);
    listView.setLayoutManager(
        new GridLayoutManager(this, 
            getResources().getInteger(R.integer.grid_width)));
    listView.setAdapter(adapter);
  }

  /** 
   * See the adapter in the sample project for a click listener implementation. Click listeners 
   * aren't provided by this library.
   */
  @Override
  public void onClick(int index) {
    // Single click will select or deselect an item
    adapter.toggleSelected(index);
  }

  /** 
   * See the adapter in the sample project for a click listener implementation. Click listeners 
   * aren't provided by this library.
   */
  @Override
  public void onLongClick(int index) {
    // Initialize drag selection -- also selects the initial item
    listView.setDragSelectActive(true, index);
  }
}
```

---

# Auto Scroll

By default, this library will auto scroll. During drag selection, moving your finger to the top
of the list will scroll up. Moving your finger to the bottom of the list will scroll down.

At the start of the activation point at the top or bottom, the list will scroll slowly. The further
you move into the activation area, the faster it will scroll.

You can disable auto scroll, or change the activation hotspot from your layout XML:

```xml
<com.afollestad.dragselectrecyclerview.DragSelectRecyclerView
    android:id="@+id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="vertical"
    app:dsrv_autoScrollEnabled="true"
    app:dsrv_autoScrollHotspotHeight="56dp" />
```

56dp is the default hotspot height, you can raise or lower it if necessary. Smaller hotspots will
scroll quickly since there's not much room for velocity change.
