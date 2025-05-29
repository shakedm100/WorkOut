package com.example.workout;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import Adapters.ReviewAdapter;
import com.example.workout.models.Review;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_business);

        recyclerView = findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // הדמיה של נתונים (בשלב זה - סטטיים)
        reviews = new ArrayList<>();
        reviews.add(new Review("Noa123", "Great classes, highly recommend!", 5));
        reviews.add(new Review("AviM", "Instructor was friendly.", 4));
        reviews.add(new Review("Dana89", "Good but room was a bit crowded.", 3));

        adapter = new ReviewAdapter(reviews);
        recyclerView.setAdapter(adapter);
    }
}
