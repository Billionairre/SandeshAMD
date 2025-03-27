package edu.sandesh.mealmate.home;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.sandesh.mealmate.CustomProgressDialog;
import edu.sandesh.mealmate.R;
import okhttp3.*;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final String IMGUR_CLIENT_ID = "e115074dd9cfe62"; // Replace with your Client ID
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image";
    AutoCompleteTextView etGender;
    private ImageView profileImage;
    private EditText etFullName, etDob, etMobile, etAge;
    private Button btnUploadPhoto, btnSaveProfile;
    private Uri selectedImageUri = null;
    private Uri cameraImageUri;
    private String uploadedImageUrl = "";  // Default empty if no image selected
    private String fullName, dob, mobile, age;
    private CustomProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileImage = findViewById(R.id.profileImage);
        etFullName = findViewById(R.id.etFullName);
        etMobile = findViewById(R.id.etMobile);
        etGender = findViewById(R.id.etGender);
        etDob = findViewById(R.id.etDob);
        etAge = findViewById(R.id.etAge);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Initialize progress dialog
        progressDialog = new CustomProgressDialog(this);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Setup the Gender dropdown
        setupGenderDropdown();

        // Setup DatePicker for DOB
        setupDatePicker();

        // Load existing profile data if available
        loadUserProfile();

        // Set click listeners
        btnUploadPhoto.setOnClickListener(v -> showImagePickerDialog());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    // 📅 Show Date Picker Dialog & Calculate Age
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int year = currentYear - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDob.setText(selectedDate);

                    // Calculate Age
                    int calculatedAge = currentYear - selectedYear;
                    etAge.setText(String.valueOf(calculatedAge));
                }, year, month, day);

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    // Show Image Picker for Camera & Gallery
    private void showImagePickerDialog() {
        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickGalleryIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        Intent chooser = Intent.createChooser(pickGalleryIntent, "Select or Capture Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
        startActivityForResult(chooser, REQUEST_GALLERY);
    }

    // Handle Image Selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                selectedImageUri = data.getData();
            } else if (requestCode == REQUEST_CAMERA) {
                selectedImageUri = cameraImageUri;
            }

            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    profileImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    showSnackbar("Error loading image.");
                }
            }
        }
    }

    // 🎯 Save Profile Data (Uploads Image if Available)
    private void saveProfile() {
        fullName = etFullName.getText().toString();
        dob = etDob.getText().toString();
        mobile = etMobile.getText().toString();
        age = etAge.getText().toString();
        String gender = etGender.getText().toString();

        if (fullName.isEmpty() || dob.isEmpty() || mobile.isEmpty()) {
            showSnackbar("Please fill in all fields.");
            return;
        }

        if (gender.isEmpty()) {
            gender = "Other"; // Default value
        }

        if (!isValidGender(gender)) {
            showSnackbar("Please select a valid gender from the dropdown.");
            return;
        }

        if (selectedImageUri != null) {
            uploadImageToImgur(selectedImageUri);
        } else {
            saveUserData("", gender);
        }
    }

    // 🌍 Upload Image to Imgur
    private void uploadImageToImgur(Uri imageUri) {
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
                    runOnUiThread(() -> saveUserData(imgUrl, etGender.getText().toString().trim())); // Pass trimmed gender
                } else {
                    runOnUiThread(() -> showSnackbar("Upload failed!"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> showSnackbar("Network error. Try again."));
            }
            progressDialog.show();
        }).start();
    }

    private File getFileFromUri(Uri uri) {
        try {
            // ✅ Create a temporary file in cache directory
            File tempFile = new File(getCacheDir(), "upload_image.jpg");
            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(tempFile);

            // ✅ Copy file contents
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

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

    // 🎯 Extract Image URL from Imgur Response
    private String extractImageUrl(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("data").getString("link");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 🎯 Save Data to Firebase Firestore
    private void saveUserData(String photoUrl, String gender) {
        progressDialog.show();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", null);

        if (userId == null) {
            showSnackbar("Error: User ID not found.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", fullName);
        user.put("mobile", mobile);
        user.put("dob", dob);
        user.put("gender", gender.trim()); // Trim the gender value
        user.put("photoUrl", photoUrl);

        db.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Profile saved to Firebase! ✅");

                    savePref(fullName,mobile,dob,gender.trim(),photoUrl);

                    // Navigate to DashboardActivity and remove all previous activities
                    Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Close the current activity
                })
                .addOnFailureListener(e -> showSnackbar("Error saving profile!"));
        progressDialog.dismiss();
    }

    private void savePref(String name, String mobile, String dob, String gender, String photoUrl){
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("USER_NAME", name);
        editor.putString("USER_MOBILE", mobile);
        editor.putString("USER_DOB", dob);
        editor.putString("USER_GENDER", gender);
        editor.putString("USER_PHOTO", photoUrl);

        editor.apply();
    }
    // ✅ Show Snackbar for Messages
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.primary_variant))
                .setTextColor(getResources().getColor(R.color.white))
                .show();
    }

    // ✅ Validate Gender
    private boolean isValidGender(String gender) {
        String[] validGenders = new String[]{"Male", "Female", "Other"};
        for (String validGender : validGenders) {
            if (validGender.equalsIgnoreCase(gender)) {
                return true;
            }
        }
        return false;
    }

    private void setupGenderDropdown() {
        String[] genders = new String[]{"Male", "Female", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item, // Use the custom layout
                genders
        );

        if (etGender != null) {
            etGender.setAdapter(adapter);

            // Show dropdown when clicked
            etGender.setOnClickListener(v -> etGender.showDropDown());
        } else {
           // Log.e("ProfileActivity", "Gender dropdown not found!");
        }
    }

    private void setupDatePicker() {
        etDob.setOnClickListener(v -> showDatePicker());
    }

    private void loadUserProfile() {
        // Get saved user data from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = sharedPref.getString("USER_NAME", "");
        String mobile = sharedPref.getString("USER_MOBILE", "");
        String dob = sharedPref.getString("USER_DOB", "");
        String gender = sharedPref.getString("USER_GENDER", "");
        String photoUrl = sharedPref.getString("USER_PHOTO", "");

        // Populate UI with saved data
        if (!TextUtils.isEmpty(name)) {
            etFullName.setText(name);
        }
        
        if (!TextUtils.isEmpty(mobile)) {
            etMobile.setText(mobile);
        }
        
        if (!TextUtils.isEmpty(gender)) {
            etGender.setText(gender);
        }
        
        if (!TextUtils.isEmpty(dob)) {
            etDob.setText(dob);
            calculateAge(dob);
        }
        
        // Load profile image if available
        if (!TextUtils.isEmpty(photoUrl)) {
            try {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.default_pic)
                    .error(R.drawable.default_pic)
                    .into(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateAge(String dobString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dob = sdf.parse(dobString);
            if (dob != null) {
                Calendar dobCal = Calendar.getInstance();
                dobCal.setTime(dob);
                
                Calendar today = Calendar.getInstance();
                
                int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
                
                if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                
                etAge.setText(String.valueOf(age));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}