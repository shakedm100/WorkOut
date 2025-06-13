package com.example.workout.Business;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import Adapters.ReviewAdapter;
import Model.Business;

public class StatusActivity extends AppCompatActivity
{

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    Business current;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_business);

        recyclerView = findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        current = getIntent().getParcelableExtra("business");

        if (current.getRatings() == null)
        {
            //TODO: No ratings yet
        }
        else
        {
            adapter = new ReviewAdapter(current.getRatings());
            recyclerView.setAdapter(adapter);
        }

        setUpBottomNavigationView();
    }

    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setSelectedItemId(R.id.nav_status);

        // Setup event listener
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, BusinessHomeActivity.class);
                intent.putExtra("business", current);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                Intent intent = new Intent(this, BusinessProfileActivity.class);
                intent.putExtra("business", current);
                startActivity(intent);
            }
            else if (id == R.id.nav_courses)
            {
                Intent intent = new Intent(this, CoursesActivity.class);
                intent.putExtra("business", current);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_status)
            {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_calendar)
            {
                Intent intent = new Intent(this, BusinessCalendarActivity.class);
                intent.putExtra("business", current);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
