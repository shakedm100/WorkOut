package com.example.workout.Client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.MainActivity;
import com.example.workout.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import Adapters.CourseAdapter;
import Model.Client;
import Model.Course;
import Model.Enrollment;
import Model.Repository.CourseRepository;

public class CalendarActivity extends AppCompatActivity
{

    private CalendarView calendarView;
    private CourseRepository courseRepository;
    private RecyclerView clientCourseRecyclerView;
    private BottomNavigationView bottomNavigationView;
    private Client client;
    private ArrayList<Course> courses;
    private ArrayList<Enrollment> allEnrollments;
    private CourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        client = getIntent().getParcelableExtra("client");

        calendarView = findViewById(R.id.clientCalendarView);
        clientCourseRecyclerView = findViewById(R.id.clientCourseRecyclerView);

        courseRepository = new CourseRepository();

        // Find the BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_Calendar);
        setupBottomNavigationView();

        courses = new ArrayList<>();
        allEnrollments = new ArrayList<>();
        courseRepository.getAllEnrollmentsByClient(client)
                .addOnSuccessListener(enrollments ->
                {
                    allEnrollments = (ArrayList<Enrollment>) enrollments;
                });

        try
        {
            courseAdapter = new CourseAdapter(courses);
            clientCourseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            clientCourseRecyclerView.setAdapter(courseAdapter);
        }
        catch (Exception e)
        {
            //TODO: No courses message
        }

        calendarView.setOnDateChangeListener((calendarView, year, month, dayOfMonth) ->
        {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(year, month, dayOfMonth);
            loadCoursesByDate(dayOfMonth, month, year);
        });
    }

    private void loadCoursesByDate(int day, int month, int year)
    {
        month++;
        // Clear out any existing list
        courses.clear();
        courseAdapter.notifyDataSetChanged();

        // Construct the target LocalDate
        LocalDate targetDate = LocalDate.of(year, month, day);
        ZoneId zone = ZoneId.systemDefault();

        // Filter in-memory enrollments
        for (Enrollment enrollment : allEnrollments)
        {
            LocalDate signupDate = enrollment.getTime()
                    .toInstant()
                    .atZone(zone)
                    .toLocalDate();

            if (signupDate.equals(targetDate))
            {
                courses.add(enrollment.getCourse());
            }
        }

        Collections.sort(allEnrollments, Enrollment.BY_TIME);

        // Refresh UI
        courseAdapter.notifyDataSetChanged();
    }

    private void setupBottomNavigationView()
    {
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, MainActivity.class);
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
