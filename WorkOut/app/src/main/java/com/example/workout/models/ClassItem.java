package com.example.workout.models;

public class ClassItem {
    private String title;
    private String time;

    public ClassItem(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }
}
