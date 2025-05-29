package com.example.workout;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import Model.Repository.CourseRepository;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView activityTextView;
    private CourseRepository courseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_business); // ודא שזה שם הקובץ XML

        // מציאת רכיבי תצוגה
        calendarView = findViewById(R.id.calendarView);
        activityTextView = findViewById(R.id.activityExampleTextView);

        // יצירת רפוזיטורי עם נתונים מדומים
        courseRepository = new CourseRepository();

        // מאזין לבחירת תאריך בלוח השנה
        /* calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // יצירת מפתח תאריך בפורמט YYYY-MM-DD
            String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            // שליפת השיעורים לפי תאריך
            String lesson = courseRepository.getAllBusinessesCourses(dateKey);

            // הצגת השיעורים
            activityTextView.setText(lesson);
        });  */
    }
}
