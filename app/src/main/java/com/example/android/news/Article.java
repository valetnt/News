package com.example.android.news;


import android.graphics.Bitmap;

public class Article {

    private String mTitle;
    private String mAuthor;
    private String mDate;
    private String mSection;
    private String mLink;
    private Bitmap mThumbnail;
    private boolean mHasThumbnail = false;

    /*
     * Constructor
     */
    public Article(String title, String author, String date, String section, String link) {
        mTitle = title;
        mAuthor = author;
        mDate = date;
        mSection = section;
        mLink = link;
    }

    /*
     * Getters
     */
    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDate() {
        return mDate;
    }

    public String getSection() {
        return mSection;
    }

    public String getLink() {
        return mLink;
    }

    // Call getThumbnail() ONLY IF hasThumbnail() has returned TRUE
    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    // Set thumbnail
    public void setThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
        mHasThumbnail = true;
    }

    public boolean hasThumbnail() {
        return mHasThumbnail;
    }

}
