// Generated by view binder compiler. Do not edit!
package edu.sandesh.mealmate.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import edu.sandesh.mealmate.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentStoreSelectionBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextInputEditText edittextSearchStores;

  @NonNull
  public final TextInputEditText edittextStoreLocation;

  @NonNull
  public final TextInputEditText edittextStoreName;

  @NonNull
  public final LinearLayout layoutManualEntry;

  @NonNull
  public final LinearLayout layoutStoreList;

  @NonNull
  public final RadioButton radioManualEntry;

  @NonNull
  public final RadioButton radioSelectFromServer;

  @NonNull
  public final RadioGroup radiogroupStoreMode;

  @NonNull
  public final RecyclerView recyclerviewStores;

  @NonNull
  public final TextInputLayout storeLocationLayout;

  @NonNull
  public final TextInputLayout storeNameLayout;

  @NonNull
  public final ViewSwitcher viewSwitcher;

  private FragmentStoreSelectionBinding(@NonNull LinearLayout rootView,
      @NonNull TextInputEditText edittextSearchStores,
      @NonNull TextInputEditText edittextStoreLocation,
      @NonNull TextInputEditText edittextStoreName, @NonNull LinearLayout layoutManualEntry,
      @NonNull LinearLayout layoutStoreList, @NonNull RadioButton radioManualEntry,
      @NonNull RadioButton radioSelectFromServer, @NonNull RadioGroup radiogroupStoreMode,
      @NonNull RecyclerView recyclerviewStores, @NonNull TextInputLayout storeLocationLayout,
      @NonNull TextInputLayout storeNameLayout, @NonNull ViewSwitcher viewSwitcher) {
    this.rootView = rootView;
    this.edittextSearchStores = edittextSearchStores;
    this.edittextStoreLocation = edittextStoreLocation;
    this.edittextStoreName = edittextStoreName;
    this.layoutManualEntry = layoutManualEntry;
    this.layoutStoreList = layoutStoreList;
    this.radioManualEntry = radioManualEntry;
    this.radioSelectFromServer = radioSelectFromServer;
    this.radiogroupStoreMode = radiogroupStoreMode;
    this.recyclerviewStores = recyclerviewStores;
    this.storeLocationLayout = storeLocationLayout;
    this.storeNameLayout = storeNameLayout;
    this.viewSwitcher = viewSwitcher;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentStoreSelectionBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentStoreSelectionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_store_selection, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentStoreSelectionBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.edittext_search_stores;
      TextInputEditText edittextSearchStores = ViewBindings.findChildViewById(rootView, id);
      if (edittextSearchStores == null) {
        break missingId;
      }

      id = R.id.edittext_store_location;
      TextInputEditText edittextStoreLocation = ViewBindings.findChildViewById(rootView, id);
      if (edittextStoreLocation == null) {
        break missingId;
      }

      id = R.id.edittext_store_name;
      TextInputEditText edittextStoreName = ViewBindings.findChildViewById(rootView, id);
      if (edittextStoreName == null) {
        break missingId;
      }

      id = R.id.layout_manual_entry;
      LinearLayout layoutManualEntry = ViewBindings.findChildViewById(rootView, id);
      if (layoutManualEntry == null) {
        break missingId;
      }

      id = R.id.layout_store_list;
      LinearLayout layoutStoreList = ViewBindings.findChildViewById(rootView, id);
      if (layoutStoreList == null) {
        break missingId;
      }

      id = R.id.radio_manual_entry;
      RadioButton radioManualEntry = ViewBindings.findChildViewById(rootView, id);
      if (radioManualEntry == null) {
        break missingId;
      }

      id = R.id.radio_select_from_server;
      RadioButton radioSelectFromServer = ViewBindings.findChildViewById(rootView, id);
      if (radioSelectFromServer == null) {
        break missingId;
      }

      id = R.id.radiogroup_store_mode;
      RadioGroup radiogroupStoreMode = ViewBindings.findChildViewById(rootView, id);
      if (radiogroupStoreMode == null) {
        break missingId;
      }

      id = R.id.recyclerview_stores;
      RecyclerView recyclerviewStores = ViewBindings.findChildViewById(rootView, id);
      if (recyclerviewStores == null) {
        break missingId;
      }

      id = R.id.store_location_layout;
      TextInputLayout storeLocationLayout = ViewBindings.findChildViewById(rootView, id);
      if (storeLocationLayout == null) {
        break missingId;
      }

      id = R.id.store_name_layout;
      TextInputLayout storeNameLayout = ViewBindings.findChildViewById(rootView, id);
      if (storeNameLayout == null) {
        break missingId;
      }

      id = R.id.view_switcher;
      ViewSwitcher viewSwitcher = ViewBindings.findChildViewById(rootView, id);
      if (viewSwitcher == null) {
        break missingId;
      }

      return new FragmentStoreSelectionBinding((LinearLayout) rootView, edittextSearchStores,
          edittextStoreLocation, edittextStoreName, layoutManualEntry, layoutStoreList,
          radioManualEntry, radioSelectFromServer, radiogroupStoreMode, recyclerviewStores,
          storeLocationLayout, storeNameLayout, viewSwitcher);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
