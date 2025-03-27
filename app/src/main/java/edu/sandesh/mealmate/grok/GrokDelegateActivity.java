package edu.sandesh.mealmate.grok;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.utils.GroceryDatabaseHelper;

public class GrokDelegateActivity extends AppCompatActivity {
    private GroceryDatabaseHelper dbHelper;
    private RecyclerView mealRecyclerView, ingredientRecyclerView;
    private GrokMealAdapter mealAdapter;
    private GrokIngredientAdapter ingredientAdapter;
    private List<String> selectedCategories = new ArrayList<>();
    private List<GrokIngredient> allIngredients = new ArrayList<>(); // All available ingredients
    private List<GrokIngredient> selectedIngredients = new ArrayList<>(); // Ingredients with prices set

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grok_delegate);

        dbHelper = new GroceryDatabaseHelper(this);
        mealRecyclerView = findViewById(R.id.mealRecyclerView);
        ingredientRecyclerView = findViewById(R.id.ingredientRecyclerView);

        mealRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.sendRequestButton).setOnClickListener(v -> showShareOptions());

        loadMeals();
    }

    private void loadMeals() {
        Map<String, Map<String, List<String>>> weeklyItems = dbHelper.getGroceryItemsForWeek();
        List<String> categories = new ArrayList<>(weeklyItems.keySet());
        mealAdapter = new GrokMealAdapter(categories, this::updateIngredients);
        mealRecyclerView.setAdapter(mealAdapter);

        // Preload all ingredients for reference
        allIngredients.clear();
        for (String category : categories) {
            Map<String, List<String>> dateMap = weeklyItems.get(category);
            if (dateMap != null) {
                for (String date : dateMap.keySet()) {
                    for (String itemName : dateMap.get(date)) {
                        boolean isPurchased = dbHelper.isItemPurchased(itemName, date);
                        allIngredients.add(new GrokIngredient(itemName, date, category, isPurchased, 0.0f));
                    }
                }
            }
        }
    }

    private void updateIngredients() {
        selectedCategories.clear();
        for (int i = 0; i < mealAdapter.categories.size(); i++) {
            if (mealAdapter.selectedCategories.contains(mealAdapter.categories.get(i))) {
                selectedCategories.add(mealAdapter.categories.get(i));
            }
        }

        List<GrokIngredient> ingredients = new ArrayList<>();
        for (GrokIngredient ingredient : allIngredients) {
            if (selectedCategories.contains(ingredient.category)) {
                // Create a new ingredient instance to avoid modifying the original
                GrokIngredient newIngredient = new GrokIngredient(
                    ingredient.name, 
                    ingredient.date, 
                    ingredient.category, 
                    ingredient.isPurchased, 
                    0.0f);
                
                // Copy existing price from selectedIngredients if available
                for (GrokIngredient selected : selectedIngredients) {
                    if (selected.name.equals(ingredient.name) && selected.date.equals(ingredient.date)) {
                        newIngredient.price = selected.price;
                        break;
                    }
                }
                
                ingredients.add(newIngredient);
            }
        }
        
        // Create a new adapter with the updated ingredient list
        ingredientAdapter = new GrokIngredientAdapter(ingredients, selectedIngredients);
        ingredientRecyclerView.setAdapter(ingredientAdapter);
    }

    private void showShareOptions() {
        if (selectedIngredients.isEmpty()) {
            Toast.makeText(this, "Please select ingredients and set prices", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {
            "Share via SMS",
            "Share via Email"
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle("Share Shopping List")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    shareViaGeneric("sms");
                } else {
                    shareViaGeneric("email");
                }
            })
            .show();
    }

    private void shareViaGeneric(String method) {
        StringBuilder message = new StringBuilder("Shopping List:\n\n");
        float totalPrice = 0;
        for (GrokIngredient ingredient : selectedIngredients) {
            message.append(ingredient.name).append(" (").append(ingredient.category).append("): $")
                    .append(String.format("%.2f", ingredient.price)).append("\n");
            totalPrice += ingredient.price;
        }
        message.append("\nTotal: NPR").append(String.format("%.2f", totalPrice));

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shopping List");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        
        // Start share activity
        if (method.equals("email")) {
            shareIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(shareIntent, "Share via Email"));
        } else {
            startActivity(Intent.createChooser(shareIntent, "Share via SMS"));
        }
    }
}