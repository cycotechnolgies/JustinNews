package com.cycotechnologies.justinnews;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class NewsArticle {
    private String title;
    private String imageUrl;
    private String content; // Full article content
    private String source;
    private Date publishDate; // Use Date for Firebase Timestamp
    private boolean isTrending; // For filtering trending news

    // No-argument constructor required for Firebase
    public NewsArticle() {
    }

    public NewsArticle(String title, String imageUrl, String content, String source, Date publishDate, boolean isTrending) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.content = content;
        this.source = source;
        this.publishDate = publishDate;
        this.isTrending = isTrending;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @ServerTimestamp // This annotation helps Firebase automatically convert Server Timestamps
    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public boolean isTrending() {
        return isTrending;
    }

    public void setTrending(boolean trending) {
        isTrending = trending;
    }
}
