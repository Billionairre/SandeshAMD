package edu.sandesh.mealmate.utils;

import android.util.Log;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sandesh.mealmate.model.Recipe;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface FirestoreCallback {
        void onCallback(List<Recipe> recipeList);
    }

    public void loadRecipes(FirestoreCallback callback) {
        db.collection("recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING) // 🔥 Order by latest timestamp
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> recipeList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Recipe recipe = new Recipe();
                                recipe.setRecipeName(document.getString("recipeName"));
                                recipe.setCookTime(document.getString("cookTime"));
                                recipe.setPhotoUrl(document.getString("photoUrl"));
                                recipe.setTimestamp(document.getLong("timestamp"));

                                // ✅ Convert Ingredients Map
                                Map<String, List<String>> ingredientsMap = new HashMap<>();
                                Map<String, Object> firestoreIngredients = (Map<String, Object>) document.get("ingredients");
                                if (firestoreIngredients != null) {
                                    for (Map.Entry<String, Object> entry : firestoreIngredients.entrySet()) {
                                        ingredientsMap.put(entry.getKey(), (List<String>) entry.getValue());
                                    }
                                }
                                recipe.setIngredients(ingredientsMap);

                                // ✅ Convert Instructions Array
                                List<Map<String, Object>> instructionsList = (List<Map<String, Object>>) document.get("instructions");
                                if (instructionsList != null) {
                                    recipe.setInstructions(instructionsList);
                                }

                                // ✅ Add to list
                                recipeList.add(recipe);

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing recipe: " + e.getMessage());
                            }
                        }
                        callback.onCallback(recipeList);
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }





}
