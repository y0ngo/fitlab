package com.example.fitlab;

import com.google.gson.annotations.SerializedName;

public class ExerciseImage {
    @SerializedName("image")
    private String imageUrl;

    @SerializedName("exercise")
    private int exerciseId;

    public String getImageUrl() {
        return imageUrl;
    }

    public int getExerciseId() {
        return exerciseId;
    }
}