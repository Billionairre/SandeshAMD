package edu.sandesh.mealmate.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.sandesh.mealmate.CustomProgressDialog;
import edu.sandesh.mealmate.GeoTagActivity;
import edu.sandesh.mealmate.GroceryActivity;
import edu.sandesh.mealmate.MapExplorerActivity;
import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.WeeklyPlanActivity;
import edu.sandesh.mealmate.adapters.ChatAdapter;
import edu.sandesh.mealmate.adapters.MealAdapter;
import edu.sandesh.mealmate.adapters.StoreAdapter;
import edu.sandesh.mealmate.model.ChatMessage;
import edu.sandesh.mealmate.model.Meal;
import edu.sandesh.mealmate.model.Recipe;
import edu.sandesh.mealmate.model.SavedLocation;
import edu.sandesh.mealmate.utils.APIKey;
import edu.sandesh.mealmate.utils.GroceryDatabaseHelper;
import edu.sandesh.mealmate.utils.ChatbotHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class HomeFragment extends Fragment implements MealAdapter.OnMealRemoveListener {

    private RecyclerView todaysMealRecyclerView, favStoreRecyclerView, chatMessagesRecyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private TextView noMealText;
    private MaterialButton viewWeeklyPlanButton, sendMessageButton;
    private FloatingActionButton shop, chatbotFab;
    private CustomProgressDialog customProgressDialog;

    private int completedRequests = 0;
    private int totalRequests = 0;

    private ShapeableImageView addStore, explore, filterList, groceryListIcon, notificationIcon;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private List<SavedLocation> storeList = new ArrayList<>();
    private StoreAdapter storeAdapter;
    
    // Chatbot related variables
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private TextInputEditText chatInputEditText;
    private BottomSheetDialog chatbotDialog;
    private com.google.android.material.chip.Chip suggestionChip1, suggestionChip2, suggestionChip3;
    private boolean chatbotVisible = false;

    private View view;
    private View chatbotView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Initialize Views
            todaysMealRecyclerView = view.findViewById(R.id.todaysMealRecyclerView);
            if (todaysMealRecyclerView != null && getContext() != null) {
                todaysMealRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            
            noMealText = view.findViewById(R.id.noMealText);
            addStore = view.findViewById(R.id.addFavStoreIcon);
            filterList = view.findViewById(R.id.filterStoresIcon);
            groceryListIcon = view.findViewById(R.id.groceryListIcon);
            notificationIcon = view.findViewById(R.id.notificationIcon);
            explore = view.findViewById(R.id.viewMapIcon);
            favStoreRecyclerView = view.findViewById(R.id.favStoresRecyclerView);
            viewWeeklyPlanButton = view.findViewById(R.id.viewWeeklyPlanButton);
            shop = view.findViewById(R.id.shop);
            chatbotFab = view.findViewById(R.id.chatbotFab);
            
            // Initialize or reinitialize CustomProgressDialog
            if (getActivity() != null) {
                customProgressDialog = new CustomProgressDialog(getActivity());
            }
            
            if (getContext() != null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                if (favStoreRecyclerView != null) {
                    favStoreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                }
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error initializing views: " + e.getMessage());
        }
        
        // Setup components safely, one at a time
        try {
            setupChatbot();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error setting up chatbot: " + e.getMessage());
        }

        try {
            setupClickListeners();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error setting up click listeners: " + e.getMessage());
        }

        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                // Load data only if activity is valid
                new Handler().post(() -> {
                    try {
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                            loadFavStoreDataFromFireStore();
                            loadDataMealToday(true);
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error loading data: " + e.getMessage());
                        if (customProgressDialog != null) {
                            customProgressDialog.dismiss();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error initializing data loading: " + e.getMessage());
        }

        return view;
    }

    void loadFavStoreDataFromFireStore() {
        if (!isAdded() || getContext() == null) return; // Check if fragment is still attached
        
        try {
            if (customProgressDialog != null) {
                customProgressDialog.show();
            }
            storeList.clear();
    
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            GroceryDatabaseHelper groceryDbHelper = new GroceryDatabaseHelper(getContext());
    
            // Fetch grocery items in background
            new Thread(() -> {
                try {
                    if (!isAdded() || getContext() == null) {
                        if (customProgressDialog != null) {
                            customProgressDialog.dismiss();
                        }
                        return;
                    }
                    
                    // Fetch Unpurchased Grocery Items for the Week
                    Map<String, Map<String, List<String>>> weeklyGroceryMap = groceryDbHelper.getGroceryItemsForWeekUnpurchased();
                    Set<String> uniqueGroceryItems = new HashSet<>();
    
                    for (Map<String, List<String>> categoryMap : weeklyGroceryMap.values()) {
                        for (List<String> items : categoryMap.values()) {
                            uniqueGroceryItems.addAll(items);
                        }
                    }
    
                    List<String> groceryItems = new ArrayList<>(uniqueGroceryItems);
    
                    // Get Current Location
                    if (checkLocationPermission()) {
                        if (fusedLocationClient == null) {
                            if (getContext() != null) {
                                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                            } else {
                                throw new Exception("Context is null when initializing fusedLocationClient");
                            }
                        }
                        
                        fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(location -> {
                                if (!isAdded() || getContext() == null) {
                                    if (customProgressDialog != null) {
                                        customProgressDialog.dismiss();
                                    }
                                    return;
                                }
                                
                                if (location != null) {
                                    loadStoresWithLocation(db, groceryItems, location);
                                } else {
                                    handleLocationNull(db, groceryItems);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded() || getContext() == null) {
                                    if (customProgressDialog != null) {
                                        customProgressDialog.dismiss();
                                    }
                                    return;
                                }
                                handleLocationError(db, groceryItems, e);
                            });
                    } else {
                        if (isAdded() && getContext() != null) {
                            if (customProgressDialog != null) {
                                customProgressDialog.dismiss();
                            }
                            Toast.makeText(getContext(), 
                                "Location permission is required to show distances to stores", 
                                Toast.LENGTH_LONG).show();
                        }
                        loadStoresWithoutLocation(db, groceryItems);
                    }
                } catch (Exception e) {
                    if (isAdded() && getContext() != null) {
                        if (customProgressDialog != null) {
                            customProgressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), 
                            "Error loading data: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                    Log.e("HomeFragment", "Error in loadFavStoreDataFromFireStore: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error starting data loading: " + e.getMessage());
            if (customProgressDialog != null) {
                customProgressDialog.dismiss();
            }
            if (getActivity() != null && !getActivity().isFinishing() && getView() != null) {
                Snackbar.make(getView(), "Unable to load data. Please try again.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void loadStoresWithLocation(FirebaseFirestore db, List<String> groceryItems, Location location) {
        final double currentLat = location.getLatitude();
        final double currentLng = location.getLongitude();

        db.collection("favstore")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!isAdded()) return;

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    processStoreDocument(document, groceryItems, currentLat, currentLng);
                }

                updateStoreAdapter();
                if (customProgressDialog != null) {
                    customProgressDialog.dismiss();
                }
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                handleStoreLoadError(e);
            });
    }

    private void processStoreDocument(QueryDocumentSnapshot document, List<String> groceryItems, 
                                    double currentLat, double currentLng) {
        String storeName = document.getString("storeName");
        String address = document.getString("address");
        String latLong = document.getString("latLong");
        List<String> ingredients = (List<String>) document.get("ingredients");

        if (latLong != null && latLong.contains("Lat:") && latLong.contains("Long:")) {
            try {
                String[] latLngParts = latLong.replace("Lat:", "").replace("Long:", "").split(",");
                if (latLngParts.length == 2) {
                    double storeLat = Double.parseDouble(latLngParts[0].trim());
                    double storeLng = Double.parseDouble(latLngParts[1].trim());
                    String distance = calculateDistance(currentLat, currentLng, storeLat, storeLng);
                    
                    List<String> matchedIngredients = getMatchedIngredients(ingredients, groceryItems);
                    
                    getPhotoReference(String.valueOf(storeLat), String.valueOf(storeLng), 
                        APIKey.GOOGLE_API_KEY, imageUrl -> {
                            if (!isAdded()) return;
                            
                            SavedLocation savedLocation = new SavedLocation(
                                storeName,
                                imageUrl != null ? imageUrl : "R.drawable.ic_image_placeholder",
                                address,
                                storeLat,
                                storeLng,
                                distance,
                                ingredients,
                                matchedIngredients.size()
                            );
                            
                            savedLocation.setMatchedIngredients(matchedIngredients);
                            storeList.add(savedLocation);
                            updateStoreAdapter();
                        });
                }
            } catch (NumberFormatException e) {
                Log.e("HomeFragment", "Error parsing coordinates: " + e.getMessage());
            }
        }
    }

    private List<String> getMatchedIngredients(List<String> ingredients, List<String> groceryItems) {
        List<String> matchedIngredients = new ArrayList<>();
        for (String ingredient : ingredients) {
            for (String dbItem : groceryItems) {
                if (ingredient.equalsIgnoreCase(dbItem.trim())) {
                    matchedIngredients.add(ingredient);
                }
            }
        }
        return matchedIngredients;
    }

    private void updateStoreAdapter() {
        if (!isAdded() || getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            if (!isAdded() || getContext() == null || favStoreRecyclerView == null) return;
            
            try {
                if (storeAdapter == null) {
                    storeAdapter = new StoreAdapter(getContext(), storeList);
                    // Set delete listener
                    storeAdapter.setOnStoreDeleteListener(new StoreAdapter.OnStoreDeleteListener() {
                        @Override
                        public void onStoreDeleted(SavedLocation store, int position) {
                            deleteStoreFromFirestore(store);
                        }
                    });
                    favStoreRecyclerView.setAdapter(storeAdapter);
                } else {
                    storeAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.e("HomeFragment", "Error updating store adapter: " + e.getMessage());
            }
        });
    }

    private void deleteStoreFromFirestore(SavedLocation store) {
        if (!isAdded() || getContext() == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        try {
            // Show a progress dialog
            if (customProgressDialog != null && !customProgressDialog.isShowing()) {
                customProgressDialog.show();
            }
            
            db.collection("favstore")
                .whereEqualTo("storeName", store.getName())
                .get()
                .addOnCompleteListener(task -> {
                    try {
                        if (customProgressDialog != null && customProgressDialog.isShowing()) {
                            customProgressDialog.dismiss();
                        }
                        
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            boolean foundMatch = false;
                            
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                // Get latLong string from document
                                String latLong = document.getString("latLong");
                                
                                if (latLong == null) {
                                    // If latLong is null, just delete this document since name matched
                                    deleteDocument(db, document.getId());
                                    foundMatch = true;
                                    break;
                                }
                                
                                // Check if this document's coordinates match the store's coordinates
                                if (latLong.contains("Lat:") && latLong.contains("Long:")) {
                                    try {
                                        String[] latLngParts = latLong.replace("Lat:", "").replace("Long:", "").split(",");
                                        if (latLngParts.length == 2) {
                                            double docLat = Double.parseDouble(latLngParts[0].trim());
                                            double docLng = Double.parseDouble(latLngParts[1].trim());
                                            
                                            // If coordinates match within a small tolerance
                                            if (Math.abs(docLat - store.getLatitude()) < 0.0001 && 
                                                Math.abs(docLng - store.getLongitude()) < 0.0001) {
                                                
                                                deleteDocument(db, document.getId());
                                                foundMatch = true;
                                                break;
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.e("HomeFragment", "Error parsing coordinates: " + e.getMessage());
                                    }
                                } else {
                                    // If latLong format doesn't match expected, delete by name only
                                    deleteDocument(db, document.getId());
                                    foundMatch = true;
                                    break;
                                }
                            }
                            
                            if (!foundMatch && isAdded() && getContext() != null) {
                                // If no exact match, delete the first document with matching name
                                String docId = task.getResult().getDocuments().get(0).getId();
                                deleteDocument(db, docId);
                            }
                        } else if (isAdded() && getContext() != null) {
                            Log.e("HomeFragment", "Error finding store to delete: " + 
                                (task.getException() != null ? task.getException().getMessage() : "No matching store found"));
                            Toast.makeText(getContext(), "Error finding store to delete", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error in delete completion: " + e.getMessage());
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Error processing delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (customProgressDialog != null && customProgressDialog.isShowing()) {
                        customProgressDialog.dismiss();
                    }
                    Log.e("HomeFragment", "Error querying for store: " + e.getMessage());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Error deleting store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            Log.e("HomeFragment", "Exception in deleteStoreFromFirestore: " + e.getMessage());
            if (customProgressDialog != null && customProgressDialog.isShowing()) {
                customProgressDialog.dismiss();
            }
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void deleteDocument(FirebaseFirestore db, String documentId) {
        if (!isAdded() || getContext() == null) return;
        
        db.collection("favstore").document(documentId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Store deleted successfully", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Failed to delete store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void handleLocationNull(FirebaseFirestore db, List<String> groceryItems) {
        if (!isAdded()) return;
        
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
        }
        Toast.makeText(requireContext(), 
            "Unable to get location. Make sure location is enabled on the device.", 
            Toast.LENGTH_LONG).show();
        
        loadStoresWithoutLocation(db, groceryItems);
    }

    private void handleLocationError(FirebaseFirestore db, List<String> groceryItems, Exception e) {
        if (!isAdded()) return;
        
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
        }
        Toast.makeText(requireContext(), 
            "Failed to get location: " + e.getMessage(), 
            Toast.LENGTH_LONG).show();
        
        loadStoresWithoutLocation(db, groceryItems);
    }

    private void handleStoreLoadError(Exception e) {
        if (!isAdded()) return;
        
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
        }
        Toast.makeText(requireContext(), 
            "Failed to load store data: " + e.getMessage(), 
            Toast.LENGTH_SHORT).show();
    }

    private String calculateDistance(double currentLat, double currentLng, double storeLat, double storeLng) {
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, storeLat, storeLng, results);
        float distanceInMeters = results[0];

        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", distanceInMeters / 1000);
        }
    }


    private float parseDistance(String distanceStr) {
        // Assumes distanceStr is in the format "500 m" or "1.2 km"
        if (distanceStr.toLowerCase().contains("km")) {
            // Remove "km", trim, and convert to float then multiply by 1000 to get meters.
            try {
                float km = Float.parseFloat(distanceStr.toLowerCase().replace("km", "").trim());
                return km * 1000;
            } catch (NumberFormatException e) {
                return Float.MAX_VALUE;
            }
        } else if (distanceStr.toLowerCase().contains("m")) {
            // Remove "m", trim, and convert to float
            try {
                return Float.parseFloat(distanceStr.toLowerCase().replace("m", "").trim());
            } catch (NumberFormatException e) {
                return Float.MAX_VALUE;
            }
        }
        return Float.MAX_VALUE; // Fallback if the format is unrecognized
    }

    private void sortStoresByDistance() {
        Collections.sort(storeList, (s1, s2) -> {
            float d1 = parseDistance(s1.getDistance());
            float d2 = parseDistance(s2.getDistance());
            return Float.compare(d1, d2);
        });
        if (storeAdapter != null) {
            storeAdapter.notifyDataSetChanged();
        }
    }
    private void sortStoresByGroceryMatches() {
        Collections.sort(storeList, (s1, s2) ->
                Integer.compare(s2.getMatchingCount(), s1.getMatchingCount())
        );
        if (storeAdapter != null) {
            storeAdapter.notifyDataSetChanged();
        }
    }




    private void getPhotoReference(String latitude, String longitude, String apiKey, OnPhotoUrlReceivedListener listener) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + latitude + "," + longitude
                + "&radius=1000&types=store|grocery_or_supermarket|shopping_mall&key=" + apiKey;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onPhotoUrlReceived(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    listener.onPhotoUrlReceived(null);
                    return;
                }

                String jsonResponse = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray results = jsonObject.optJSONArray("results");

                    if (results != null && results.length() > 0) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);

                            // Filter by relevant place types
                            JSONArray types = place.getJSONArray("types");
                            for (int j = 0; j < types.length(); j++) {
                                String type = types.getString(j);
                                if (type.equals("store") || type.equals("grocery_or_supermarket") || type.equals("shopping_mall")) {
                                    JSONArray photos = place.optJSONArray("photos");
                                    if (photos != null && photos.length() > 0) {
                                        JSONObject photo = photos.getJSONObject(0);
                                        String photoReference = photo.getString("photo_reference");

                                        String imageUrl = getPhotoUrl(photoReference);
                                        listener.onPhotoUrlReceived(imageUrl);
                                        return; // Return first found image URL
                                    }
                                }
                            }
                        }
                        listener.onPhotoUrlReceived(null); // No image found for relevant types
                    } else {
                        listener.onPhotoUrlReceived(null); // No results
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onPhotoUrlReceived(null);
                }
            }
        });
    }






    public String getPhotoUrl(String photoReference) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                + photoReference + "&key=" + APIKey.GOOGLE_API_KEY;
    }




    @Override
    public void onResume() {
        super.onResume();
        if (mealList == null || mealList.isEmpty()) {
            loadDataMealToday(false);
        }
    }

    private void loadDataMealToday(boolean showLoad) {
        completedRequests = 0;
        totalRequests = 0;

        if (showLoad && customProgressDialog != null) {
            customProgressDialog.show();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.enableNetwork();
        DocumentReference mealRef = db.collection("meals").document(todayDate);

        mealRef.get().addOnCompleteListener(task -> {
            if (!isAdded()) return; // Check if fragment is still attached

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<Long> breakfastTimestamps = (List<Long>) document.get("Breakfast");
                    List<Long> lunchTimestamps = (List<Long>) document.get("Lunch");
                    List<Long> dinnerTimestamps = (List<Long>) document.get("Dinner");

                    if (breakfastTimestamps == null) breakfastTimestamps = new ArrayList<>();
                    if (lunchTimestamps == null) lunchTimestamps = new ArrayList<>();
                    if (dinnerTimestamps == null) dinnerTimestamps = new ArrayList<>();

                    totalRequests = breakfastTimestamps.size() + lunchTimestamps.size() + dinnerTimestamps.size();
                    List<Meal> allMeals = new ArrayList<>();

                    fetchAllMeals(breakfastTimestamps, lunchTimestamps, dinnerTimestamps, allMeals);
                } else {
                    updateMealRecyclerView(new ArrayList<>());
                    if (showLoad && customProgressDialog != null) {
                        customProgressDialog.dismiss();
                    }
                }
            } else {
                showSnackbar("error");
                if (showLoad && customProgressDialog != null) {
                    customProgressDialog.dismiss();
                }
            }
        });
    }

    private void fetchAllMeals(List<Long> breakfastTimestamps, List<Long> lunchTimestamps,
                               List<Long> dinnerTimestamps, List<Meal> allMeals) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (totalRequests == 0) {
            updateMealRecyclerView(new ArrayList<>());
            if (isAdded() && customProgressDialog != null) {
                customProgressDialog.dismiss();
            }
            return;
        }

        for (Long timestamp : breakfastTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Breakfast", allMeals);
        }
        for (Long timestamp : lunchTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Lunch", allMeals);
        }
        for (Long timestamp : dinnerTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Dinner", allMeals);
        }
    }

    private void fetchRecipeByTimestamp(FirebaseFirestore db, Long timestamp, String mealType, List<Meal> allMeals) {
        db.collection("recipes")
                .whereEqualTo("timestamp", timestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                allMeals.add(new Meal(recipe, mealType));
                            }
                        }
                    }
                    checkAndUpdateRecyclerView(allMeals);
                })
                .addOnFailureListener(e -> checkAndUpdateRecyclerView(allMeals));
    }

    private void checkAndUpdateRecyclerView(List<Meal> allMeals) {
        completedRequests++;
        if (completedRequests == totalRequests) {
            updateMealRecyclerView(allMeals);
            if (isAdded() && customProgressDialog != null) {
                customProgressDialog.dismiss();
            }
        }
    }

    private void updateMealRecyclerView(List<Meal> allMeals) {
        boolean hasMeals = !allMeals.isEmpty();
        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
        todaysMealRecyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);

        if (hasMeals) {
            mealAdapter = new MealAdapter(requireContext(), allMeals, false, this);
            todaysMealRecyclerView.setAdapter(mealAdapter);
        } else {
            todaysMealRecyclerView.setAdapter(null);
        }
    }



    private boolean checkLocationPermission() {
        try {
            if (!isAdded() || getContext() == null || getActivity() == null) {
                Log.e("HomeFragment", "Fragment not attached in checkLocationPermission");
                return false;
            }
            
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    
                // Request Permissions
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("HomeFragment", "Error checking location permission: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting location

            } else {
                // Permission denied, show a message to the user
                showSnackbar("Location permission denied. Please enable it in settings to get location.");

            }
        }
    }



    @Override
    public void onMealRemove(Meal meal, int position) {
        // Implement meal removal logic if needed
    }

    public interface OnPhotoUrlReceivedListener {
        void onPhotoUrlReceived(String imageUrl);
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Load favorite stores without location information (distance calculation)
     */
    private void loadStoresWithoutLocation(FirebaseFirestore db, List<String> groceryItems) {
        if (db == null || !isAdded()) return;
        
        db.collection("favstore")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!isAdded()) return;
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    if (!isAdded()) break;
                    
                    final String storeName = document.getString("storeName");
                    final String address = document.getString("address");
                    String latLong = document.getString("latLong");
                    List<String> ingredients = (List<String>) document.get("ingredients");
                    
                    // Set default distance
                    final String distance = "Unknown";
                    
                    // Safely parse coordinates for the photo reference, but don't calculate distance
                    double storeLat = 0.0;
                    double storeLng = 0.0;
                    
                    if (latLong != null && latLong.contains("Lat:") && latLong.contains("Long:")) {
                        try {
                            String[] latLngParts = latLong.replace("Lat:", "").replace("Long:", "").split(",");
                            if (latLngParts.length == 2) {
                                storeLat = Double.parseDouble(latLngParts[0].trim());
                                storeLng = Double.parseDouble(latLngParts[1].trim());
                            }
                        } catch (NumberFormatException e) {
                            Log.e("HomeFragment", "Error parsing coordinates: " + e.getMessage());
                        }
                    }
                    
                    // Get Matched Ingredients
                    final List<String> matchedIngredients = new ArrayList<>();
                    if (ingredients != null && groceryItems != null) {
                        for (String ingredient : ingredients) {
                            for (String dbItem : groceryItems) {
                                if (ingredient != null && dbItem != null && 
                                    ingredient.equalsIgnoreCase(dbItem.trim())) {
                                    matchedIngredients.add(ingredient);
                                }
                            }
                        }
                    }
                    
                    // Make a final copy of these values for use in lambda
                    final double finalStoreLat = storeLat;
                    final double finalStoreLng = storeLng;
                    final List<String> finalIngredients = ingredients != null ? 
                        new ArrayList<>(ingredients) : new ArrayList<>();
                    
                    // Use default image if coordinates are invalid
                    if (storeLat == 0.0 && storeLng == 0.0) {
                        // Create location with default image
                        SavedLocation savedLocation = new SavedLocation(
                            storeName,
                            "R.drawable.ic_image_placeholder",
                            address,
                            finalStoreLat,
                            finalStoreLng,
                            distance,
                            finalIngredients,
                            matchedIngredients.size()
                        );
                        
                        savedLocation.setMatchedIngredients(matchedIngredients);
                        storeList.add(savedLocation);
                        
                        // Update UI
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                if (!isAdded()) return;
                                
                                if (storeAdapter == null) {
                                    storeAdapter = new StoreAdapter(requireContext(), storeList);
                                    if (favStoreRecyclerView != null) {
                                        favStoreRecyclerView.setAdapter(storeAdapter);
                                    }
                                } else {
                                    storeAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } else {
                        // Get photo reference if coordinates are valid
                        getPhotoReference(String.valueOf(finalStoreLat), String.valueOf(finalStoreLng), 
                            APIKey.GOOGLE_API_KEY, new OnPhotoUrlReceivedListener() {
                                @Override
                                public void onPhotoUrlReceived(String imageUrl) {
                                    if (!isAdded()) return;
                                    
                                    SavedLocation savedLocation = new SavedLocation(
                                        storeName,
                                        imageUrl != null ? imageUrl : "R.drawable.ic_image_placeholder",
                                        address,
                                        finalStoreLat,
                                        finalStoreLng,
                                        distance,
                                        finalIngredients,
                                        matchedIngredients.size()
                                    );
                                    
                                    savedLocation.setMatchedIngredients(matchedIngredients);
                                    storeList.add(savedLocation);
                                    
                                    // Update UI
                                    if (getActivity() != null && isAdded()) {
                                        getActivity().runOnUiThread(() -> {
                                            if (!isAdded()) return;
                                            
                                            if (storeAdapter == null) {
                                                storeAdapter = new StoreAdapter(requireContext(), storeList);
                                                if (favStoreRecyclerView != null) {
                                                    favStoreRecyclerView.setAdapter(storeAdapter);
                                                }
                                            } else {
                                                storeAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            });
                    }
                }
                
                // Set adapter in case there are no stores
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        
                        if (storeAdapter == null && !storeList.isEmpty() && favStoreRecyclerView != null) {
                            storeAdapter = new StoreAdapter(requireContext(), storeList);
                            favStoreRecyclerView.setAdapter(storeAdapter);
                        }
                    });
                }
            })
            .addOnFailureListener(e -> {
                if (!isAdded() || getContext() == null) return;
                
                Toast.makeText(getContext(), "Failed to load store data: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Shows the notifications dialog with meal reminders
     */
    private void showNotificationsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Meal Reminders");
        
        // Get today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        
        // Get meal reminders from database
        GroceryDatabaseHelper dbHelper = new GroceryDatabaseHelper(requireContext());
        Map<String, Map<String, List<String>>> mealMap = dbHelper.getGroceryItemsForDay(todayDate);
        
        if (mealMap == null || mealMap.isEmpty()) {
            builder.setMessage("No meal reminders for today");
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Today's meals:\n\n");
            
            for (String mealName : mealMap.keySet()) {
                message.append("• ").append(mealName).append("\n");
            }
            
            message.append("\nTap on the Weekly Plan to set meal reminders.");
            builder.setMessage(message.toString());
        }
        
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("View Weekly Plan", (dialog, which) -> {
            startActivity(new Intent(getContext(), WeeklyPlanActivity.class));
        });
        
        builder.show();
    }

    /**
     * Sets up the chatbot interface and functionality
     */
    private void setupChatbot() {
        if (!isAdded() || getContext() == null) return;
        
        try {
            // Initialize chatbot dialog
            if (chatbotDialog == null) {
                chatbotDialog = new BottomSheetDialog(getContext());
            }
            
            if (getLayoutInflater() == null) return;
            
            chatbotView = getLayoutInflater().inflate(R.layout.bottom_sheet_chatbot, null);
            if (chatbotView == null) {
                Log.e("HomeFragment", "Failed to inflate chatbot view");
                return;
            }
            
            chatbotDialog.setContentView(chatbotView);
            
            // Initialize chat views
            chatMessagesRecyclerView = chatbotView.findViewById(R.id.chatMessagesRecyclerView);
            if (chatMessagesRecyclerView == null) {
                Log.e("HomeFragment", "Failed to find chatMessagesRecyclerView");
                return;
            }
            
            chatInputEditText = chatbotView.findViewById(R.id.chatInputEditText);
            sendMessageButton = chatbotView.findViewById(R.id.sendMessageButton);
            suggestionChip1 = chatbotView.findViewById(R.id.suggestionChip1);
            suggestionChip2 = chatbotView.findViewById(R.id.suggestionChip2);
            suggestionChip3 = chatbotView.findViewById(R.id.suggestionChip3);
            
            // Setup RecyclerView for chat messages
            chatMessages = new ArrayList<>();
            chatAdapter = new ChatAdapter(getContext(), chatMessages);
            
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setStackFromEnd(true); // To start from bottom
            chatMessagesRecyclerView.setLayoutManager(layoutManager);
            chatMessagesRecyclerView.setAdapter(chatAdapter);
            
            // Setup close button
            View closeButton = chatbotView.findViewById(R.id.closeChatButton);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> {
                    if (chatbotDialog != null) {
                        chatbotDialog.dismiss();
                        chatbotVisible = false;
                    }
                });
            }
            
            // Show dialog when FAB is clicked
            if (chatbotFab != null) {
                chatbotFab.setOnClickListener(v -> {
                    try {
                        if (!chatbotVisible && chatbotDialog != null) {
                            chatbotDialog.show();
                            chatbotVisible = true;
                            
                            // Show welcome message when chatbot is first opened
                            if (chatMessages.isEmpty()) {
                                addBotMessage(ChatbotHelper.getWelcomeMessage());
                            }
                        } else if (chatbotDialog != null) {
                            chatbotDialog.dismiss();
                            chatbotVisible = false;
                        }
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error showing chatbot dialog: " + e.getMessage());
                        Toast.makeText(getContext(), "Unable to open chat. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            // Set dialog dismiss listener
            if (chatbotDialog != null) {
                chatbotDialog.setOnDismissListener(dialog -> {
                    chatbotVisible = false;
                });
            }
            
            // Send message button click listener
            if (sendMessageButton != null) {
                sendMessageButton.setOnClickListener(v -> sendMessage());
            }
            
            // Send message on Enter key
            if (chatInputEditText != null) {
                chatInputEditText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendMessage();
                        return true;
                    }
                    return false;
                });
            }
            
            // Set up suggestion chip click listeners
            if (suggestionChip1 != null) {
                suggestionChip1.setOnClickListener(v -> {
                    addUserMessage("I need recipe ideas");
                    processUserMessage("I need recipe ideas");
                });
            }
            
            if (suggestionChip2 != null) {
                suggestionChip2.setOnClickListener(v -> {
                    addUserMessage("Help me with meal planning");
                    processUserMessage("Help me with meal planning");
                });
            }
            
            if (suggestionChip3 != null) {
                suggestionChip3.setOnClickListener(v -> {
                    addUserMessage("Tips for grocery shopping");
                    processUserMessage("Tips for grocery shopping");
                });
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error setting up chatbot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sends user message and gets bot response
     */
    private void sendMessage() {
        if (!isAdded() || chatInputEditText == null) return;
        
        String message = chatInputEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            processUserMessage(message);
            chatInputEditText.setText("");
        }
    }
    
    /**
     * Adds a user message to the chat
     */
    private void addUserMessage(String message) {
        if (!isAdded() || chatAdapter == null || chatMessages == null) return;
        
        ChatMessage chatMessage = new ChatMessage(message, ChatMessage.TYPE_USER);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        if (chatMessagesRecyclerView != null) {
            chatMessagesRecyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }
    
    /**
     * Adds a bot message to the chat
     */
    private void addBotMessage(String message) {
        if (!isAdded() || chatAdapter == null || chatMessages == null) return;
        
        ChatMessage chatMessage = new ChatMessage(message, ChatMessage.TYPE_BOT);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        if (chatMessagesRecyclerView != null) {
            chatMessagesRecyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }
    
    /**
     * Processes the user message and generates a response
     */
    private void processUserMessage(String message) {
        if (!isAdded()) return;
        
        // Simulate typing delay
        new Handler().postDelayed(() -> {
            if (!isAdded()) return; // Check again after delay
            
            String response = ChatbotHelper.getResponse(message);
            addBotMessage(response);
        }, 500);
    }

    /**
     * Sets up click listeners for the buttons
     */
    private void setupClickListeners() {
        if (!isAdded() || view == null) return;
        
        try {
            if (filterList != null) {
                filterList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isAdded() || getContext() == null) return;
                        
                        if (storeAdapter != null && !storeList.isEmpty()) {
                            try {
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                                View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_sort_options, null);
                                bottomSheetDialog.setContentView(bottomSheetView);

                                // Set up click listeners for each option
                                View distanceOption = bottomSheetView.findViewById(R.id.sort_by_distance);
                                if (distanceOption != null) {
                                    distanceOption.setOnClickListener(option -> {
                                        sortStoresByDistance();
                                        bottomSheetDialog.dismiss();
                                    });
                                }

                                View groceryOption = bottomSheetView.findViewById(R.id.sort_by_grocery);
                                if (groceryOption != null) {
                                    groceryOption.setOnClickListener(option -> {
                                        sortStoresByGroceryMatches();
                                        bottomSheetDialog.dismiss();
                                    });
                                }

                                bottomSheetDialog.show();
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Error showing sort options: " + e.getMessage());
                            }
                        } else {
                            if (view != null) {
                                Snackbar.make(view, "No stores available to sort", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

            // Button Listeners
            if (viewWeeklyPlanButton != null) {
                viewWeeklyPlanButton.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    startActivity(new Intent(getContext(), WeeklyPlanActivity.class));
                });
            }

            if (shop != null) {
                shop.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    startActivity(new Intent(getContext(), GroceryActivity.class));
                });
            }

            if (addStore != null) {
                addStore.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    startActivity(new Intent(getContext(), GeoTagActivity.class));
                });
            }

            if (groceryListIcon != null) {
                groceryListIcon.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    startActivity(new Intent(getContext(), GroceryActivity.class));
                });
            }

            if (explore != null) {
                explore.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    Intent intent = new Intent(getContext(), MapExplorerActivity.class);
                    intent.putParcelableArrayListExtra("storeList", new ArrayList<>(storeList));
                    startActivity(intent);
                });
            }

            // Setup notification icon click listener
            if (notificationIcon != null) {
                notificationIcon.setOnClickListener(v -> {
                    if (!isAdded() || getContext() == null) return;
                    showNotificationsDialog();
                });
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error in setupClickListeners: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any dialog that might be showing
        if (chatbotDialog != null && chatbotDialog.isShowing()) {
            chatbotDialog.dismiss();
        }
        
        // Clean up progress dialog
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
            customProgressDialog = null;
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Final cleanup of resources
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
            customProgressDialog = null;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Dismiss dialogs in onPause to avoid window leaks
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
        }
    }

}