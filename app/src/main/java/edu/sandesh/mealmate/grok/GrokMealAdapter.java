package edu.sandesh.mealmate.grok;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.sandesh.mealmate.R;

class GrokMealAdapter extends RecyclerView.Adapter<GrokMealAdapter.ViewHolder> {
    List<String> categories;
    Set<String> selectedCategories = new HashSet<>();
    Runnable onSelectionChanged;

    GrokMealAdapter(List<String> categories, Runnable onSelectionChanged) {
        this.categories = categories;
        this.onSelectionChanged = onSelectionChanged;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grok_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String category = categories.get(position);
        holder.textView.setText(category);
        holder.checkBox.setChecked(selectedCategories.contains(category));
        
        // Use final category reference for lambda
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedCategories.add(category);
            else selectedCategories.remove(category);
            onSelectionChanged.run();
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.checkbox.MaterialCheckBox checkBox;
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.mealCheckBox);
            textView = itemView.findViewById(R.id.mealTextView);
        }
    }
}