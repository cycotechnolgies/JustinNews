package com.cycotechnologies.justinnews;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrendingNewsAdapter extends RecyclerView.Adapter<TrendingNewsAdapter.NewsViewHolder> {
    private List<TrendNews> TrendingList;
    private Context context;

    public TrendingNewsAdapter(List<TrendNews> newsList, Context context) {
        this.TrendingList = newsList;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {

        TrendNews news = TrendingList.get(position);
        holder.title.setText(news.getTitle());
        holder.dateCreated.setText(news.getDateCreated());
        Log.d("ImageDebug", "Loading image from URL: " + news.getImageUrl());
        Picasso.get().load(news.getImageUrl()).into(holder.image);
        Log.d("NewsDebug", "Date: " + news.getDateCreated());
    }

    @Override
    public int getItemCount() {
        return TrendingList.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, dateCreated;
        ImageView image;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.newsTitle);
            dateCreated = itemView.findViewById(R.id.dateCreated);
            image = itemView.findViewById(R.id.newsImage);
        }
    }
}
