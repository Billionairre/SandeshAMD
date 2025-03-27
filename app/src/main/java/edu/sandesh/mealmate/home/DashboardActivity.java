package edu.sandesh.mealmate.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.sandesh.mealmate.R;
import edu.sandesh.mealmate.fragments.AddRecipeFragment;
import edu.sandesh.mealmate.fragments.HomeFragment;
import edu.sandesh.mealmate.fragments.ProfileFragment;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        loadLocalProfile(); // Load User Profile Data

        // Default Fragment (Home Screen)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new HomeFragment())
                    .commit();
        }

        // Handle Bottom Navigation Item Clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            else if (item.getItemId() == R.id.nav_add) {
                selectedFragment = new AddRecipeFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Floating Action Button Click (Opens Add Recipe Fragment)

    }

    void loadLocalProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", null);
        String name = sharedPreferences.getString("USER_NAME", null);
        String mobile = sharedPreferences.getString("USER_MOBILE", null);
        String dob = sharedPreferences.getString("USER_DOB", null);
        String gender = sharedPreferences.getString("USER_GENDER", null);
        String photoUrl = sharedPreferences.getString("USER_PHOTO", null);

        Log.d("userID", userId);
        Log.d("name", name);
        Log.d("mobile", mobile);
        Log.d("dob", dob);
        Log.d("gender", gender);
        Log.d("photoUrl", photoUrl);
    }




}
