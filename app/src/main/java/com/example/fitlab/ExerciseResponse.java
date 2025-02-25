package com.example.fitlab;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseResponse {
    @SerializedName("results")
    private List<Exercise> exercises;

    public List<Exercise> getExercises() {
        return exercises;
    }
}

