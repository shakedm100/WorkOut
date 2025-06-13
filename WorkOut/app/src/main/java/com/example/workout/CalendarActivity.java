package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

import Model.Client;
import Model.Repository.CourseRepository;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView activityTextView;
    private CourseRepository courseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);

        Client client = getIntent().getParcelableExtra("client");

        // Find the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);

        // Setup event listener
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_search)
            {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_map)
            {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_Calendar)
            {
                // you’re already here
                return true;
            }
            return false;
        });
    }
}
