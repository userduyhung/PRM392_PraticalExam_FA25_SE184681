package com.example.nguyenduyhung_se184681.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Google Books API Response Model
 * API: https://www.googleapis.com/books/v1/volumes?q=programming
 */
public class GoogleBooksResponse {
    @SerializedName("items")
    private List<BookItem> items;

    @SerializedName("totalItems")
    private int totalItems;

    public List<BookItem> getItems() {
        return items;
    }

    public void setItems(List<BookItem> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public static class BookItem {
        @SerializedName("id")
        private String id;

        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public VolumeInfo getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(VolumeInfo volumeInfo) {
            this.volumeInfo = volumeInfo;
        }
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;

        @SerializedName("authors")
        private List<String> authors;

        @SerializedName("description")
        private String description;

        @SerializedName("categories")
        private List<String> categories;

        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        @SerializedName("publishedDate")
        private String publishedDate;

        @SerializedName("publisher")
        private String publisher;

        @SerializedName("pageCount")
        private Integer pageCount;

        @SerializedName("averageRating")
        private Double averageRating;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public void setImageLinks(ImageLinks imageLinks) {
            this.imageLinks = imageLinks;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public Integer getPageCount() {
            return pageCount;
        }

        public void setPageCount(Integer pageCount) {
            this.pageCount = pageCount;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }
    }

    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;

        @SerializedName("smallThumbnail")
        private String smallThumbnail;

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getSmallThumbnail() {
            return smallThumbnail;
        }

        public void setSmallThumbnail(String smallThumbnail) {
            this.smallThumbnail = smallThumbnail;
        }
    }
}

