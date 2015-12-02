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

Add the following to your module's `build.gradle` file:

```Gradle
repositories {
    // ...
    maven { url "https://jitpack.io" }
}

dependencies {
    // ...
    compile('com.github.afollestad:drag-select-recyclerview:0.2.1@aar') {
        transitive = true
    }
}
```

[![Release](https://img.shields.io/github/release/afollestad/drag-select-recyclerview.svg?label=jitpack)](https://jitpack.io/#afollestad/drag-select-recyclerview)

---

# Tutorial

1. [Introduction](https://github.com/afollestad/drag-select-recyclerview#introduction)
2. [DragSelectRecyclerView](https://github.com/afollestad/drag-select-recyclerview#dragselectrecyclerview)
3. [DragSelectRecyclerViewAdapter](https://github.com/afollestad/drag-select-recyclerview#dragselectrecyclerviewadapter)
4. [User Activation](https://github.com/afollestad/drag-select-recyclerview#user-activation)
5. [Selection Retrieval and Modification](https://github.com/afollestad/drag-select-recyclerview#selection-retrieval-and-modification)
6. [Auto Scroll](https://github.com/afollestad/drag-select-recyclerview#auto-scroll)

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
public class MainAdapter extends DragSelectRecyclerViewAdapter<MainAdapter.MainViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    // Receives View.OnClickListener set in onBindViewHolder(), tag contains index
    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int index = (int) v.getTag();
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onClick(index);
        }
    }

    // Receives View.OnLongClickListener set in onBindViewHolder(), tag contains index
    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() != null) {
            int index = (int) v.getTag();
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onLongClick(index);
            return true;
        }
        return false;
    }

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
        // Sets position + 1 to a label view
        holder.label.setText(String.format("%d", position + 1));

        if (isIndexSelected(position)) {
            // Item is selected, change it somehow 
        } else {
            // Item is not selected, reset it to a non-selected state
        }

        // Tag is used to retrieve index from the click/long-click listeners
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setOnLongClickListener(this);
    }

    @Override
    public int getItemCount() {
        return 60;
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        public final TextView label;

        public MainViewHolder(View itemView) {
            super(itemView);
            this.label = (TextView) itemView.findViewById(R.id.label);
        }
    }
}
```

You choose what to do when an item is selected (in `onBindViewHolder`). `isIndexSelected(int)` returns
true or false. The click listener implementation used here will aid in the next section.

---

# User Activation

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