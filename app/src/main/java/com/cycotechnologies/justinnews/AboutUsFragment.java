package com.cycotechnologies.justinnews; // Your package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Added import for Nullable
import androidx.cardview.widget.CardView; // Added import for CardView
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Added import for Toast

/**
 * A simple {@link Fragment} subclass for the About Us page.
 * Use the {@link AboutUsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutUsFragment extends Fragment {

    // UI elements from the layout
    private CardView contactUsCard;
    private CardView privacyPolicyCard;

    public AboutUsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment. No parameters are needed as the content is static.
     *
     * @return A new instance of fragment AboutUsFragment.
     */
    public static AboutUsFragment newInstance() {
        AboutUsFragment fragment = new AboutUsFragment();
        // No arguments needed for static content
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { // Changed to @Nullable
        super.onCreate(savedInstanceState);
        // No arguments to retrieve since newInstance() no longer takes params
        // if (getArguments() != null) { ... } // Removed this block
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, // Changed to @Nullable
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        // Initialize UI elements from the inflated view
        contactUsCard = view.findViewById(R.id.contactUsCard);
        privacyPolicyCard = view.findViewById(R.id.privacyPolicyCard);

        // Set click listeners for the cards
        if (contactUsCard != null) {
            contactUsCard.setOnClickListener(v -> handleContactUsClick());
        }
        if (privacyPolicyCard != null) {
            privacyPolicyCard.setOnClickListener(v -> handlePrivacyPolicyClick());
        }

        // You can set images or dynamic content here if needed, e.g.:
        // developerProfileImage.setImageResource(R.drawable.your_actual_developer_pic);

        return view;
    }

    /**
     * Handles the click on the "Contact Us" card.
     * Opens an email client to send an email to the developer.
     */
    private void handleContactUsClick() {
        // Replace "cycotechnologies@gmail.com" with your actual contact email
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:cycotechnologies@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about Justin News App");

        // Check if there's an app to handle this intent
        if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(getContext(), "No email app found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the click on the "Privacy and Policies" card.
     * Opens a web browser to the privacy policy URL.
     */
    private void handlePrivacyPolicyClick() {
        // Replace "https://www.yourwebsite.com/privacy-policy" with your actual privacy policy URL
        String privacyPolicyUrl = "https://www.yourwebsite.com/privacy-policy";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));

        // Check if there's an app to handle this intent
        if (browserIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(browserIntent);
        } else {
            Toast.makeText(getContext(), "No browser found.", Toast.LENGTH_SHORT).show();
        }
    }
}