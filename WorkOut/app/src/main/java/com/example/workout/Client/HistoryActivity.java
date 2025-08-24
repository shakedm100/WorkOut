package com.example.workout.Client;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
