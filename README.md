# Drag Select Recycler View

This library allows you to implement Google Photos style multi-selection in your apps! You start
by long pressing an item in your list, then you drag your finger without letting go to select more.

![Art](https://github.com/afollestad/drag-select-recyclerview/raw/master/art/showcase.gif)

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

    compile('com.afollestad:drag-select-recyclerview:0.3.5@aar') {
        transitive = true
    }
}
```

---

# Tutorial

1. [Introduction](https://github.com/afollestad/drag-select-recyclerview#introduction)
2. [DragSelectRecyclerView](https://github.com/afollestad/drag-select-recyclerview#dragselectrecyclerview)
    1. How to create a DragSelectRecyclerView in your layout. How to set it up from code.
3. [DragSelectRecyclerViewAdapter](https://github.com/afollestad/drag-select-recyclerview#dragselectrecyclerviewadapter)
    1. An example of how adapter's should be setup. Goes over how to know which items are selected, how to prevent certain items from being selected, etc.
4. [User Activation, Initializing Drag Selection](https://github.com/afollestad/drag-select-recyclerview#user-activation-initializing-drag-selection)
    1. How drag selection mode is activated by a long press. How to maintain selected items through configuration changes, etc. 
5. [Selection Retrieval and Modification](https://github.com/afollestad/drag-select-recyclerview#selection-retrieval-and-modification)
    1. How to retrieve selected indices and modify them.
6. [Auto Scroll](https://github.com/afollestad/drag-select-recyclerview#auto-scroll)
    1. By default, this library will auto-scroll up or down if the user holds their finger at the top or bottom of the list during selection mode.

---

# Introduction

`DragSelectRecyclerView` and `DragSelectRecyclerViewAdapter` are the two main classes of this library.
They work together to provide the functionality you seek.

This library will also automatically auto scroll like Google Photos. If you drag to the top of the RecyclerView,
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

The only major difference here is what you need to pass inside of `setAdapter()`. It cannot be any
  regular `RecyclerView.Adapter`, it has to be a sub-class of `DragSelectRecyclerViewAdapter` which 
  is discussed below.

---

# DragSelectRecyclerViewAdapter

`DragSelectRecyclerViewAdapter` is a `RecyclerView.Adapter` sub-class that `DragSelectRecyclerView` is
able to communicate with. It keeps track of selected indices – and it allows you to change them, clear them,
listen for changes, and check if a certain index is selected.

A basic adapter implementation looks like this:

```java
public class MainAdapter extends DragSelectRecyclerViewAdapter<MainAdapter.MainViewHolder> {

    public interface ClickListener {
        void onClick(int index);

        void onLongClick(int index);
    }

    private final ClickListener mCallback;

    // Constructor takes click listener callback
    protected MainAdapter(ClickListener callback) {
        super();
        mCallback = callback;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.griditem_main, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!
    
        // Sets position + 1 to a label view
        holder.label.setText(String.format("%d", position + 1));

        if (isIndexSelected(position)) {
            // Item is selected, change it somehow 
        } else {
            // Item is not selected, reset it to a non-selected state
        }
    }

    @Override
    protected boolean isIndexSelectable(int index) {
        // This method is OPTIONAL, returning false will prevent the item at the specified index from being selected.
        // Both initial selection, and drag selection.
        return true;
    }

    @Override
    public int getItemCount() {
        return 60;
    }

    public class MainViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        public final TextView label;

        public MainViewHolder(View itemView) {
            super(itemView);
            this.label = (TextView) itemView.findViewById(R.id.label);
            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
        }
        
        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onClick(getAdapterPosition());
        }
    
        @Override
        public boolean onLongClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onLongClick(getAdapterPosition());
            return true;
        }
    }
}
```

You choose what to do when an item is selected (in `onBindViewHolder`). `isIndexSelected(int)` returns
true or false. The click listener implementation used here will aid in the next section.

---

# User Activation, Initializing Drag Selection

The library won't start selection mode unless you tell it to. You want the user to be able to active it.
The click listener implementation setup in the adapter above will help with this.

```java
public class MainActivity extends AppCompatActivity implements
        MainAdapter.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener {

    private DragSelectRecyclerView mList;
    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup adapter and callbacks
        mAdapter = new MainAdapter(this);
        // Receives selection updates, recommended to set before restoreInstanceState() so initial reselection is received
        mAdapter.setSelectionListener(this);
        // Restore selected indices after Activity recreation
        mAdapter.restoreInstanceState(savedInstanceState);

        // Setup the RecyclerView
        mList = (DragSelectRecyclerView) findViewById(R.id.list);
        mList.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_width)));
        mList.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        // Save selected indices to be restored after recreation
        mAdapter.saveInstanceState(outState);
    }

    @Override
    public void onClick(int index) {
        // Single click will select or deselect an item
        mAdapter.toggleSelected(index);
    }

    @Override
    public void onLongClick(int index) {
        // Long click initializes drag selection, and selects the initial item
        mList.setDragSelectActive(true, index);
    }

    @Override
    public void onDragSelectionChanged(int count) {
        // TODO Selection was changed, updating an indicator, e.g. a Toolbar or contextual action bar
    }
}
```

---

# Selection Retrieval and Modification

`DragSelectRecyclerViewAdapter` contains many methods to help you!

```java
// Clears all selected indices
adapter.clearSelected();

// Sets an index as selected (true) or unselected (false);
adapter.setSelected(index, true);

// If an index is selected, unselect it. Otherwise select it. Returns new selection state.
boolean selectedNow = adapter.toggleSelected(index);

// Gets the number of selected indices
int count = adapter.getSelectedCount();

// Gets all selected indices
Integer[] selectedItems = adapter.getSelectedIndices();

// Checks if an index is selected, useful in adapter subclass
boolean selected = adapter.isIndexSelected(index);

// Sets a listener that's notified of selection changes, used in the section above
adapter.setSelectionListener(listener);

// Used in section above, saves selected indices to Bundle
adapter.saveInstanceState(outState);

// Used in section above, restores selected indices from Bundle
adapter.restoreInstanceState(inState);
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
