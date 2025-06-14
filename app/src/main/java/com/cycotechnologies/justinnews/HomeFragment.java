package com.cycotechnologies.justinnews;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnNewsClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ViewPager2 viewPager;
    private DotsIndicator dotIndicator;
    private TrendingNewsAdapter trendingAdaptor;
    private List<TrendNews> TrendingList;
    private NewsForYouAdaptor newsForYouAdapter;
    private RecyclerView newsForYouRecyclerView;
    private List<NewsForYouItem> newsForYouList;

    private MainActivity hostActivity;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public HomeFragment() {

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
        // Initialize newsList here, before it's used in the adapter
        TrendingList = new ArrayList<>();
        newsForYouList = new ArrayList<>();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.newsViewPager);
        dotIndicator = view.findViewById(R.id.dotsIndicator);

        trendingAdaptor = new TrendingNewsAdapter(TrendingList, getContext(), this);
        viewPager.setAdapter(trendingAdaptor);

        viewPager.setPageTransformer((page, position) -> {
            float scale = 1 - Math.abs(position) * 0.15f;
            page.setScaleY(scale);
        });

        dotIndicator.setViewPager2(viewPager);

        db.collection("Public_News")
                .whereEqualTo("trending", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    TrendingList.clear();
                    for(QueryDocumentSnapshot doc : querySnapshot){
                        TrendNews news = doc.toObject(TrendNews.class);
                        news.setNewsId(doc.getId());
                        TrendingList.add(news);
                    }
                    trendingAdaptor.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch Trending news", Toast.LENGTH_SHORT).show() // Use getContext() or getActivity()
                );


        //News for you section

        newsForYouRecyclerView = view.findViewById(R.id.newsforyouView);
        newsForYouRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        newsForYouAdapter = new NewsForYouAdaptor(newsForYouList, getContext(), this);
        newsForYouRecyclerView.setAdapter(newsForYouAdapter);

        db.collection("Public_News")
                .whereEqualTo("trending", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    newsForYouList.clear();
                    for(QueryDocumentSnapshot doc : querySnapshot){
                        NewsForYouItem newsItems = doc.toObject(NewsForYouItem.class);
                        newsItems.setNewsId(doc.getId());
                        newsForYouList.add(newsItems);
                    }
                    newsForYouAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch news for you", Toast.LENGTH_SHORT).show() // Use getContext() or getActivity()
                );

        return view;
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