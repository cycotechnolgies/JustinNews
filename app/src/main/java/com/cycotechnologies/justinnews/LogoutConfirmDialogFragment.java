package com.cycotechnologies.justinnews;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LogoutConfirmDialogFragment extends DialogFragment {

    // Listener to communicate back to the hosting activity/fragment
    public interface LogoutConfirmDialogListener {
        void onLogoutConfirmed();
    }

    private LogoutConfirmDialogListener listener;

    public static LogoutConfirmDialogFragment newInstance() {
        return new LogoutConfirmDialogFragment();
    }

    // This method is called when the fragment is first attached to its activity or parent fragment.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Try to get the listener from the parent fragment first
        if (getParentFragment() instanceof LogoutConfirmDialogListener) {
            listener = (LogoutConfirmDialogListener) getParentFragment();
        } else if (context instanceof LogoutConfirmDialogListener) {
            // Fallback to getting the listener from the hosting activity
            listener = (LogoutConfirmDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement LogoutConfirmDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this dialog
        View view = inflater.inflate(R.layout.fragment_logout_confirm_dialog, container, false);

        Button confirmLogoutButton = view.findViewById(R.id.confirmLogoutButton);
        Button cancelLogoutButton = view.findViewById(R.id.cancelLogoutButton);

        confirmLogoutButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLogoutConfirmed();
            }
            dismiss(); // Dismiss the dialog after confirmation
        });

        cancelLogoutButton.setOnClickListener(v -> dismiss()); // Just dismiss the dialog

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Set Dialog Position and Width (Same as Edit Profile Dialog) ---
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawableResource(R.drawable.rounded_dialog_background); // Use your existing rounded background

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM; // Align to bottom
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT; // Make it full width
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Wrap content for height
            window.setAttributes(layoutParams);
        }
    }
}