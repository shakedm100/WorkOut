package com.example.workout.Client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workout.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import Model.AgeRange;
import Model.Client;
import Model.Course;
import Model.Enrollment;
import Model.Repository.BusinessRepository;
import Model.Repository.CourseRepository;
import Model.Schedule;

public class SearchResultsActivity extends AppCompatActivity
{

    private LinearLayout coursesContainer;
    private TextView emptyStateText;
    private Client current;

    // Keep track of which “item” is currently expanded, so we can collapse it
    private LinearLayout currentlyExpandedItem = null;
    private CourseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.searchResultsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        current = getIntent().getParcelableExtra("client");

        // Find our container & empty-state TextView
        coursesContainer = findViewById(R.id.coursesSearchResultsContainer);
        emptyStateText = findViewById(R.id.emptyStateText);
        repository = new CourseRepository();
        // Call showBusinesses() to inflate everything
        showCourses();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        finish();
        return true;
    }

    private void showCourses()
    {
        // Grab the List<Course> from the Intent extras
        ArrayList<Course> courses = getIntent().getParcelableArrayListExtra("course_list");

        // Clear any old child views
        coursesContainer.removeAllViews();

        if (courses == null || courses.isEmpty())
        {
            // Show the “no courses” message
            emptyStateText.setText("No courses found.");
            emptyStateText.setVisibility(View.VISIBLE);
            return;
        }

        emptyStateText.setVisibility(View.GONE);

        // For each Course, create:
        // [ TextView (course info) ]
        // [ Button “Sign Up” (initially GONE) ]
        // [ Button “Review”   (initially GONE) ]
        for (Course course : courses)
        {
            // --- Parent “card” layout for this single course ---
            LinearLayout itemLayout = new LinearLayout(SearchResultsActivity.this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            itemLayout.setPadding(
                    dpToPx(8),  // left
                    dpToPx(8),  // top
                    dpToPx(8),  // right
                    dpToPx(8));   // bottom

            itemLayout.setBackgroundResource(R.drawable.rectangle_background_selector);

            // --- TEXTVIEW showing the course details ---
            TextView courseTv = new TextView(SearchResultsActivity.this);
            courseTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            courseTv.setTypeface(Typeface.DEFAULT_BOLD);
            courseTv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Build a multi-line string with: name, type, age range, day & time, category, description
            StringBuilder sb = new StringBuilder();
            sb.append("Name: ").append(course.getName()).append("\n");
            sb.append("Type: ").append(course.getType().name()).append("\n");

            AgeRange ar = course.getAgeRange();
            sb.append("Age Range: ")
                    .append(ar.getMinAge()).append(" - ")
                    .append(ar.getMaxAge()).append("\n");

            Schedule s = course.getSchedule();
            String dayString = s.getDay().toString();
            Date occurrenceDate = s.getOccurrence().toDate();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String startTime = timeFormat.format(occurrenceDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(occurrenceDate);
            calendar.add(Calendar.MINUTE, course.getDuration());
            Date endTime = calendar.getTime();
            String endTimeString = timeFormat.format(endTime);
            sb.append("Schedule: ")
                    .append(dayString).append(" at ")
                    .append(startTime).append(" - ").append(endTimeString).append("\n");

            sb.append("Category: ").append(course.getCategory().name()).append("\n");
            sb.append("Description: ").append(course.getDescription()).append("\n");

            courseTv.setText(sb.toString());

            // --- “Sign Up” button (initially GONE) ---
            Button signUpBtn = new Button(SearchResultsActivity.this);
            signUpBtn.setText("Sign Up");
            signUpBtn.setVisibility(View.GONE);
            signUpBtn.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            signUpBtn.setId(View.generateViewId());
            signUpBtn.setOnClickListener(v ->
            {
                Timestamp nextOccurrence = nextScheduleTimestamp(course.getSchedule());

                checkIfUserAlreadySigned(current, course, nextOccurrence).addOnSuccessListener(check ->
                {
                    if(!check)
                    {
                        repository.signupClientToCourse(current, course,
                                nextOccurrence).addOnSuccessListener(task ->
                        {
                            Toast.makeText(this, "Sign up succeeded", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(task ->
                        {
                            Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                    else
                    {
                        Toast.makeText(this, "Sign up failed - you are signed up to an overlapping course", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(error ->
                {
                    Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show();
                });
            });

            // “Review” button (initially GONE) ---
            Button reviewBtn = new Button(SearchResultsActivity.this);
            reviewBtn.setText("Review");
            reviewBtn.setVisibility(View.GONE);
            reviewBtn.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            reviewBtn.setId(View.generateViewId());
            reviewBtn.setOnClickListener(v ->
            {
                BusinessRepository businessRepository = new BusinessRepository();
                businessRepository.getBusinessesById(course.getBusinessId())
                        .addOnSuccessListener(business ->
                        {
                            Intent intent = new Intent(SearchResultsActivity.this, RatingsActivity.class);
                            intent.putExtra("business", business);
                            intent.putExtra("client", current);
                            startActivity(intent);
                        }).addOnFailureListener(e ->
                        {
                            // TODO: Add failure handling
                        });
            });

            // --- Handle tapping the courseTv itself to toggle these two buttons ---
            courseTv.setOnClickListener(v ->
            {
                // If some other item's buttons are currently visible, hide them first:
                if (currentlyExpandedItem != null && currentlyExpandedItem != itemLayout)
                {
                    Button oldSignUp = currentlyExpandedItem.findViewById(signUpBtn.getId());
                    Button oldReview = currentlyExpandedItem.findViewById(reviewBtn.getId());
                    if (oldSignUp != null) oldSignUp.setVisibility(View.GONE);
                    if (oldReview != null) oldReview.setVisibility(View.GONE);
                }

                // Toggle this item’s own buttons:
                if (signUpBtn.getVisibility() == View.GONE)
                {
                    signUpBtn.setVisibility(View.VISIBLE);
                    reviewBtn.setVisibility(View.VISIBLE);
                    currentlyExpandedItem = itemLayout;
                } else
                {
                    signUpBtn.setVisibility(View.GONE);
                    reviewBtn.setVisibility(View.GONE);
                    currentlyExpandedItem = null;
                }
            });

            // --- Add TextView + Buttons to the parent “card” layout in order ---
            itemLayout.addView(courseTv);
            itemLayout.addView(signUpBtn);
            itemLayout.addView(reviewBtn);

            // Add a bottom‐margin so items aren’t jammed together
            LinearLayout.LayoutParams wrapperParams =
                    (LinearLayout.LayoutParams) itemLayout.getLayoutParams();
            wrapperParams.setMargins(0, 0, 0, dpToPx(12));
            itemLayout.setLayoutParams(wrapperParams);

            // Finally, add this “card” to the container ---
            coursesContainer.addView(itemLayout);
        }
    }

    public static Timestamp nextScheduleTimestamp(Schedule schedule)
    {
        Timestamp timeOfDayOnly = schedule.getOccurrence();
        DayOfWeek targetDay = DayOfWeek.valueOf(schedule.getDay().toString().toUpperCase());
        // Turn original Timestamp into an Instant
        Instant origInstant = Instant.ofEpochSecond(
                timeOfDayOnly.getSeconds(),
                timeOfDayOnly.getNanoseconds());
        // Extract just the time-of-day in system default zone
        LocalTime tod = origInstant.atZone(ZoneId.systemDefault()).toLocalTime();

        // Find today’s date and its DayOfWeek
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        DayOfWeek todayDow = today.getDayOfWeek();

        // Compute days until the next (or same) targetDay
        int daysUntil = (targetDay.getValue() - todayDow.getValue() + 7) % 7;
        // if you want *strictly* next (i.e. skip today when equal), use:
        // if (daysUntil == 0) daysUntil = 7;

        LocalDate nextDate = today.plusDays(daysUntil);

        // Combine date + time back into an Instant
        ZonedDateTime zdt = ZonedDateTime.of(nextDate, tod, ZoneId.systemDefault());
        Instant nextInstant = zdt.toInstant();

        // Build and return the new Firestore Timestamp
        return new Timestamp(nextInstant.getEpochSecond(), nextInstant.getNano());
    }

    /**
     * Utility: convert dp → px so that padding/margin is density‐aware.
     */
    private int dpToPx(int dp)
    {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * This method checks if a user is already signed to another course
     * by checking if is there any overlap between the given enrollment time
     * and the existing enrollments
     * @param client the client that signs up
     * @param course the course to sign up to
     * @param minTime the starting time of the next occurrence of the course
     * @return Task<true> if there is any overlap, false otherwise
     */
    private Task<Boolean> checkIfUserAlreadySigned(Client client, Course course, Timestamp minTime)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(minTime.toDate());
        calendar.add(Calendar.MINUTE, course.getDuration());
        Timestamp maxTime = new Timestamp(calendar.getTime());
        return repository.getAllEnrollmentsByClient(client).continueWith(task ->
        {
            if (!task.isSuccessful() || task.getResult() == null)
                return false;

            for(Enrollment current : task.getResult())
            {
                calendar.setTime(current.getTime().toDate());
                calendar.add(Calendar.MINUTE, current.getCourse().getDuration());

                Timestamp currentMinTime = current.getTime();
                Timestamp currentMaxTime = new Timestamp(calendar.getTime());

                // overlap check
                if(currentMinTime.getSeconds() >= minTime.getSeconds() && currentMinTime.getSeconds() <= maxTime.getSeconds())
                    return true;
                if(currentMaxTime.getSeconds() >= minTime.getSeconds() && currentMaxTime.getSeconds() <= maxTime.getSeconds())
                    return true;
            }

            return false;
        });
    }
}
