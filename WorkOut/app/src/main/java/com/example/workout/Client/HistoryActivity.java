package com.example.workout.Client;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.workout.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import Model.Client;
import Model.Enrollment;
import Model.Repository.CourseRepository;

public class HistoryActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    CourseRepository courseRepository;
    ArrayList<Enrollment> history;
    Client current;
    LinearLayout historyContainer;
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
        historyContainer = findViewById(R.id.historyContainer);
        courseRepository.getClientHistory(current).addOnSuccessListener(task ->
        {
            history = (ArrayList<Enrollment>) task;
            showHistory();

        }).addOnFailureListener(error ->
        {
            //TODO: Show error message
        });
    }

    private void showHistory()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());

        for (Enrollment enrollment : history)
        {
            String formattedDate = simpleDateFormat.format(enrollment.getTime().toDate());

            String builder = "Name: " + enrollment.getCourse().getName() + "\n" +
                    "At: " + formattedDate + "\n";

            // Create the text view
            TextView courseTextView = new TextView(HistoryActivity.this);
            courseTextView.setText(builder);
            courseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            courseTextView.setLineSpacing(0f, 1.2f); // slight line spacing
            courseTextView.setPadding(5, 1, 5, 1);
            courseTextView.setBackgroundResource(R.drawable.rectangle_background_selector);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            params.setMargins(0, 0, 0, 6); // 24px bottom margin between course items
            courseTextView.setLayoutParams(params);

            // Add to the container
            historyContainer.addView(courseTextView);
        }
    }
}
