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

public class NewsForYouAdaptor extends RecyclerView.Adapter<NewsForYouAdaptor.NewsViewHolder> {
    private List<NewsForYouItem> newsForYouList;
    private Context context;
    private OnNewsClickListener listener;

    public NewsForYouAdaptor(List<NewsForYouItem> newsList, Context context, OnNewsClickListener listener) {
        this.newsForYouList = newsList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {

        NewsForYouItem news = newsForYouList.get(position);
        holder.title.setText(news.getTitle());
        holder.subTitle.setText(news.getSubTitle());
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
        return newsForYouList.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, subTitle, dateCreated;
        ImageView image;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.newsTitle);
            subTitle = itemView.findViewById(R.id.newsSubTitle);
            dateCreated = itemView.findViewById(R.id.newsDate);
            image = itemView.findViewById(R.id.newsImage);
        }
    }
}
