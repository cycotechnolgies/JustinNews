package com.cycotechnologies.justinnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
// import android.view.View; // Unused
// import android.widget.Switch; // Unused

// import androidx.activity.EdgeToEdge; // Unused
import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.graphics.Insets; // Unused
// import androidx.core.view.ViewCompat; // Unused
// import androidx.core.view.WindowInsetsCompat; // Unused
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cycotechnologies.justinnews.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "shared_prefs";
    public static final String EMAIL_KEY = "email_key";
    public static final String PASSWORD_KEY = "password_key";
    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(EMAIL_KEY, null);
        String password = sharedPreferences.getString(PASSWORD_KEY, null);

        if(email == null || password == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initially load the HomeFragment when the activity starts
        replaceFragment(new HomeFragment());

        binding.BottomNavView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.home_tab) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.feature_tab) {
                replaceFragment(new FeaturedFragment());
            } else if (itemId == R.id.saved_tab) {
                replaceFragment(new SavedFragment());
            } else if (itemId == R.id.profile_tab) {
                replaceFragment(new ProfileFragment());
            }

            return true; // Return true to indicate that the item selection has been handled
        });

    }

    /**
     * Replaces the current fragment in the frame_views container with the specified fragment.
     * @param fragment The new fragment to display.
     */
    private void replaceFragment(Fragment fragment){
        // Get the FragmentManager to manage fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Begin a FragmentTransaction to perform fragment operations
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the existing fragment in R.id.frame_views with the new fragment
        fragmentTransaction.replace(R.id.frame_views, fragment);
        // Commit the transaction to apply the changes
        fragmentTransaction.commit();
    }
}