package com.example.fitlab;

import com.google.gson.annotations.SerializedName;

public class Exercise {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    private String imageUrl;

    private String id;

    // Getters and setters
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}