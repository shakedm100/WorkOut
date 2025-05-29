package com.example.workout.models;

public class Review {
    private String username;
    private String reviewText;
    private float rating;

    public Review(String username, String reviewText, float rating) {
        this.username = username;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public String getReviewText() {
        return reviewText;
    }

    public float getRating() {
        return rating;
    }
}
