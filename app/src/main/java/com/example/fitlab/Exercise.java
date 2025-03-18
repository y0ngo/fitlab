package com.example.fitlab;

import com.google.gson.annotations.SerializedName;

public class Exercise {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    private String imageUrl;

    private Integer id;
    private String muscle;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getId() {
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

    public String getMuscle() {
        return muscle;
    }

    public void setMuscle(String muscle) {
        this.muscle = muscle;
    }

    @Override
    public String toString() {
        return name;
    }
}