package com.example.fitlab;

public class Tutorial {
    private String videoId;
    private String title;

    public Tutorial(String videoId, String title) {
        this.videoId = videoId;
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }
}
