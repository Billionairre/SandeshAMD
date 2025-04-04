// Generated by view binder compiler. Do not edit!
package edu.sandesh.mealmate.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.google.android.material.chip.Chip;
import edu.sandesh.mealmate.R;
import java.lang.NullPointerException;
import java.lang.Override;

public final class DelegateItemMealBinding implements ViewBinding {
  @NonNull
  private final Chip rootView;

  @NonNull
  public final Chip chipMeal;

  private DelegateItemMealBinding(@NonNull Chip rootView, @NonNull Chip chipMeal) {
    this.rootView = rootView;
    this.chipMeal = chipMeal;
  }

  @Override
  @NonNull
  public Chip getRoot() {
    return rootView;
  }

  @NonNull
  public static DelegateItemMealBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static DelegateItemMealBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.delegate_item_meal, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static DelegateItemMealBinding bind(@NonNull View rootView) {
    if (rootView == null) {
      throw new NullPointerException("rootView");
    }

    Chip chipMeal = (Chip) rootView;

    return new DelegateItemMealBinding((Chip) rootView, chipMeal);
  }
}
