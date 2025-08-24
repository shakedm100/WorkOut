package com.example.workout.Client;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;

import java.util.ArrayList;

import Adapters.UpcomingEnrollmentsAdapter;
import Model.Client;
import Model.Enrollment;
import Model.Repository.CourseRepository;

public class HistoryActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    CourseRepository courseRepository;
    ArrayList<Enrollment> history;
    Client current;
    RecyclerView historyRecycler;
    UpcomingEnrollmentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // create back arrow
        toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);

        // Enable back arrow in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Handle arrow click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        current = this.getIntent().getParcelableExtra("client");
        courseRepository = new CourseRepository();
        historyRecycler = findViewById(R.id.historyRecycler);
        adapter = new UpcomingEnrollmentsAdapter(e -> {});
        courseRepository.getClientHistory(current).addOnSuccessListener(task ->
        {
            history = new ArrayList<>(task);
            showHistory();

        }).addOnFailureListener(error ->
        {
            //TODO: Show error message
        });
    }

    private void showHistory()
    {
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(adapter);
        adapter.submit(history);
    }
}
