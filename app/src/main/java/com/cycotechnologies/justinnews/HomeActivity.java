package com.cycotechnologies.justinnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "shared_prefs";
    public static final String EMAIL_KEY = "email_key";
    public static final String PASSWORD_KEY = "password_key";

    private ViewPager2 viewPager;
    private DotsIndicator dotIndicator;
    private NewsAdapter adapter;
    private List<NewsItem> newsList;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(EMAIL_KEY, null);
        String password = sharedPreferences.getString(PASSWORD_KEY, null);

        if(email == null || password == null){
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_home);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = findViewById(R.id.newsViewPager);
        dotIndicator = findViewById(R.id.dotsIndicator);

        List<NewsItem> newsList = new ArrayList<>();

        newsList.add(new NewsItem("AI Breakthrough", "2025-06-01",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGN-CgxyttelaLsxvCXjE5LW-DvMV48EQ04Q&s"
        ));

        newsList.add(new NewsItem("AI Breakthrough", "2025-06-01",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGN-CgxyttelaLsxvCXjE5LW-DvMV48EQ04Q&s"
        ));

        newsList.add(new NewsItem("AI Breakthrough", "2025-06-01",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGN-CgxyttelaLsxvCXjE5LW-DvMV48EQ04Q&s"
        ));

        adapter =  new NewsAdapter(newsList, this);
        viewPager.setAdapter(adapter);

        viewPager.setPageTransformer((page, position) -> {
            float scale = 1 - Math.abs(position) * 0.15f;
            page.setScaleY(scale);
        });

        dotIndicator.setViewPager2(viewPager);

    }
}