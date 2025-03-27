package edu.sandesh.mealmate.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.sandesh.mealmate.CustomProgressDialog;
import edu.sandesh.mealmate.DelegateActivity;
import edu.sandesh.mealmate.GroceryActivity;
import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.adapters.GroceryAdapter;
import edu.sandesh.mealmate.adapters.OnItemCheckListener;
import edu.sandesh.mealmate.model.Meal;
import edu.sandesh.mealmate.model.Recipe;
import edu.sandesh.mealmate.utils.GroceryDatabaseHelper;


public class GroceryListFragment extends Fragment implements OnItemCheckListener, GroceryAdapter.OnItemClickListener {

    private ProgressUpdateListener progressUpdateListener;
    private static final String ARG_GROCERY_TYPE = "grocery_type";
    private String groceryType;
    private RecyclerView recyclerView;
    private GroceryAdapter groceryAdapter;
    private FloatingActionButton fabMain, fabAddItem, fabImportItems;
    private boolean isFabOpen = false;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;
    private MaterialTextView labelAddItem, labelImportItems;
    private GroceryDatabaseHelper dbHelper;
    private String selectedTab = "Today"; // Default to "Today"

    private FloatingActionButton fabDelegateShopping;
    private MaterialTextView labelDelegateShopping;



    CustomProgressDialog customProgressDialog;
    public static GroceryListFragment newInstance(String groceryType) {
        GroceryListFragment fragment = new GroceryListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROCERY_TYPE, groceryType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProgressUpdateListener) {
            progressUpdateListener = (ProgressUpdateListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement ProgressUpdateListener");
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groceryType = getArguments().getString(ARG_GROCERY_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_list, container, false);
        dbHelper = new GroceryDatabaseHelper(requireContext());
        customProgressDialog = new CustomProgressDialog(getContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down));
        recyclerView.scheduleLayoutAnimation();

        String date = getDateForTab(groceryType);
        loadData(date, selectedTab);
        fabMain = view.findViewById(R.id.fab_main);
        fabAddItem = view.findViewById(R.id.fab_add_item);
        fabImportItems = view.findViewById(R.id.fab_import_items);
        labelAddItem = view.findViewById(R.id.label_add_item);
        labelImportItems = view.findViewById(R.id.label_import_items);
        fabDelegateShopping = view.findViewById(R.id.fab_delegate_shopping);
        labelDelegateShopping = view.findViewById(R.id.label_delegate_shopping);


        fabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_backward);

        fabMain.setOnClickListener(v -> {
            toggleFabMenu();

        });
        fabAddItem.setOnClickListener(v -> {
            toggleFabMenu();
            String dates = getDateForTab(groceryType);
            showAddItemDialog(dates);
        });
        fabImportItems.setOnClickListener(v -> {toggleFabMenu();
            showImportConfirmationDialog();
        });

        fabDelegateShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if( dbHelper.hasGroceryDataForWeek()){
                Intent intent = new Intent(getActivity(), DelegateActivity.class );
                startActivity(intent);
               }
               else {
                   showSnackbar("No Grocery Items are available to delegate");
               }
            }
        });

        TabLayout tabLayout = requireActivity().findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getText().toString();
                String date = getDateForTab(selectedTab);
                Log.d("GroceryListFragment", "Tab Selected: " + selectedTab + ", Date: " + date);
                loadData(date, selectedTab);

                // Force update the listener for the first switch
                if (progressUpdateListener != null) {
                    int progress = calculateProgressForTab(selectedTab); // Implement this method
                    String summaryText = getSummaryTextForTab(selectedTab, progress); // Implement this method
                    progressUpdateListener.onProgressUpdated(progress, summaryText);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });

        return view;
    }


    private void showImportConfirmationDialog() {
        // Create Material AlertDialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import from Meal Plan")
                .setMessage("Are you sure you want to import from the meal plan for the upcoming week? " +
                        "This will remove and replace all the grocery list items you have saved before.")
                .setPositiveButton("Yes, Import", (dialog, which) -> {
                    // If user confirms, proceed with import
                    importFromMealPlan();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // If user cancels, dismiss the dialog
                    dialog.dismiss();
                })
                .setCancelable(false) // Prevent dismissing by clicking outside
                .show();
    }

    private void importFromMealPlan() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        GroceryDatabaseHelper dbHelper = new GroceryDatabaseHelper(requireContext());

        // Show Loading Indicator
        customProgressDialog.show();

        // Get Dates from Today to Next 6 Days
        List<String> datesToFetch = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i <= 7; i++) {
            datesToFetch.add(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Clear Existing Grocery Data only once
        dbHelper.clearGroceryData();

        // Counter to track Firestore calls
        final int[] counter = {0};
        final int totalTasks = datesToFetch.size(); // Total number of dates to fetch

        // Loop Through Dates and Fetch Data
        for (String date : datesToFetch) {
            db.collection("meals").document(date).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> mealData = documentSnapshot.getData();
                    if (mealData != null) {
                        for (String mealType : mealData.keySet()) {
                            List<Object> recipeIds = (List<Object>) mealData.get(mealType);

                            if (recipeIds != null) {
                                for (Object recipeIdObject : recipeIds) {
                                    String recipeId = String.valueOf(recipeIdObject); // Convert Long to String

                                    // Fetch Each Recipe
                                    db.collection("recipes").document(recipeId).get().addOnSuccessListener(recipeSnapshot -> {
                                        if (recipeSnapshot.exists()) {
                                            // Map Recipe Data to Recipe Model
                                            Recipe recipe = recipeSnapshot.toObject(Recipe.class);

                                            if (recipe != null) {
                                                // Add to Grocery Database
                                                Map<String, List<String>> ingredientsMap = recipe.getIngredients();
                                                if (ingredientsMap != null) {
                                                    for (String category : ingredientsMap.keySet()) {
                                                        List<String> ingredients = ingredientsMap.get(category);

                                                        for (String ingredient : ingredients) {
                                                            // Use Recipe Name as Category
                                                            dbHelper.addGroceryItem(ingredient, date, recipe.getRecipeName());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(e -> {
                                        showSnackbar("Failed to fetch recipe: " + recipeId);
                                    }).addOnCompleteListener(task -> {
                                        // Increment counter and check if all tasks are complete
                                        counter[0]++;
                                        if (counter[0] == totalTasks) {
                                            customProgressDialog.dismiss();
                                            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
                                            loadData(todayDate, "Today");
                                        }
                                    });
                                }
                            } else {
                                // If no recipes for this meal type, increment counter
                                counter[0]++;
                            }
                        }
                    } else {
                        // If mealData is null, increment counter
                        counter[0]++;
                    }
                } else {
                    // If no document for this date, increment counter
                    counter[0]++;
                }
            }).addOnFailureListener(e -> {
                showSnackbar("Failed to fetch meal plan for date: " + date);
                // Increment counter even on failure
                counter[0]++;
            });
        }
    }





    private int calculateProgressForTab(String tab) {
        int totalItems = 0;
        int purchasedItems = 0;

        if ("Week".equals(tab)) {
            Map<String, Map<String, List<String>>> weeklyGroceryMap = dbHelper.getGroceryItemsForWeek();
            for (Map<String, List<String>> dateMap : weeklyGroceryMap.values()) {
                for (String dateKey : dateMap.keySet()) {
                    List<String> items = dateMap.get(dateKey);
                    totalItems += items.size();
                    for (String itemName : items) {
                        if (dbHelper.isItemPurchased(itemName, dateKey)) {
                            purchasedItems++;
                        }
                    }
                }
            }
        } else {
            String date = getDateForTab(tab);
            Map<String, List<String>> groceryMap = dbHelper.getGroceryItemsByDate(date);
            for (String category : groceryMap.keySet()) {
                totalItems += groceryMap.get(category).size();
                for (String itemName : groceryMap.get(category)) {
                    if (dbHelper.isItemPurchased(itemName, date)) {
                        purchasedItems++;
                    }
                }
            }
        }

        return totalItems > 0 ? (int) ((purchasedItems / (float) totalItems) * 100) : 0;
    }

    private String getSummaryTextForTab(String tab, int progress) {
        if ("Today".equals(tab)) {
            return "Today (" + progress + "% Completed)";
        } else if ("Tomorrow".equals(tab)) {
            return "Tomorrow (" + progress + "% Completed)";
        } else if ("Week".equals(tab)) {
            return "This Week (" + progress + "% Completed)";
        } else {
            return tab + " (" + progress + "% Completed)";
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        progressUpdateListener = null; // Avoid memory leaks
    }

    private void toggleFabMenu() {
        if (isFabOpen) {
            // Close Animations
            fabAddItem.startAnimation(fabClose);
            fabImportItems.startAnimation(fabClose);
            fabDelegateShopping.startAnimation(fabClose); // New FAB
            labelAddItem.startAnimation(fabClose);
            labelImportItems.startAnimation(fabClose);
            labelDelegateShopping.startAnimation(fabClose); // New Label

            fabAddItem.setVisibility(View.GONE);
            fabImportItems.setVisibility(View.GONE);
            fabDelegateShopping.setVisibility(View.GONE); // New FAB
            labelAddItem.setVisibility(View.GONE);
            labelImportItems.setVisibility(View.GONE);
            labelDelegateShopping.setVisibility(View.GONE); // New Label

            fabMain.startAnimation(rotateBackward);
        } else {
            // Open Animations
            fabAddItem.setVisibility(View.VISIBLE);
            fabImportItems.setVisibility(View.VISIBLE);
            fabDelegateShopping.setVisibility(View.VISIBLE); // New FAB
            labelAddItem.setVisibility(View.VISIBLE);
            labelImportItems.setVisibility(View.VISIBLE);
            labelDelegateShopping.setVisibility(View.VISIBLE); // New Label

            fabAddItem.startAnimation(fabOpen);
            fabImportItems.startAnimation(fabOpen);
            fabDelegateShopping.startAnimation(fabOpen); // New FAB
            labelAddItem.startAnimation(fabOpen);
            labelImportItems.startAnimation(fabOpen);
            labelDelegateShopping.startAnimation(fabOpen); // New Label

            fabMain.startAnimation(rotateForward);
        }
        isFabOpen = !isFabOpen;
    }


    private void loadData(String date, String selectedTab) {
        int totalItems = 0;
        int purchasedItems = 0;

        if ("Week".equals(selectedTab)) {
            Map<String, Map<String, List<String>>> weeklyGroceryMap = dbHelper.getGroceryItemsForWeek();
            for (Map<String, List<String>> dateMap : weeklyGroceryMap.values()) {
                for (String dateKey : dateMap.keySet()) {
                    List<String> items = dateMap.get(dateKey);
                    totalItems += items.size();
                    for (String itemName : items) {
                        if (dbHelper.isItemPurchased(itemName, dateKey)) {
                            purchasedItems++;
                        }
                    }
                }
            }
            groceryAdapter = new GroceryAdapter(requireContext(), weeklyGroceryMap, () -> loadData("", "Week"));
        } else {
            Map<String, List<String>> groceryMap = dbHelper.getGroceryItemsByDate(date);
            Map<String, Map<String, List<String>>> dataMap = new HashMap<>();
            for (String category : groceryMap.keySet()) {
                Map<String, List<String>> dateMap = new HashMap<>();
                dateMap.put(date, groceryMap.get(category));
                dataMap.put(category, dateMap);
                totalItems += groceryMap.get(category).size();
                for (String itemName : groceryMap.get(category)) {
                    if (dbHelper.isItemPurchased(itemName, date)) {
                        purchasedItems++;
                    }
                }
            }
            groceryAdapter = new GroceryAdapter(requireContext(), dataMap, () -> loadData(date, selectedTab));
        }

        recyclerView.setAdapter(groceryAdapter);
        groceryAdapter.notifyDataSetChanged();

        // Calculate overall progress
        int overallProgress = totalItems > 0 ? (int) ((purchasedItems / (float) totalItems) * 100) : 0;

        // Update Overall Summary
        String summaryText = purchasedItems + "/" + totalItems + " Items Purchased";
        if (progressUpdateListener != null) {
            progressUpdateListener.onProgressUpdated(overallProgress, summaryText);
        }
    }


    private void showAddItemDialog(String date) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Add New Item");
        final EditText input = new EditText(requireContext());
        input.setHint("Enter item name");
        builder.setView(input);

        if ("Week".equals(selectedTab)) {
            builder.setMessage("Select a date for the upcoming week");
            builder.setPositiveButton("Select Date", (dialog, which) -> showDatePickerForWeek(input.getText().toString().trim()));
        } else {
            builder.setPositiveButton("Add", (dialog, which) -> {
                String itemName = input.getText().toString().trim();
                if (!itemName.isEmpty()) {
                    if (dbHelper.isItemExistsForDate(itemName, date)) {
                        showSnackbar("Item already exists for this date!");
                    } else {
                        dbHelper.addGroceryItem(itemName, date);
                        loadData(date, selectedTab);
                        showSnackbarWithUndo("Item Added Successfully", itemName, date);
                    }
                }
            });
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDatePickerForWeek(String itemName) {
        if (itemName.isEmpty()) {
            showSnackbar("Please enter an item name");
            return;
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    java.util.Calendar selectedDate = java.util.Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

                    if (dbHelper.isItemExistsForDate(itemName, formattedDate)) {
                        showSnackbar("Item already exists for this date!");
                    } else {
                        dbHelper.addGroceryItem(itemName, formattedDate);
                        loadData(formattedDate, selectedTab);
                        showSnackbarWithUndo("Item Added for " + formattedDate, itemName, formattedDate);
                    }
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 7);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showSnackbarWithUndo(String message, String itemName, String date) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    dbHelper.removeGroceryItem(itemName, date);
                    loadData(date, selectedTab);
                    Snackbar.make(requireView(), "Action Undone", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }

    private String getDateForTab(String type) {
        if ("Today".equals(type)) {
            return dbHelper.getTodayDate();
        } else if ("Tomorrow".equals(type)) {
            return dbHelper.getTomorrowDate();
        }
        return dbHelper.getTodayDate();  // Default to today if no match
    }

    private void showSnackbar(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setAction("OK", v -> {})
                .show();
    }

    private void setupRecyclerView() {
        Map<String, Map<String, List<String>>> weeklyItems = dbHelper.getGroceryItemsForWeek();
        groceryAdapter = new GroceryAdapter(requireContext(), weeklyItems, this);
        groceryAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(groceryAdapter);
    }

    private void showEditDialog(String itemName, String category) {
        String date = getDateForTab(selectedTab);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_grocery, null);
        
        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        
        editTextName.setText(itemName);
        
        // Setup category spinner
        List<String> categories = dbHelper.getAllCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_spinner_item, 
            categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // Set current category
        int categoryPosition = categories.indexOf(category);
        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition);
        }

        builder.setView(dialogView)
               .setTitle("Edit Item")
               .setPositiveButton("Save", (dialog, which) -> {
                   String newName = editTextName.getText().toString().trim();
                   String newCategory = spinnerCategory.getSelectedItem().toString();
                   
                   if (!newName.isEmpty()) {
                       dbHelper.updateGroceryItem(itemName, newName, date);
                       dbHelper.updateGroceryItemCategory(newName, newCategory, date);
                       loadData(date, selectedTab);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    @Override
    public void onItemCheckChanged() {
        // Refresh the data to update the UI
        String date = getDateForTab(selectedTab);
        loadData(date, selectedTab);
    }
    
    @Override
    public void onItemClick(String itemName, String category) {
        // Handle item click if needed
    }
    
    @Override
    public void onEditClick(String itemName, String category) {
        showEditDialog(itemName, category);
    }
}

