package edu.sandesh.mealmate.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.sandesh.mealmate.EditProfileFromDashboard;
import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.adapters.RecipeCardAdapter;
import edu.sandesh.mealmate.home.MainActivity;
import edu.sandesh.mealmate.model.Recipe;
import edu.sandesh.mealmate.utils.AgeCalculate;
import edu.sandesh.mealmate.utils.FirestoreHelper;
import edu.sandesh.mealmate.utils.GroceryDatabaseHelper;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView tvUserName, tvGender;
    private FirebaseFirestore db;
    private String userId;
    private View rootView;
    private FirestoreHelper firestoreHelper;
    RecyclerView recyclerView;

    private TextView recipeCount, groceryCount, MealCount;
    private GroceryDatabaseHelper groceryDatabaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        profileImage = rootView.findViewById(R.id.profileImage);
        tvUserName = rootView.findViewById(R.id.tvUserName);
        tvGender = rootView.findViewById(R.id.tvGender);
        
        db = FirebaseFirestore.getInstance();
        groceryDatabaseHelper = new GroceryDatabaseHelper(getContext());

        recipeCount = rootView.findViewById(R.id.tvRecipes);
        groceryCount = rootView.findViewById(R.id.tvShoppingLists);
        MealCount = rootView.findViewById(R.id.tillMealPlans);
        
        // Empty state message for recipes
        TextView emptyRecipesMessage = rootView.findViewById(R.id.emptyRecipesMessage);

        recyclerView = rootView.findViewById(R.id.savedRecipesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        firestoreHelper = new FirestoreHelper();

        // Set up MaterialToolbar
        MaterialToolbar toolbar = rootView.findViewById(R.id.profileToolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.profile_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_profile) {
                openEditProfile();
                return true;
            } else if (item.getItemId() == R.id.action_sign_out) {
                signOutUser();
                return true;
            }
            return false;
        });

        // Load user data
        loadUserData();
        loadUserDataRemote();

        firestoreHelper.loadRecipes(new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onCallback(List<Recipe> recipeList) {
                if (recipeList != null && !recipeList.isEmpty()) {
                    recipeCount.setText(String.valueOf(recipeList.size()));
                    RecipeCardAdapter adapter = new RecipeCardAdapter(getContext(), recipeList);
                    recyclerView.setAdapter(adapter);
                    
                    // Hide empty state
                    emptyRecipesMessage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    
                    for (Recipe recipe : recipeList) {
                        Log.d(TAG, "imgurl: " + recipe.getPhotoUrl());
                        Log.d(TAG, "timestamp: " + recipe.getTimestamp());
                        Log.d(TAG, "Recipe: " + recipe.getRecipeName());
                        Log.d(TAG, "Cook Time: " + recipe.getCookTime());
                        Log.d(TAG, "Ingredients: " + recipe.getIngredients().toString());
                        Log.d(TAG, "Instructions: " + recipe.getInstructions().toString());
                    }
                } else {
                    // Show empty state
                    recipeCount.setText("0");
                    emptyRecipesMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        loadGroceryCount();
        mealCounter();

        return rootView;
    }

    private void loadGroceryCount() {
        // Get total count from database
        int totalGroceryCount = groceryDatabaseHelper.getWeeklyGroceryItemCount();

        // Update the UI
        groceryCount.setText(String.valueOf(totalGroceryCount));
    }

    void mealCounter() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> datesToFetch = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Get Dates from Today to Next 7 Days
        for (int i = 0; i <= 6; i++) {
            datesToFetch.add(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Initialize total count and completion counter
        final int[] totalCount = {0};
        final int[] completedCount = {0};

        // Loop Through Dates and Fetch Data
        for (String date : datesToFetch) {
            db.collection("meals").document(date).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> mealData = documentSnapshot.getData();
                    if (mealData != null) {
                        for (String mealType : mealData.keySet()) {
                            List<Object> timestamps = (List<Object>) mealData.get(mealType);
                            if (timestamps != null) {
                                totalCount[0] += timestamps.size(); // Count timestamps
                            }
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                showSnackbar("Failed to fetch meal plan for date: " + date);
            }).addOnCompleteListener(task -> {
                completedCount[0]++;
                // When all dates are fetched, update UI
                if (completedCount[0] == datesToFetch.size()) {
                    MealCount.setText(String.valueOf(totalCount[0]));
                    Log.d(TAG, "Total meal count: " + totalCount[0]);
                }
            });
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileFromDashboard.class);
        editProfileLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK) {
                            loadUserData(); // Reload profile when returning from EditProfile
                            loadUserDataRemote(); // Fetch latest data from Firebase
                        }
                    });

    private void signOutUser() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false) // Prevents dismissing by tapping outside
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Firebase Sign Out
                    FirebaseAuth.getInstance().signOut();

                    // Clear SharedPreferences
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Clears all saved values
                    editor.apply();

                    // Navigate to MainActivity and clear backstack
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevents going back to Profile
                    startActivity(intent);
                    requireActivity().finish(); // Ensures ProfileFragment is fully removed
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Properly dismisses dialog
                .show();
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);
        String name = sharedPreferences.getString("USER_NAME", null);
        String mobile = sharedPreferences.getString("USER_MOBILE", null);
        String dob = sharedPreferences.getString("USER_DOB", null);
        String gender = sharedPreferences.getString("USER_GENDER", null);
        String photoUrl = sharedPreferences.getString("USER_PHOTO", null);

        // Set user name if available
        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        } else {
            tvUserName.setText("User");
        }
        
        // Set gender if available
        if (gender != null && !gender.isEmpty()) {
            tvGender.setText(gender);
        } else {
            tvGender.setText("");
        }

        // Load profile photo if available
        if (photoUrl != null && !photoUrl.isEmpty() && getContext() != null) {
            Glide.with(getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(profileImage);
        }
    }

    private void loadUserDataRemote() {
        if (userId == null) return;

        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String mobile = documentSnapshot.getString("mobile");
                String dob = documentSnapshot.getString("dob");
                String gender = documentSnapshot.getString("gender");
                String photoUrl = documentSnapshot.getString("photoUrl");

                // Update SharedPreferences
                SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                
                if (name != null) {
                    editor.putString("USER_NAME", name);
                    tvUserName.setText(name);
                }
                
                if (gender != null) {
                    editor.putString("USER_GENDER", gender);
                    tvGender.setText(gender);
                }
                
                if (mobile != null) {
                    editor.putString("USER_MOBILE", mobile);
                }
                
                if (dob != null) {
                    editor.putString("USER_DOB", dob);
                }
                
                if (photoUrl != null) {
                    editor.putString("USER_PHOTO", photoUrl);
                    
                    // Load profile image
                    if (getContext() != null) {
                        Glide.with(getContext())
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .into(profileImage);
                    }
                }
                
                editor.apply();
            }
        }).addOnFailureListener(e -> {
            showSnackbar("Failed to load profile data: " + e.getMessage());
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
}