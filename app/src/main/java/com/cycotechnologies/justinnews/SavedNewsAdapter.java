package com.cycotechnologies.justinnews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SavedNewsAdapter extends RecyclerView.Adapter<SavedNewsAdapter.NewsViewHolder> {

    private List<SavedNews> savedNewsList;
    private Context context;
    private OnNewsClickListener listener;

    // Constructor now takes OnNewsItemClickListener
    public SavedNewsAdapter(List<SavedNews> savedNewsList, Context context, OnNewsClickListener listener) {
        this.savedNewsList = savedNewsList;
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

        SavedNews news = savedNewsList.get(position);
        holder.title.setText(news.getTitle());
        holder.subTitle.setText(news.getSubTitle());
        holder.dateCreated.setText(news.getDateCreated());

        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Picasso.get().load(news.getImageUrl())
                    .placeholder(R.drawable.ic_profile_background)
                    .error(R.drawable.ic_profile_background)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_profile_background);
        }

        // Set the click listener using the global interface
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsClick(news);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedNewsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, dateCreated, subTitle;
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