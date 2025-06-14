package com.cycotechnologies.justinnews;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SavedFragment extends Fragment implements OnNewsClickListener {

    private static final String TAG = "SavedNewsFragment";

    private RecyclerView savedNewsRecyclerView;
    private SavedNewsAdapter savedNewsAdapter;
    private List<SavedNews> savedNewsList;
    private TextView noSavedNewsMessage;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private MainActivity hostActivity;

    public SavedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            hostActivity = (MainActivity) context;
        } else {
            throw new RuntimeException(context.toString() + " must be hosted by MainActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hostActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        savedNewsRecyclerView = view.findViewById(R.id.savedNewsRecyclerView);
        noSavedNewsMessage = view.findViewById(R.id.noSavedNewsMessage);
        progressBar = view.findViewById(R.id.progressBar);

        savedNewsList = new ArrayList<>();
        savedNewsAdapter = new SavedNewsAdapter(savedNewsList, getContext(), this);
        savedNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedNewsRecyclerView.setAdapter(savedNewsAdapter);

        if (currentUser == null) {
            noSavedNewsMessage.setText("Please log in to view your saved news.");
            noSavedNewsMessage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "You need to log in to see saved news.", Toast.LENGTH_LONG).show();
        } else {
            fetchSavedNews();
        }

        return view;
    }

    private void fetchSavedNews() {
        progressBar.setVisibility(View.VISIBLE);
        noSavedNewsMessage.setVisibility(View.GONE);
        savedNewsList.clear();

        String userId = currentUser.getUid();
        Log.d(TAG, "Fetching saved news for user: " + userId);

        db.collection("User").document(userId).collection("savedNews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoSavedMessage("No saved news found.");
                        return;
                    }

                    Set<String> uniqueNewsIds = new HashSet<>();
                    for (QueryDocumentSnapshot savedNewsDoc : queryDocumentSnapshots) {
                        String newsId = savedNewsDoc.getString("newsId");
                        Log.d(TAG, "Found saved newsId: " + newsId);
                        if (newsId != null) {
                            uniqueNewsIds.add(newsId);
                        }
                    }

                    if (uniqueNewsIds.isEmpty()) {
                        showNoSavedMessage("No valid news IDs found.");
                        return;
                    }

                    List<Task<DocumentSnapshot>> fetchNewsTasks = new ArrayList<>();
                    for (String newsId : uniqueNewsIds) {
                        Log.d(TAG, "Fetching details for newsId: " + newsId);
                        fetchNewsTasks.add(db.collection("Public_News").document(newsId).get());
                    }

                    Tasks.whenAllSuccess(fetchNewsTasks)
                            .addOnSuccessListener(results -> {
                                Map<String, SavedNews> uniqueNewsMap = new HashMap<>();
                                for (Object result : results) {
                                    DocumentSnapshot newsDoc = (DocumentSnapshot) result;
                                    if (newsDoc.exists()) {
                                        SavedNews news = newsDoc.toObject(SavedNews.class);
                                        if (news != null) {
                                            news.setNewsId(newsDoc.getId());
                                            uniqueNewsMap.put(news.getNewsId(), news);
                                            Log.d(TAG, "Loaded: " + news.getTitle() + " (ID: " + news.getNewsId() + ")");
                                        }
                                    } else {
                                        Log.w(TAG, "News document not found for ID: " + newsDoc.getId());
                                    }
                                }

                                savedNewsList.clear();
                                savedNewsList.addAll(uniqueNewsMap.values());
                                progressBar.setVisibility(View.GONE);
                                noSavedNewsMessage.setVisibility(savedNewsList.isEmpty() ? View.VISIBLE : View.GONE);
                                savedNewsAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                showError("Error fetching news details: " + e.getMessage(), e);
                            });
                })
                .addOnFailureListener(e -> {
                    showError("Error fetching saved news list: " + e.getMessage(), e);
                });
    }

    private void showNoSavedMessage(String message) {
        savedNewsList.clear();
        savedNewsAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        noSavedNewsMessage.setText(message);
        noSavedNewsMessage.setVisibility(View.VISIBLE);
        Log.d(TAG, message);
    }

    private void showError(String message, Exception e) {
        progressBar.setVisibility(View.GONE);
        noSavedNewsMessage.setText(message);
        noSavedNewsMessage.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message, e);
    }

    @Override
    public void onNewsClick(Serializable newsItem) {
        if (hostActivity != null) {
            NewsArticleFragment newsArticleFragment = NewsArticleFragment.newInstance(newsItem);
            hostActivity.replaceFragment(newsArticleFragment);
        } else {
            Toast.makeText(getContext(), "Error: Host activity not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            fetchSavedNews();
        }
    }
}
