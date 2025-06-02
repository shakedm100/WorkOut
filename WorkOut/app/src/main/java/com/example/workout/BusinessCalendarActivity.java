package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Adapters.CourseAdapter;
import Model.Business;
import Model.Course;
import Model.Day;
import Model.Repository.CourseRepository;

public class BusinessCalendarActivity extends AppCompatActivity
{
    private CalendarView calendarView;
    private RecyclerView courseRecyclerView;
    private CourseAdapter adapter;
    Business business;
    private List<Course> courseList = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_business);

        calendarView = findViewById(R.id.calendarView);
        courseRecyclerView = findViewById(R.id.courseRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        business = getIntent().getParcelableExtra("business");

        try
        {
            adapter = new CourseAdapter(courseList);
            courseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            courseRecyclerView.setAdapter(adapter);
        }
        catch (Exception e)
        {
            //TODO: No courses message
        }

        calendarView.setOnDateChangeListener((calendarView, year, month, dayOfMonth) ->
        {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(year, month, dayOfMonth);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            Day selectedDay = mapCalendarDowToDayEnum(dayOfWeek);
            loadCoursesByDay(selectedDay);
        });

        setUpBottomNavigationView();
    }

    // Convert Calendar.SUNDAY=1,… SATURDAY=7 into your Day enum
    private Day mapCalendarDowToDayEnum(int calendarDayOfWeek)
    {
        switch (calendarDayOfWeek)
        {
            case Calendar.SUNDAY:
                return Day.Sunday;
            case Calendar.MONDAY:
                return Day.Monday;
            case Calendar.TUESDAY:
                return Day.Tuesday;
            case Calendar.WEDNESDAY:
                return Day.Wednesday;
            case Calendar.THURSDAY:
                return Day.Thursday;
            case Calendar.FRIDAY:
                return Day.Friday;
            case Calendar.SATURDAY:
                return Day.Saturday;
            default:
                return Day.Sunday; // fallback
        }
    }

    private void loadCoursesByDay(Day day)
    {
        CourseRepository courseRepository = new CourseRepository();
        courseList.clear();
        adapter.notifyDataSetChanged();
        courseRepository.getCoursesByDayOfWeek(business, day).addOnSuccessListener(task ->
        {
            courseList.addAll(task);
           adapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
        {
            // TODO: Couldn't load courses for this business, show error
        });
    }

    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);

        // Setup event listener
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, BusinessHomeActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                Intent intent = new Intent(this, BusinessProfileActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_courses)
            {
                Intent intent = new Intent(this, CoursesActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_status)
            {
                Intent intent = new Intent(this, StatusActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
            }
            else if (id == R.id.nav_calendar)
            {
                // you’re already here
                return true;
            }
            return false;
        });
    }
}
