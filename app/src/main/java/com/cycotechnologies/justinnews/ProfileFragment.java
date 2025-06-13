package com.cycotechnologies.justinnews; // Your package name

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass for the user profile page.
 * Implements {@link EditProfileDialogFragment.EditProfileDialogListener} to handle updates from the dialog.
 */
public class ProfileFragment extends Fragment implements EditProfileDialogFragment.EditProfileDialogListener, LogoutConfirmDialogFragment.LogoutConfirmDialogListener{

    private static final String TAG = "ProfileFragment";

    // UI elements
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private CardView editProfileCard;
    private MaterialButton signOutButton;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private static final String PREFS_NAME = "MyAuthPrefs";

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fragment created.");
        // Initialize Firebase instances here
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: Inflating fragment_profile.xml layout.");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI elements from the inflated view
        userNameTextView = view.findViewById(R.id.userNameHolder);
        userEmailTextView = view.findViewById(R.id.emailHolder);
        editProfileCard = view.findViewById(R.id.editProfileCard);
        signOutButton = view.findViewById(R.id.signOutButton);

        // --- Debugging checks ---
        if (editProfileCard == null) {
            Log.e(TAG, "onCreateView: editProfileCard is null. Check ID in fragment_profile.xml");
        } else {
            Log.d(TAG, "onCreateView: editProfileCard found.");
            // Set click listener for the "Edit Profile" card
            editProfileCard.setOnClickListener(v -> {
                Log.d(TAG, "onClick: Edit Profile Card clicked.");
                showEditProfileDialog();
            });
        }

