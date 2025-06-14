package com.cycotechnologies.justinnews;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class NewsArticleFragment extends Fragment {

    private ImageView detailNewsImage;
    private TextView detailNewsTitle, detailNewsDate, detailNewsSummary; // Removed likeCountTextView
    private ImageButton btnLike, btnShare, btnSave, backBtn;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String newsId;
    private boolean isLiked = false;
    private boolean isSaved = false;
    private static final String ARG_NEWS_ITEM = "news_item";

    private Object newsItem;

    private static final String TAG = "NewsArticleFragment"; // TAG for logging

    public NewsArticleFragment() {
        // Required empty public constructor
    }

    public static NewsArticleFragment newInstance(Serializable newsItem) {
        NewsArticleFragment fragment = new NewsArticleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NEWS_ITEM, newsItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            newsItem = getArguments().getSerializable(ARG_NEWS_ITEM);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Log current user status
        if (currentUser != null) {
            Log.d(TAG, "Current User: " + currentUser.getUid());
        } else {
            Log.d(TAG, "No user logged in.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news_article, container, false);

        detailNewsImage = view.findViewById(R.id.detailNewsImage);
        detailNewsTitle = view.findViewById(R.id.detailNewsTitle);
        detailNewsDate = view.findViewById(R.id.detailNewsDate);
        detailNewsSummary = view.findViewById(R.id.detailNewsSummary);
        btnLike = view.findViewById(R.id.btnLike);
        btnShare = view.findViewById(R.id.btnShare);
        btnSave = view.findViewById(R.id.btnSave);
        backBtn = view.findViewById(R.id.btnBack);
        // Removed: likeCountTextView = view.findViewById(R.id.likeCount);


        if (newsItem instanceof TrendNews) {
            TrendNews news = (TrendNews) newsItem;
            newsId = news.getNewsId(); // IMPORTANT: Get newsId from the object
            displayNewsDetails(news.getTitle(), news.getDateCreated(), news.getImageUrl(), news.getSummary(), news.getNewsId());
            Log.d(TAG, "News ID from TrendNews: " + newsId);
        } else if (newsItem instanceof NewsForYouItem) {
            NewsForYouItem news = (NewsForYouItem) newsItem;
            newsId = news.getNewsId(); // IMPORTANT: Get newsId from the object
            displayNewsDetails(news.getTitle(), news.getDateCreated(), news.getImageUrl(), news.getSummary(), news.getNewsId());
            Log.d(TAG, "News ID from NewsForYouItem: " + newsId);
        } else {
            Toast.makeText(getContext(), "Error loading news details: News item is null or incorrect type.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: newsItem is null or not TrendNews/NewsForYouItem. Type: " + (newsItem != null ? newsItem.getClass().getName() : "null"));
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
            return view;
        }

        // Check if newsId is valid
        if (newsId == null || newsId.isEmpty()) {
            Toast.makeText(getContext(), "Error: News ID is missing.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "News ID is null or empty. Cannot perform Firestore operations.");
            btnLike.setEnabled(false);
            btnSave.setEnabled(false);
        }


        if (currentUser != null) {
            checkLikeStatus();
            checkSaveStatus();
        } else {
            btnLike.setEnabled(false);
            btnSave.setEnabled(false);
            Toast.makeText(getContext(), "Please log in to like or save news.", Toast.LENGTH_SHORT).show();
        }

        btnLike.setOnClickListener(v -> toggleLike());
        btnSave.setOnClickListener(v -> toggleSave());
        btnShare.setOnClickListener(v -> shareNews());

        backBtn.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void displayNewsDetails(String title, String date, String imageUrl, String summary, String id) {
        detailNewsTitle.setText(title);
        detailNewsDate.setText(date);
        detailNewsSummary.setText(summary);
        // newsId is already set in onCreateView from the newsItem object
        Log.d(TAG, "Displaying details for News ID: " + id);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.ic_profile_background)
                    .error(R.drawable.ic_profile_background)
                    .into(detailNewsImage);
        } else {
            detailNewsImage.setImageResource(R.drawable.ic_profile_background); // Default image
            Log.w(TAG, "Image URL is null or empty for newsId: " + id);
        }
    }

    // --- Like/Save/Share Logic ---

    private void checkLikeStatus() {
        if (currentUser == null || newsId == null || newsId.isEmpty()) return;
        String userId = currentUser.getUid();
        Log.d(TAG, "Checking like status for userId: " + userId + ", newsId: " + newsId);
        db.collection("User").document(userId).collection("likedNews").document(newsId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLiked = documentSnapshot.exists();
                    updateLikeButtonUI();
                    Log.d(TAG, "Like status for newsId " + newsId + ": " + isLiked);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking like status", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error checking like status: " + e.getMessage(), e);
                });
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_heart_blue); // Assuming this is your filled heart
        } else {
            btnLike.setImageResource(R.drawable.ic_heart); // Assuming this is your outline heart
        }
    }

    private void toggleLike() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to like news.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newsId == null || newsId.isEmpty()) {
            Toast.makeText(getContext(), "Error: News ID is missing for like operation.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "News ID is null or empty when trying to toggle like.");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference likeRef = db.collection("User").document(userId).collection("likedNews").document(newsId);
        Log.d(TAG, "Attempting to toggle like for userId: " + userId + ", newsId: " + newsId + ". Current liked status: " + isLiked);

        if (isLiked) {
            // Unlike: Remove from Firestore
            likeRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        isLiked = false;
                        updateLikeButtonUI();
                        Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "News " + newsId + " unliked successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error unliking news", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error unliking news " + newsId + ": " + e.getMessage(), e);
                    });
        } else {
            // Like: Add to Firestore
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("newsId", newsId);
            likeData.put("timestamp", FieldValue.serverTimestamp());

            likeRef.set(likeData)
                    .addOnSuccessListener(aVoid -> {
                        isLiked = true;
                        updateLikeButtonUI();
                        Toast.makeText(getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "News " + newsId + " liked successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error liking news", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error liking news " + newsId + ": " + e.getMessage(), e);
                    });
        }
    }

    private void checkSaveStatus() {
        if (currentUser == null || newsId == null || newsId.isEmpty()) return;
        String userId = currentUser.getUid();
        Log.d(TAG, "Checking save status for userId: " + userId + ", newsId: " + newsId);
        db.collection("User").document(userId).collection("savedNews").document(newsId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isSaved = documentSnapshot.exists();
                    updateSaveButtonUI();
                    Log.d(TAG, "Save status for newsId " + newsId + ": " + isSaved);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking save status", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error checking save status: " + e.getMessage(), e);
                });
    }

    private void updateSaveButtonUI() {
        if (isSaved) {
            btnSave.setImageResource(R.drawable.ic_save_blue); // Assuming this is your filled save icon
        } else {
            btnSave.setImageResource(R.drawable.ic_save); // Assuming this is your outline save icon
        }
    }

    private void toggleSave() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to save news.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newsId == null || newsId.isEmpty()) {
            Toast.makeText(getContext(), "Error: News ID is missing for save operation.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "News ID is null or empty when trying to toggle save.");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference saveRef = db.collection("User").document(userId).collection("savedNews").document(newsId);
        Log.d(TAG, "Attempting to toggle save for userId: " + userId + ", newsId: " + newsId + ". Current saved status: " + isSaved);

        if (isSaved) {
            // Unsave: Remove from Firestore
            saveRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        isSaved = false;
                        updateSaveButtonUI();
                        Toast.makeText(getContext(), "Unsaved", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "News " + newsId + " unsaved successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error unsaving news", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error unsaving news " + newsId + ": " + e.getMessage(), e);
                    });
        } else {
            // Save: Add to Firestore
            Map<String, Object> saveData = new HashMap<>();
            saveData.put("newsId", newsId);
            saveData.put("timestamp", FieldValue.serverTimestamp());

            saveRef.set(saveData)
                    .addOnSuccessListener(aVoid -> {
                        isSaved = true;
                        updateSaveButtonUI();
                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "News " + newsId + " saved successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving news", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error saving news " + newsId + ": " + e.getMessage(), e);
                    });
        }
    }

    private void shareNews() {
        String newsTitle = detailNewsTitle.getText().toString();
        String newsSummary = detailNewsSummary.getText().toString();
        String shareText = "Check out this news: " + newsTitle + "\n\n" + newsSummary + "\nRead more: [Link to original news or your app's news detail]";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, newsTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share News Via"));
    }
}