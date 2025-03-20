package com.example.fitlab;

import java.util.ArrayList;

public class Workout {
    private String date;
    private String timestamp;
    private String userId;
    private ArrayList<String> completedExercises;

    public Workout(String date, String timestamp, String userId, ArrayList<String> completedExercises) {
        this.date = date;
        this.timestamp = timestamp;
        this.userId = userId;
        this.completedExercises = completedExercises;
    }

    public String getDate() {
        return date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public ArrayList<String> getCompletedExercises() {
        return completedExercises;
    }
}