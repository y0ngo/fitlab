package com.example.fitlab;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseImageResponse {
    @SerializedName("results")
    private List<ExerciseImage> exerciseImages;

    public List<ExerciseImage> getExerciseImages() {
        return exerciseImages;
    }
}