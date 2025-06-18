package com.cycotechnologies.justinnews;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Keep if you use it elsewhere
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager; // Good for hiding keyboard

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnNewsClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ViewPager2 viewPager;
    private DotsIndicator dotIndicator;
    private TrendingNewsAdapter trendingAdaptor;
    private List<TrendNews> TrendingList;
    private NewsForYouAdaptor newsForYouAdapter;
    private RecyclerView newsForYouRecyclerView;
    private List<NewsForYouItem> newsForYouList; // Still used for "News For You" section
    private TextInputEditText searchBar;
    private Button searchButton;
    private TextView GreetingText;

    private MainActivity hostActivity;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        TrendingList = new ArrayList<>();
        newsForYouList = new ArrayList<>(); // Still needed for the "News For You" section
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.newsViewPager);
        dotIndicator = view.findViewById(R.id.dotsIndicator);
        searchBar = view.findViewById(R.id.search_bar);
        searchButton = view.findViewById(R.id.searchBtn);
        newsForYouRecyclerView = view.findViewById(R.id.newsforyouView);
        newsForYouRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        GreetingText = view.findViewById(R.id.greetText);

        trendingAdaptor = new TrendingNewsAdapter(TrendingList, getContext(), this);
        viewPager.setAdapter(trendingAdaptor);

        viewPager.setPageTransformer((page, position) -> {
            float scale = 1 - Math.abs(position) * 0.15f;
            page.setScaleY(scale);
        });

        dotIndicator.setViewPager2(viewPager);

        searchButton.setOnClickListener(v -> {
            String searchText = searchBar.getText().toString().trim();
            // Redirect to the new SearchResultFragment
            if (hostActivity != null && isAdded()) {
                hostActivity.replaceFragment(SearchResultFragment.newInstance(searchText));
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            } else if (isAdded()){
                Toast.makeText(getContext(), "Error: Host activity not found.", Toast.LENGTH_SHORT).show();
            }
        });

        // Original trending news fetch (unchanged)
        db.collection("Public_News")
                .whereEqualTo("trending", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isAdded()) {
                        TrendingList.clear();
                        for(QueryDocumentSnapshot doc : querySnapshot){
                            TrendNews news = doc.toObject(TrendNews.class);
                            news.setNewsId(doc.getId());
                            TrendingList.add(news);
                        }
                        trendingAdaptor.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Failed to fetch Trending news", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        // News for you section (unchanged - it will continue to show non-trending news)
        newsForYouAdapter = new NewsForYouAdaptor(newsForYouList, getContext(), this);
        newsForYouRecyclerView.setAdapter(newsForYouAdapter);
        fetchNewsForYou(); // Call without a search text parameter now
        displayGreeting();

        return view;
    }

    // Simplified fetchNewsForYou as it no longer handles search
    private void fetchNewsForYou() {
        if (!isAdded()) return;

        Query baseQuery = db.collection("Public_News")
                .whereEqualTo("trending", false); // Only non-trending for this section

        baseQuery.get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isAdded()) {
                        newsForYouList.clear();
                        for(QueryDocumentSnapshot doc : querySnapshot){
                            NewsForYouItem newsItems = doc.toObject(NewsForYouItem.class);
                            newsItems.setNewsId(doc.getId());
                            newsForYouList.add(newsItems);
                        }
                        newsForYouAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Failed to fetch news for you: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
    }

    // --- New method to display the greeting ---
    private void displayGreeting() {
        if (getContext() == null || GreetingText == null) {
            return; // Ensure context and TextView are available
        }

        // Use the same SHARED_PREFS name as in LoginActivity
        SharedPreferences sharedPref = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        // Use the same USERNAME_KEY as in LoginActivity
        String username = sharedPref.getString("USERNAME", "Guest"); // "Guest" is the default value if "USERNAME" key not found

        String greetingPrefix;
        greetingPrefix = "Hi";
        GreetingText.setText(String.format(Locale.getDefault(), "%s, %s !", greetingPrefix, username));
    }

    @Override
    public void onNewsClick(Serializable newsItem) {
        if (hostActivity != null && isAdded()) {
            NewsArticleFragment newsArticleFragment = NewsArticleFragment.newInstance(newsItem);
            hostActivity.replaceFragment(newsArticleFragment);
        } else if (isAdded()){
            Toast.makeText(getContext(), "Error: Host activity not found.", Toast.LENGTH_SHORT).show();
        }
    }
}