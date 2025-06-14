package com.cycotechnologies.justinnews;

import android.content.Context; // Needed for the adapter constructor
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import java.io.Serializable;
import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List

 import com.google.firebase.firestore.FirebaseFirestore;
 import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * A simple {@link Fragment} subclass for displaying news based on a specific category.
 * This fragment receives the category as an argument and displays relevant news content
 * using a RecyclerView, leveraging the SavedNews data model and SavedNewsAdapter.
 * It also implements the OnNewsClickListener interface to handle news item clicks.
 */
public class CategoryNewsFragment extends Fragment implements OnNewsClickListener {

    private static final String ARG_CATEGORY = "category";

    private TextView textViewCategoryTitle;
    private RecyclerView recyclerViewNews;
    private SavedNewsAdapter savedNewsAdapter;
    private List<SavedNews> newsList;
    private String newsCategory;
    private  MainActivity hostActivity;

    public CategoryNewsFragment() {
        // Required empty public constructor
    }

    /**
     *
     * @param category The news category to display.
     * @return A new instance of fragment CategoryNewsFragment.
     */
    public static CategoryNewsFragment newInstance(String category) {
        CategoryNewsFragment fragment = new CategoryNewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newsCategory = getArguments().getString(ARG_CATEGORY);
        }
        newsList = new ArrayList<>();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewCategoryTitle = view.findViewById(R.id.textViewCategoryTitle);
        recyclerViewNews = view.findViewById(R.id.recyclerViewNews);

        savedNewsAdapter = new SavedNewsAdapter(newsList, getContext(), this);

        recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNews.setAdapter(savedNewsAdapter);

        if (newsCategory != null) {
            textViewCategoryTitle.setText(newsCategory + " News");
            fetchNewsData(newsCategory); // Call the method to fetch news
        } else {
            textViewCategoryTitle.setText("News");
            // Optionally, display a message if no category is selected
            newsList.add(new SavedNews("No Category", "Please select a category.", "", "", "", false, ""));
            savedNewsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @param category The category for which to fetch news.
     */
    private void fetchNewsData(String category) {
        // Clear existing news before fetching new ones
        newsList.clear();
        savedNewsAdapter.notifyDataSetChanged();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Public_News")
          .whereEqualTo("Catagory", category)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      String title = document.getString("Title");
                      String subTitle = document.getString("Sub_Title");
                      String summary = document.getString("News_summery");
                      String dateCreated = document.getString("Date");
                      String imageUrl = document.getString("imageUrl");
                      boolean trending = document.getBoolean("trending") != null ? document.getBoolean("trending") : false;
                      String Catagory = document.getString("Catagory");

                      SavedNews news = new SavedNews(title, subTitle, summary, dateCreated, imageUrl, trending, Catagory);
                      news.setNewsId(document.getId());
                      newsList.add(news);
                  }
                  if (newsList.isEmpty()) {
                      newsList.add(new SavedNews("No News Found", "There are no articles available for this category yet.", "", "", "", false, category));
                  }
                  savedNewsAdapter.notifyDataSetChanged();
              } else {
                  Log.e("CategoryNewsFragment", "Error fetching news: ", task.getException());
                  newsList.add(new SavedNews("Error", "Failed to load news: " + task.getException().getMessage(), "", "", "", false, category));
                  savedNewsAdapter.notifyDataSetChanged();
              }
          });


        // --- Mock Data for demonstration without Firebase ---
        // This simulates fetching data. Remove this section when Firebase is implemented.
        Log.d("CategoryNewsFragment", "Simulating fetching news for category: " + category);

        savedNewsAdapter.notifyDataSetChanged();
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
}
