package com.cycotechnologies.justinnews;

import android.content.Context;

import android.content.Intent;
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
    private OnNewsClickListener listener;

    public TrendingNewsAdapter(List<TrendNews> newsList, Context context, OnNewsClickListener listener) {
        this.TrendingList = newsList;
        this.context = context;
        this.listener = listener;
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
        Picasso.get().load(news.getImageUrl()).into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsClick(news);
            }
        });
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
