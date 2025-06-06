package com.cycotechnologies.justinnews;

public class NewsItem {
        private String title;
        private String dateCreated;
        private String imageUrl;

        public NewsItem(String title, String description, String imageUrl) {
            this.title = title;
            this.dateCreated = description;
            this.imageUrl = imageUrl;
        }

        public String getTitle() { return title; }
        public String getDateCreated() { return dateCreated; }
        public String getImageUrl() { return imageUrl; }

}