        if (signOutButton == null) {
            Log.e(TAG, "onCreateView: signOutButton is null. Check ID in fragment_profile.xml");
        } else {
            Log.d(TAG, "onCreateView: signOutButton found.");
            // Set click listener for the "Sign Out" button
            signOutButton.setOnClickListener(v -> showLogoutConfirmDialog());
        }
        // --- End debugging checks ---


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Fragment started. Checking user login status.");
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "onStart: User not logged in.");
            Toast.makeText(getContext(), "Please log in to view your profile.", Toast.LENGTH_LONG).show();
            // Optional: Redirect to login activity
            // If you have a LoginActivity, uncomment and replace
            /*
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class); // Replace LoginActivity.class
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
            */
        } else {
            Log.d(TAG, "onStart: User logged in. Loading profile.");
            // User is logged in, load profile data
            loadUserProfile();
        }
    }

    /**
     * Loads the current user's profile data from Firestore and displays it.
     * This method should be called on fragment start and after a successful profile update.
     */
    private void loadUserProfile() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("User").document(userId);
            Log.d(TAG, "loadUserProfile: Attempting to load profile for UID: " + userId);

            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");
                            if (email == null) {
                                email = currentUser.getEmail();
                            }
                            userNameTextView.setText(name != null ? name : "N/A");
                            userEmailTextView.setText(email != null ? email : "N/A");
                            Log.d(TAG, "loadUserProfile: Profile data loaded - Name: " + name + ", Email: " + email);
                        } else {
                            userNameTextView.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "New User");
                            userEmailTextView.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No Email");
                            Log.d(TAG, "loadUserProfile: User document does not exist in Firestore for UID: " + userId + ". Displaying Auth data.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "loadUserProfile: Error loading user profile: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                        userNameTextView.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Error User");
                        userEmailTextView.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Error Email");
                    });
        } else {
            Log.w(TAG, "loadUserProfile: currentUser is null, cannot load profile.");
        }
    }

    /**
     * Shows the EditProfileDialogFragment.
     * It passes the current name and email to pre-fill the dialog fields.
     */
    private void showEditProfileDialog() {
        String currentName = userNameTextView.getText().toString();
        String currentEmail = userEmailTextView.getText().toString();

        Log.d(TAG, "showEditProfileDialog: Attempting to show dialog with Name: " + currentName + ", Email: " + currentEmail);

        EditProfileDialogFragment dialog = EditProfileDialogFragment.newInstance(currentName, currentEmail);

        // --- Crucial part for fragments: using the correct FragmentManager ---
        // Use getChildFragmentManager() if this dialog should be part of this fragment's lifecycle
        // and destroyed with it. This is generally preferred for dialogs launched from a fragment.
        FragmentManager fragmentManager = getChildFragmentManager();

        // As a fallback or if you intend for the dialog to persist across fragment transactions
        // or be managed by the activity, you could use:
        // FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        if (fragmentManager != null) {
            dialog.show(fragmentManager, "EditProfileDialogFragment");
            Log.d(TAG, "showEditProfileDialog: Dialog show method called successfully.");
        } else {
            Log.e(TAG, "showEditProfileDialog: FragmentManager is null. Cannot show dialog.");
            Toast.makeText(getContext(), "Error displaying edit dialog.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback method from EditProfileDialogFragment when the user clicks "Update".
     * This method handles updating the user's profile in Firebase Firestore.
     *
     * @param newName The new name entered by the user.
     * @param newEmail The new email entered by the user.
     */
    @Override
    public void onProfileUpdate(String newName, String newEmail) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("User").document(userId);
            Log.d(TAG, "onProfileUpdate: Updating profile for UID: " + userId + " with Name: " + newName + ", Email: " + newEmail);

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", newName);
            updates.put("email", newEmail); // Also update email in Firestore document

            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onProfileUpdate: Firestore document updated successfully.");

                        // Update Firebase Authentication email (optional but recommended for consistency)
                        if (!newEmail.equals(currentUser.getEmail())) {
                            Log.d(TAG, "onProfileUpdate: Email changed, attempting to update Firebase Auth email.");
                            currentUser.updateEmail(newEmail)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onProfileUpdate: User email address updated in Auth.");
                                            loadUserProfile(); // Refresh UI after Auth email updates
                                        } else {
                                            Log.e(TAG, "onProfileUpdate: Error updating email in Auth: " + task.getException().getMessage(), task.getException());
                                            Toast.makeText(getContext(), "Email update failed in authentication. Please re-login.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Log.d(TAG, "onProfileUpdate: Email not changed, refreshing profile from Firestore.");
                            loadUserProfile(); // Just refresh UI from Firestore data
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "onProfileUpdate: Error updating document in Firestore: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "onProfileUpdate: currentUser is null, authentication error.");
            Toast.makeText(getContext(), "Authentication error. Please re-login.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles user sign out.
     */
    private void showLogoutConfirmDialog() {
        Log.d(TAG, "showLogoutConfirmDialog: Displaying logout confirmation dialog.");
        LogoutConfirmDialogFragment dialog = LogoutConfirmDialogFragment.newInstance();
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager != null) {
            dialog.show(fragmentManager, "LogoutConfirmDialogFragment");
        } else {
            Log.e(TAG, "showLogoutConfirmDialog: FragmentManager is null. Cannot show logout dialog.");
            Toast.makeText(getContext(), "Error displaying logout dialog.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLogoutConfirmed() {
        Log.d(TAG, "onLogoutConfirmed: User confirmed logout.");

        // 1. Clear SharedPreferences
        if (getContext() != null) {
            SharedPreferences preferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            Log.d(TAG, "onLogoutConfirmed: SharedPreferences cleared.");
        } else {
            Log.e(TAG, "onLogoutConfirmed: Context is null, cannot clear SharedPreferences.");
        }

        // 2. Log out from Firebase Authentication
        mAuth.signOut();
        Log.d(TAG, "onLogoutConfirmed: Firebase user signed out.");
        Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
            Log.d(TAG, "onLogoutConfirmed: Redirected to LoginActivity.");
        } else {
            Log.e(TAG, "onLogoutConfirmed: getActivity() is null, cannot redirect after logout.");
        }
    }
}