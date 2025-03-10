package com.example.fitlab;



public class Workout {
    private String title;
    private String description;

    public Workout(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}