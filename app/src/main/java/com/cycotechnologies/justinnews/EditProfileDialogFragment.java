package com.cycotechnologies.justinnews; // Your package name

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment; // Import Fragment

import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

public class EditProfileDialogFragment extends DialogFragment {

    private static final String ARG_NAME = "current_name";
    private static final String ARG_EMAIL = "current_email";

    private TextInputEditText editNameInput;
    private TextInputEditText editEmailInput;
    private Button updateButton;
    private Button cancelButton;

    // Listener to communicate back to the hosting activity/fragment
    public interface EditProfileDialogListener {
        void onProfileUpdate(String newName, String newEmail);
    }

    private EditProfileDialogListener listener;

    /**
     * Factory method to create a new instance of the dialog.
     * Pass initial name and email to pre-fill the fields.
     * @param currentName The current user's name.
     * @param currentEmail The current user's email.
     * @return A new instance of EditProfileDialogFragment.
     */
    public static EditProfileDialogFragment newInstance(String currentName, String currentEmail) {
        EditProfileDialogFragment fragment = new EditProfileDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, currentName);
        args.putString(ARG_EMAIL, currentEmail);
        fragment.setArguments(args);
        return fragment;
    }

    // This method is called when the fragment is first attached to its activity or parent fragment.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Try to get the listener from the parent fragment first
        if (getParentFragment() instanceof EditProfileDialogListener) {
            listener = (EditProfileDialogListener) getParentFragment();
        } else if (context instanceof EditProfileDialogListener) {
            // Fallback to getting the listener from the hosting activity
            listener = (EditProfileDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement EditProfileDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this dialog
        View view = inflater.inflate(R.layout.dialog_edit_profile, container, false);

        // Initialize UI elements from the dialog layout
        editNameInput = view.findViewById(R.id.username_input);
        editEmailInput = view.findViewById(R.id.email_input);
        updateButton = view.findViewById(R.id.updateButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Pre-fill fields with current user data passed from the fragment/activity
        if (getArguments() != null) {
            editNameInput.setText(getArguments().getString(ARG_NAME));
            editEmailInput.setText(getArguments().getString(ARG_EMAIL));
        }

        // Set click listener for the Update button
        updateButton.setOnClickListener(v -> handleUpdateClick());

        // Set click listener for the Cancel button
        cancelButton.setOnClickListener(v -> dismiss()); // Dismiss the dialog

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /// --- FIX FOR DIALOG POSITION AND WIDTH ---
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawableResource(R.drawable.rounded_dialog_background); // Apply rounded background

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM; // Align to bottom
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT; // Make it full width
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Wrap content for height
            window.setAttributes(layoutParams);

            // Optional: Add some margin from the bottom if desired
            // layoutParams.bottomMargin = (int) (getResources().getDisplayMetrics().density * 16); // 16dp margin from bottom
            // window.setAttributes(layoutParams);
        }
    }

    /**
     * Handles the click event for the Update button.
     * Performs input validation and then notifies the activity.
     */
    private void handleUpdateClick() {
        String newName = editNameInput.getText().toString().trim();
        String newEmail = editEmailInput.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(newName)) {
            editNameInput.setError("Name cannot be empty");
            editNameInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newEmail)) {
            editEmailInput.setError("Email cannot be empty");
            editEmailInput.requestFocus();
            return;
        }

        // *** USING YOUR VALIDATOR CLASS HERE ***
        if (!validator.isValidEmail(newEmail)) {
            editEmailInput.setError("Please enter a valid email address");
            editEmailInput.requestFocus();
            return;
        }

        // If validation passes, notify the listener (ProfileFragment or hosting Activity)
        if (listener != null) {
            listener.onProfileUpdate(newName, newEmail);
        }
        dismiss(); // Close the dialog after initiating the update
    }

    /**
     * Basic email validation.
     * @param email The email string to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return Pattern.compile(
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        ).matcher(email).matches();
    }
}