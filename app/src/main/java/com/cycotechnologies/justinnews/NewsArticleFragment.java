package com.cycotechnologies.justinnews;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull; // Add this import for @NonNull
import androidx.annotation.Nullable; // Add this import for @Nullable
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
import com.google.firebase.firestore.FieldValue; // Add this import for FieldValue.serverTimestamp()
import com.squareup.picasso.Picasso; // Add this import for Picasso

import java.util.HashMap; // Add this import
import java.util.Map;     // Add this import
import java.io.Serializable; // Keep this import for passing objects

public class NewsArticleFragment extends Fragment {

    private ImageView detailNewsImage;
    private TextView detailNewsTitle, detailNewsDate, detailNewsSummary;
    private ImageButton btnLike, btnShare, btnSave, backBtn;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String newsId;
    private boolean isLiked = false;
    private boolean isSaved = false;
    private static final String ARG_NEWS_ITEM = "news_item";

    private Object newsItem;

    public NewsArticleFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided news item.
     *
     * @param newsItem The news object (TrendNews or NewsForYouItem) to display.
     * @return A new instance of fragment NewsArticleFragment.
     */
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

        View view = inflater.inflate(R.layout.fragment_news_article, container, false); // Assuming you use the activity_news_detail layout

        detailNewsImage = view.findViewById(R.id.detailNewsImage);
        detailNewsTitle = view.findViewById(R.id.detailNewsTitle);
        detailNewsDate = view.findViewById(R.id.detailNewsDate);
        detailNewsSummary = view.findViewById(R.id.detailNewsSummary);
        btnLike = view.findViewById(R.id.btnLike);
        btnShare = view.findViewById(R.id.btnShare);
        btnSave = view.findViewById(R.id.btnSave);
        backBtn = view.findViewById(R.id.btnBack);


        if (newsItem instanceof TrendNews) {
            TrendNews news = (TrendNews) newsItem;
            displayNewsDetails(news.getTitle(), news.getDateCreated(), news.getImageUrl(), news.getSummary(), news.getNewsId());
        } else if (newsItem instanceof NewsForYouItem) {
            NewsForYouItem news = (NewsForYouItem) newsItem;
            displayNewsDetails(news.getTitle(), news.getDateCreated(), news.getImageUrl(), news.getSummary(), news.getNewsId());
        } else {
            Toast.makeText(getContext(), "Error loading news details", Toast.LENGTH_SHORT).show();

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
            return view;
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
        newsId = id;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.ic_profile_background)
                    .error(R.drawable.ic_profile_background)
                    .into(detailNewsImage);
        } else {
            detailNewsImage.setImageResource(R.drawable.ic_profile_background); // Default image
        }
    }

    // --- Like/Save/Share Logic ---

    private void checkLikeStatus() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("likedNews").document(newsId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLiked = documentSnapshot.exists();
                    updateLikeButtonUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking like status", Toast.LENGTH_SHORT).show(); // Use getContext()
                });
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_heart_blue);
        } else {
            btnLike.setImageResource(R.drawable.ic_heart);
        }
    }

    private void toggleLike() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to like news.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference likeRef = db.collection("users").document(userId).collection("likedNews").document(newsId);

        if (isLiked) {
            likeRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        isLiked = false;
                        updateLikeButtonUI();
                        Toast.makeText(getContext(), "Unliked", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error unliking news", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("newsId", newsId);
            likeData.put("timestamp", FieldValue.serverTimestamp());

            likeRef.set(likeData)
                    .addOnSuccessListener(aVoid -> {
                        isLiked = true;
                        updateLikeButtonUI();
                        Toast.makeText(getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error liking news", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void checkSaveStatus() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("savedNews").document(newsId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isSaved = documentSnapshot.exists();
                    updateSaveButtonUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking save status", Toast.LENGTH_SHORT).show(); // Use getContext()
                });
    }

    private void updateSaveButtonUI() {
        if (isSaved) {
            btnSave.setImageResource(R.drawable.ic_save_blue);
        } else {
            btnSave.setImageResource(R.drawable.ic_save);
        }
    }

    private void toggleSave() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to save news.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference saveRef = db.collection("users").document(userId).collection("savedNews").document(newsId);

        if (isSaved) {
            // Unsave: Remove from Firestore
            saveRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        isSaved = false;
                        updateSaveButtonUI();
                        Toast.makeText(getContext(), "Unsaved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error unsaving news", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> saveData = new HashMap<>();
            saveData.put("newsId", newsId);
            saveData.put("timestamp", FieldValue.serverTimestamp());

            saveRef.set(saveData)
                    .addOnSuccessListener(aVoid -> {
                        isSaved = true;
                        updateSaveButtonUI();
                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving news", Toast.LENGTH_SHORT).show();
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