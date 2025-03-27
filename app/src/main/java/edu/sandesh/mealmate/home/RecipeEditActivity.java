package edu.sandesh.mealmate.home;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.sandesh.mealmate.CustomProgressDialog;
import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.adapters.CategoryExpandableListAdapter;
import edu.sandesh.mealmate.adapters.InstructionAdapter;
import edu.sandesh.mealmate.model.InstructionStep;
import edu.sandesh.mealmate.model.Recipe;
import edu.sandesh.mealmate.utils.CustomExpandableListView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecipeEditActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private InstructionAdapter instructionAdapter;
    private List<InstructionStep> instructionList;
    private Button addInstructionButton;
    private CustomExpandableListView expandableListView; // Use CustomExpandableListView
    private CategoryExpandableListAdapter expandableListAdapter;
    private List<String> categoryList = new ArrayList<>();
    private HashMap<String, List<String>> ingredientMap = new HashMap<>();
    private List<String> selectedIngredients = new ArrayList<>();

    private EditText newIngredientInput;
    private Button addIngredientButton;
    private final String othersCategory = "🆕 Others";

    private EditText recipeNameInput, cookTimeInput;

    private Uri cameraImageUri;
    private Uri selectedImageUri = null;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final String IMGUR_CLIENT_ID = "e115074dd9cfe62"; // Replace with your Client ID
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image";

    ImageView recipeImage;

    private long timeStamp;


    private CustomProgressDialog progressDialog;
    Recipe recipe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_edit);

        recipe = getIntent().getParcelableExtra("RECIPE");


        recyclerView = findViewById(R.id.instructionRecyclerView);
        addInstructionButton = findViewById(R.id.addInstructionButton);
        expandableListView = findViewById(R.id.expandableListView); // Use CustomExpandableListView
        newIngredientInput = findViewById(R.id.newIngredientInput);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        recipeNameInput = findViewById(R.id.recipeName);
        cookTimeInput = findViewById(R.id.cookTime);
        recipeImage = findViewById(R.id.recipeImage);
        Button saveRecipeButton = findViewById(R.id.saveRecipeButton);
        Button uploadImageButton = findViewById(R.id.uploadImageButton);
        progressDialog = new CustomProgressDialog(RecipeEditActivity.this);

        // Setup Expandable ListView for ingredients
        setupExpandableListView();

        // Setup RecyclerView for instructions
        setupRecyclerView();

        // ✅ If recipe is received, populate the UI



        // Handle Add Instruction button click
        addInstructionButton.setOnClickListener(v -> addInstruction());

        addIngredientButton.setOnClickListener(v -> {
            String newIngredient = newIngredientInput.getText().toString().trim();

            if (!newIngredient.isEmpty()) {
                // Check if "Others" category exists, if not, add it
                if (!categoryList.contains(othersCategory)) {
                    categoryList.add(othersCategory);
                    ingredientMap.put(othersCategory, new ArrayList<>());
                }

                String ingredientWithEmoji = "🆕 " + newIngredient;

                // Add new ingredient under "Others" and check it by default
                ingredientMap.get(othersCategory).add(ingredientWithEmoji);
                selectedIngredients.add(ingredientWithEmoji); // ✅ Auto-check it

                // Notify adapter and expand the "Others" category
                expandableListAdapter.notifyDataSetChanged();
                expandableListView.expandGroup(categoryList.indexOf(othersCategory));

                // Clear input field for next entry
                newIngredientInput.setText("");
            } else {
                showSnackbar("Please enter an ingredient!");
            }
        });
        uploadImageButton.setOnClickListener(v -> showImagePickerDialog());

        saveRecipeButton.setOnClickListener(v -> saveRecipe());

        if (recipe != null) {
            timeStamp = recipe.getTimestamp();
            setRecipeData(recipe);

        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("RECIPE", recipe); // Save the recipe object
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recipe = savedInstanceState.getParcelable("RECIPE"); // Restore the recipe object
    }


    /**
     * Populates the UI with data from the received Recipe object.
     */
    private void setRecipeData(Recipe recipe) {
        Log.d("IntentDebug", "Populating UI with recipe: " + recipe.getRecipeName());

        // ✅ Set Recipe Name and Cook Time
        recipeNameInput.setText(recipe.getRecipeName());
        cookTimeInput.setText(recipe.getCookTime());

        // ✅ Load Recipe Image
        String imageUrl = recipe.getPhotoUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_men)
                    .into(recipeImage);
        } else {
            recipeImage.setImageResource(R.drawable.input_background);
        }



        // ✅ Load Selected Ingredients from Firestore Recipe Data


        // ✅ Debugging Logs


        // ✅ Convert Instructions to Mutable List Before Adding
        if (recipe.getInstructions() != null) {
            instructionList.clear();
            for (Map<String, Object> stepData : recipe.getInstructions()) {
                int stepNumber = ((Long) stepData.get("stepNumber")).intValue();
                String stepText = stepData.get("instruction").toString();
                instructionList.add(new InstructionStep(stepNumber, stepText)); // ✅ Safe modification
            }

            Log.d("IntentDebug", "Instructions Loaded: " + instructionList.size());
            instructionAdapter.notifyDataSetChanged();
        } else {
            Log.e("IntentDebug", "No instructions found in recipe.");
        }
    }





    private void showImagePickerDialog() {
        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickGalleryIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        Intent chooser = Intent.createChooser(pickGalleryIntent, "Select or Capture Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
        imagePickerLauncher.launch(chooser);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getData() != null) { // Picked from Gallery
                        selectedImageUri = data.getData();
                    } else { // Captured from Camera
                        selectedImageUri = cameraImageUri;
                    }

                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            recipeImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            //Toast.makeText(getContext(), "Error loading image.", Toast.LENGTH_SHORT).show();
                                showSnackbar("Error loading image");
                        }
                    }
                }
            });


    private void saveRecipe() {
        // Get values from input fields
        String recipeName = recipeNameInput.getText().toString().trim();
        String cookTime = cookTimeInput.getText().toString().trim();

        // 1️⃣ Check if Recipe Name is Empty
        if (recipeName.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please enter a recipe name!");
            return;
        }

        // 2️⃣ Check if Cook Time is Empty
        if (cookTime.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please enter cook time!");
            return;
        }

        // 3️⃣ Check if Any Ingredient is Selected
        if (selectedIngredients.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please select at least one ingredient!");
            return;
        }

        // 4️⃣ Categorize Selected Ingredients
        HashMap<String, List<String>> selectedCategoryMap = new HashMap<>();

        for (String category : categoryList) {
            List<String> selectedItems = new ArrayList<>();

            if (ingredientMap.containsKey(category)) {
                for (String ingredient : ingredientMap.get(category)) {
                    if (selectedIngredients.contains(ingredient)) {
                        // ✅ Keep the ingredient name with emojis
                        selectedItems.add(ingredient);
                    }
                }
            }

            // Only add the category to the map if it has selected ingredients
            if (!selectedItems.isEmpty()) {
                selectedCategoryMap.put(category, selectedItems);
            }
        }

        // 5️⃣ Check if Any Instruction is Empty
        if (instructionList.isEmpty()) {
            progressDialog.dismiss(); // Dismiss progress dialog
            showSnackbar("Please add at least one instruction step!");
            return;
        }

        for (InstructionStep step : instructionList) {
            String stepDescription = step.getInstruction(); // Store value

            if (stepDescription == null || stepDescription.trim().isEmpty()) {
                progressDialog.dismiss(); // Dismiss progress dialog
                showSnackbar("Please fill all instruction steps!");
                return;
            }
        }

        // 6️⃣ Prepare data for sending to the server
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("recipeName", recipeName);
        recipeData.put("cookTime", cookTime);
        recipeData.put("ingredients", selectedCategoryMap); // Include categorized ingredients
        recipeData.put("instructions", instructionList); // Include instruction steps

        // 7️⃣ Upload image if available, otherwise send recipe data directly
        if (selectedImageUri != null) {
            uploadImageToImgur(selectedImageUri, recipeData); // Pass recipeData to uploadImageToImgur
        } else {
            RecipeDataSend(recipe.getPhotoUrl(), recipeData); // Pass empty string as photoUrl
        }
    }
    private void uploadImageToImgur(Uri imageUri, Map<String, Object> recipeData) {
        progressDialog.show();
        File file = getFileFromUri(imageUri);

        if (file == null || !file.exists()) {
            showSnackbar("Error: Unable to process image.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(),
                        RequestBody.create(MediaType.parse("image/*"), file))
                .build();

        Request request = new Request.Builder()
                .url(IMGUR_UPLOAD_URL)
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .post(requestBody)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    String imgUrl = extractImageUrl(responseBody);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        RecipeDataSend(imgUrl, recipeData);}); // Pass trimmed gender
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showSnackbar("Upload failed!");});
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showSnackbar("Network error. Try again.");});
            }

        }).start();
    }

    void RecipeDataSend(String photoUrl, Map<String, Object> recipeData) {
        progressDialog.show();

        Log.d("recipeData", recipeData.toString());
        long timestamp = recipe.getTimestamp();

        if (photoUrl != null && !photoUrl.isEmpty()) {
            recipeData.put("photoUrl", photoUrl); // Add the photo URL if available
        } else {
            recipeData.put("photoUrl", ""); // Save empty string if no photo is selected
        }
        recipeData.put("timestamp", timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("recipes")
                .whereEqualTo("timestamp", timestamp) // Query the collection to find the document
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            DocumentReference recipeRef = document.getReference(); // Get the document reference

                            recipeRef.update(recipeData) // Update the document
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss(); // Dismiss progress dialog
                                        showSnackbar("Recipe updated successfully!");
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        showSnackbar("Failed to update recipe: " + e.getMessage());
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        showSnackbar("No recipe found with the given timestamp.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showSnackbar("Error fetching recipe: " + e.getMessage());
                });



        progressDialog.dismiss();
        if(progressDialog!=null){
            Intent intent = new Intent(RecipeEditActivity.this, DashboardActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();

        }


    }
    private String extractImageUrl(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("data").getString("link");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    private File getFileFromUri(Uri uri) {
        try {
            // Create a temporary file in the cache directory
            File tempFile = new File(getCacheDir(), "upload_image.jpg");

            // Open an input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Open an output stream to the temporary file
            OutputStream outputStream = new FileOutputStream(tempFile);

            // Copy the file contents
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Flush and close the streams
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d("FILE_PATH", "Temp file created: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupExpandableListView() {
   // ✅ Ensure it's empty before loading

        // ✅ Predefined Categories & Ingredients (with emojis)
        categoryList.add("🥦 Vegetables");
        categoryList.add("🍎 Fruits");
        categoryList.add("🌾 Grains & Legumes");
        categoryList.add("🍗 Proteins");
        categoryList.add("🧀 Dairy");
        categoryList.add("🌿 Herbs & Spices");
        categoryList.add("🛢️ Oils & Condiments");
        categoryList.add("🆕 Others"); // ✅ Dynamic category for user-added ingredients

        ingredientMap.put("🥦 Vegetables", new ArrayList<>(List.of("🥕 Carrot", "🥦 Broccoli", "🌿 Spinach", "🍅 Tomato", "🧅 Onion", "🧄 Garlic", "🌶 Bell Pepper", "🥒 Zucchini", "🥬 Cabbage", "🥬 Kale", "🥗 Lettuce", "🥔 Cauliflower")));
        ingredientMap.put("🍎 Fruits", new ArrayList<>(List.of("🍏 Apple", "🍌 Banana", "🍊 Orange", "🍓 Strawberries", "🍇 Grapes", "🥭 Mango", "🍍 Pineapple", "🍋 Lemon/Lime")));
        ingredientMap.put("🌾 Grains & Legumes", new ArrayList<>(List.of("🍚 Rice", "🌾 Quinoa", "🥣 Oats", "🌰 Lentils", "🫘 Chickpeas", "🌽 Corn", "🥜 Peanuts")));
        ingredientMap.put("🍗 Proteins", new ArrayList<>(List.of("🍗 Chicken", "🥩 Beef", "🐖 Pork", "🐟 Fish", "🍳 Eggs", "🌱 Tofu", "🫘 Beans")));
        ingredientMap.put("🧀 Dairy", new ArrayList<>(List.of("🥛 Milk", "🍦 Yogurt", "🧀 Cheese", "🧈 Butter", "🥥 Coconut Milk", "🌱 Soy/Oat Milk")));
        ingredientMap.put("🌿 Herbs & Spices", new ArrayList<>(List.of("🌿 Basil", "🌿 Oregano", "🌿 Thyme", "🌿 Rosemary", "🧂 Salt", "🌶 Chili Powder", "🟠 Turmeric", "🟡 Ginger", "🟤 Cumin")));
        ingredientMap.put("🛢️ Oils & Condiments", new ArrayList<>(List.of("🫒 Olive Oil", "🥥 Coconut Oil", "🥫 Soy Sauce", "🔥 Hot Sauce", "🍯 Honey", "🥄 Mayonnaise", "🍶 Vinegar", "🧂 Salt", "🍚 Sugar")));
        ingredientMap.put("🆕 Others", new ArrayList<>()); // ✅ Allow dynamic user-added ingredients

        // ✅ Load Selected Ingredients from Recipe (if available)
        if (recipe != null && recipe.getIngredients() != null) {
            Log.d("checkrecipe", "Recipe Ingredients: " + recipe.getIngredients().toString());

            for (Map.Entry<String, List<String>> entry : recipe.getIngredients().entrySet()) {
                String category = entry.getKey(); // Category from recipe (with emojis)
                List<String> ingredients = entry.getValue(); // Ingredients from recipe (with emojis)

                // ✅ Find the corresponding category in ingredientMap
                if (ingredientMap.containsKey(category)) {
                    // ✅ Add ingredients with emojis to the selectedIngredients list
                    for (String ingredient : ingredients) {
                        String ingredientWithoutEmoji = removeEmojis(ingredient); // Remove emojis for comparison
                        String emojiIngredient = findIngredientWithEmoji(category, ingredientWithoutEmoji);
                        if (emojiIngredient != null) {
                            selectedIngredients.add(emojiIngredient);
                        }
                    }

                    // ✅ Ensure "Others" category captures dynamically added ingredients
                    if (category.equals("🆕 Others") && !ingredients.isEmpty()) {
                        for (String ingredient : ingredients) {
                            // ✅ Add only if the ingredient is not already in the "Others" category
                            if (!ingredientMap.get("🆕 Others").contains(ingredient)) {
                                ingredientMap.get("🆕 Others").add(ingredient);
                            }
                            // ✅ Mark the ingredient as selected
                            if (!selectedIngredients.contains(ingredient)) {
                                selectedIngredients.add(ingredient);
                            }
                        }
                    }
                }
            }
        }

        // ✅ Log selectedIngredients for debugging
        Log.d("SelectedIngredients", "Selected Ingredients: " + selectedIngredients.toString());

        // ✅ Initialize the ExpandableListAdapter
        expandableListAdapter = new CategoryExpandableListAdapter(RecipeEditActivity.this, categoryList, ingredientMap, selectedIngredients);

        // ✅ Set the adapter to the ExpandableListView
        expandableListView.setAdapter(expandableListAdapter);

        // ✅ Handle ingredient selection
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedIngredient = ingredientMap.get(categoryList.get(groupPosition)).get(childPosition);

            if (!selectedIngredients.contains(selectedIngredient)) {
                selectedIngredients.add(selectedIngredient);
                showSnackbar("added!");
            } else {
                selectedIngredients.remove(selectedIngredient);
                showSnackbar("removed!");
            }

            // ✅ Notify the adapter of changes
            expandableListAdapter.notifyDataSetChanged();

            return true;
        });
    }

    // ✅ Helper method to find the ingredient with emojis
    private String findIngredientWithEmoji(String category, String ingredientWithoutEmoji) {
        List<String> ingredients = ingredientMap.get(category);
        if (ingredients != null) {
            for (String emojiIngredient : ingredients) {
                if (removeEmojis(emojiIngredient).equals(ingredientWithoutEmoji)) {
                    return emojiIngredient;
                }
            }
        }
        return null;
    }

    // ✅ Helper method to remove emojis from a string
    private String removeEmojis(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();
    }
    /**
     * Sets up the RecyclerView for dynamic instructions.
     */
    private void setupRecyclerView() {
        instructionList = new ArrayList<>();
        instructionAdapter = new InstructionAdapter(instructionList, position -> removeInstruction(position));

        recyclerView.setLayoutManager(new LinearLayoutManager(RecipeEditActivity.this));
        recyclerView.setAdapter(instructionAdapter);
    }

    /**
     * Adds a new instruction step dynamically.
     */
    private void addInstruction() {
        instructionList.add(new InstructionStep(instructionList.size() + 1, ""));
        instructionAdapter.notifyItemInserted(instructionList.size() - 1);
    }

    /**
     * Removes an instruction step dynamically.
     */
    private void removeInstruction(int position) {
        instructionList.remove(position);
        instructionAdapter.notifyDataSetChanged();
    }



    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.primary_variant))
                .setTextColor(getResources().getColor(R.color.white))
                .show();
    }


}