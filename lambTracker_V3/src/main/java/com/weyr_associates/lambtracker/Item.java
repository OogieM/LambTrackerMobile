package com.weyr_associates.lambtracker;

import android.graphics.drawable.Drawable;
import android.widget.RatingBar;

public class Item
{
    String title;
    Drawable image;
    RatingBar ratingBar;

    // Empty Constructor
    public Item()
    {

    }

    // Constructor
    public Item(String title, RatingBar ratingBar)
    {
        super();
        this.title = title;
        this.ratingBar = ratingBar;
    }

    // Getter and Setter Method
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Drawable getImage()
    {
        return image;
    }

    public void setImage(Drawable image)
    {
        this.image = image;
    }

    public RatingBar getBar()
    {
        return ratingBar;
    }

    public void setBar()
    {
        this.ratingBar = ratingBar;
    }
}

