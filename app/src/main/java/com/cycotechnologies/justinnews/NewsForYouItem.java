package com.cycotechnologies.justinnews;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class NewsForYouItem implements Serializable {

    private String newsId;
    @PropertyName("Title")
    private String title;

    @PropertyName("Sub_Title")
    private String subTitle;

    @PropertyName("News_summery")
    private String summary;

    @PropertyName("Date")
    private String dateCreated;

    @PropertyName("imageUrl")
    private String imageUrl;

    @PropertyName("trending")
    private boolean trending;

    @PropertyName("Catagory")
    private String Catagory;

    public NewsForYouItem() {}

    public NewsForYouItem(String title, String subTitle, String summary,
                          String dateCreated, String imageUrl, boolean trending, String Catagory) {
        this.title = title;
        this.subTitle = subTitle;
        this.summary = summary;
        this.dateCreated = dateCreated;
        this.imageUrl = imageUrl;
        this.trending = trending;
        this.Catagory = Catagory;
    }

    @PropertyName("Title")
    public String getTitle() { return title; }

    @PropertyName("Sub_Title")
    public String getSubTitle() { return subTitle; }

    @PropertyName("News_summery")
    public String getSummary() { return summary; }

    @PropertyName("Date")
    public String getDateCreated() { return dateCreated; }

    @PropertyName("imageUrl")
    public String getImageUrl() { return imageUrl; }

    @PropertyName("trending")
    public boolean isTrending() { return trending; }

    @PropertyName("Catagory")
    public String getCatagory() { return Catagory; }

    public String getNewsId() { return newsId; }
    public void setNewsId(String newsId) { this.newsId = newsId; }

}

