package com.cycotechnologies.justinnews;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment implements OnNewsClickListener {

    private static final String ARG_SEARCH_QUERY = "search_query";
    private String searchQueryText;

    private RecyclerView searchResultRecyclerView;
    private NewsForYouAdaptor searchResultAdapter;
    private List<NewsForYouItem> searchResultList;
    private TextView searchResultTitle;
    private TextView nothingFoundText;
    private MainActivity hostActivity;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SearchResultFragment() {
        // Required empty public constructor
    }

    public static SearchResultFragment newInstance(String query) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchQueryText = getArguments().getString(ARG_SEARCH_QUERY);
        }
        searchResultList = new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            hostActivity = (MainActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must be hosted by MainActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hostActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        searchResultRecyclerView = view.findViewById(R.id.searchResultRecyclerView);
        searchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchResultAdapter = new NewsForYouAdaptor(searchResultList, getContext(), this);
        searchResultRecyclerView.setAdapter(searchResultAdapter);

        searchResultTitle = view.findViewById(R.id.searchResultTitle);
        nothingFoundText = view.findViewById(R.id.nothingFoundText);

        if (searchResultTitle != null) {
            if (searchQueryText != null && !searchQueryText.isEmpty()) {
                searchResultTitle.setText("Results for: \"" + searchQueryText + "\"");
            } else {
                searchResultTitle.setText("All News"); // Or hide it
            }
        }

        // Trigger the search query when the fragment's view is created
        if (searchQueryText != null && !searchQueryText.isEmpty()) {
            performClientSideSearch(searchQueryText);
        } else {
            // Handle empty query: show all non-trending news or a message
            fetchAllNews(); // Or you could show a message that no query was entered
        }

        return view;
    }

    private void performClientSideSearch(String query) {
        if (!isAdded()) return;

        nothingFoundText.setVisibility(View.GONE);
        searchResultRecyclerView.setVisibility(View.VISIBLE);

        db.collection("Public_News")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isAdded()) {
                        searchResultList.clear();
                        String lowerCaseQuery = query.toLowerCase();
                        boolean foundResults = false;

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            NewsForYouItem newsItem = doc.toObject(NewsForYouItem.class);
                            newsItem.setNewsId(doc.getId());
                            // Perform client-side 'contains' check
                            if (newsItem.getTitle() != null && newsItem.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                                searchResultList.add(newsItem);
                                foundResults = true;
                            }
                        }

                        if (!foundResults && !query.isEmpty()) {
                            Toast.makeText(getContext(), "No results found for \"" + query + "\"", Toast.LENGTH_LONG).show();
                        }
                        searchResultAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to fetch news for client-side search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        nothingFoundText.setVisibility(View.VISIBLE);
                        searchResultRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    // This method is called if the initial query is empty (e.g., user just navigates to search results)
    private void fetchAllNews() {
        if (!isAdded()) return;

        db.collection("Public_News")
                // Optionally filter here, e.g., to only show non-trending by default
                // .whereEqualTo("trending", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isAdded()) {
                        searchResultList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            NewsForYouItem newsItem = doc.toObject(NewsForYouItem.class);
                            newsItem.setNewsId(doc.getId());
                            searchResultList.add(newsItem);
                        }
                        searchResultAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to fetch all news: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onNewsClick(Serializable newsItem) {
        if (hostActivity != null && isAdded()) {
            NewsArticleFragment newsArticleFragment = NewsArticleFragment.newInstance(newsItem);
            hostActivity.replaceFragment(newsArticleFragment);
        } else if (isAdded()) {
            Toast.makeText(getContext(), "Error: Host activity not found or fragment detached.", Toast.LENGTH_SHORT).show();
        }
    }
}