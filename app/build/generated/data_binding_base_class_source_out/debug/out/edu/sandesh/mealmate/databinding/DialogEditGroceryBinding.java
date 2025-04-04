// Generated by view binder compiler. Do not edit!
package edu.sandesh.mealmate.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.textfield.TextInputEditText;
import edu.sandesh.mealmate.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class DialogEditGroceryBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextInputEditText editTextName;

  @NonNull
  public final Spinner spinnerCategory;

  private DialogEditGroceryBinding(@NonNull LinearLayout rootView,
      @NonNull TextInputEditText editTextName, @NonNull Spinner spinnerCategory) {
    this.rootView = rootView;
    this.editTextName = editTextName;
    this.spinnerCategory = spinnerCategory;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static DialogEditGroceryBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static DialogEditGroceryBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.dialog_edit_grocery, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static DialogEditGroceryBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.editTextName;
      TextInputEditText editTextName = ViewBindings.findChildViewById(rootView, id);
      if (editTextName == null) {
        break missingId;
      }

      id = R.id.spinnerCategory;
      Spinner spinnerCategory = ViewBindings.findChildViewById(rootView, id);
      if (spinnerCategory == null) {
        break missingId;
      }

      return new DialogEditGroceryBinding((LinearLayout) rootView, editTextName, spinnerCategory);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
