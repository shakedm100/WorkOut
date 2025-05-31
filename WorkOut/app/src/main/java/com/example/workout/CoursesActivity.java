package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Model.Business;
import Model.Course;
import Model.Repository.CourseRepository;
import Model.Schedule;

public class CoursesActivity extends AppCompatActivity
{
    LinearLayout coursesContainer;
    Course chosenCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classes_business);

        Business business = getIntent().getParcelableExtra("business");

        //TODO: Need to show all the business's courses

        FloatingActionButton addUpdateCourse = findViewById(R.id.fabAddClass);
        coursesContainer = findViewById(R.id.coursesContainer);
        chosenCourse = null;

        addUpdateCourse.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, AddOrEditClassActivity.class);
            intent.putExtra("business", business);
            intent.putExtra("course", chosenCourse); // No course to update
            startActivity(intent);
        });

        showCourses(business);
    }

    private void showCourses(Business business)
    {
        CourseRepository repository = new CourseRepository();

        repository.getAllBusinessesCourses(business).addOnSuccessListener(courses ->
        {
            // Just in case delete all views first
            coursesContainer.removeAllViews();

            if (courses == null || courses.isEmpty())
            {
                TextView noCoursesTv = new TextView(CoursesActivity.this);
                noCoursesTv.setText("No courses available for this business.");
                noCoursesTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                noCoursesTv.setPadding(0, 8, 0, 8);
                coursesContainer.addView(noCoursesTv);
                return;
            }

            for (Course course : courses)
            {
                StringBuilder builder = new StringBuilder();
                builder.append("Name: ").append(course.getName()).append("\n");
                builder.append("Type: ").append(course.getType().name()).append("\n");
                builder.append("Capacity: ").append(course.getCapacity()).append("\n");
                builder.append("Age Range: ").append(course.getAgeRange().getMinAge()).append(" - ")
                        .append(course.getAgeRange().getMaxAge()).append("\n");
                Schedule schedule = course.getSchedule();
                String dayString = schedule.getDay().toString();
                // Show only time
                Date occurrenceDate = schedule.getOccurrence().toDate();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeOnly = timeFormat.format(occurrenceDate);
                builder.append("Schedule: ").append(dayString).append(" at ")
                        .append(timeOnly).append("\n");
                builder.append("Category: ").append(course.getCategory().name()).append("\n");
                builder.append("Description: ").append(course.getDescription()).append("\n");

                // Create the text view
                TextView courseTextView = new TextView(CoursesActivity.this);
                courseTextView.setText(builder.toString());
                courseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                courseTextView.setLineSpacing(0f, 1.2f); // slight line spacing
                courseTextView.setPadding(5, 1, 5, 1);
                courseTextView.setBackgroundResource(R.drawable.rectangle_background_selector);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                params.setMargins(0, 0, 0, 6); // 24px bottom margin between course items
                courseTextView.setLayoutParams(params);

                courseTextView.setOnClickListener(v ->
                {
                    chosenCourse = course;
                });

                // Add to the container
                coursesContainer.addView(courseTextView);
            }
        }).addOnFailureListener(e ->
        {
            //TODO: Add error message, failed to load courses
        });
    }
}
