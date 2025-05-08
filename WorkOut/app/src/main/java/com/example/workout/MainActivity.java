package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page); // בחר את ה־XML המתאים

        // מציאת ה־BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // הגדרת מאזין ללחיצות על הכפתורים
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        // טיפול בלחיצה על בית
                        return true;
                    case R.id.nav_profile:
                        // מעבר לפרופיל
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        return true;
                    case R.id.nav_search:
                        // חיפוש
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        return true;
                    case R.id.nav_ratings:
                        // דירוגים
                        startActivity(new Intent(MainActivity.this, RatingsActivity.class));
                        return true;
                    case R.id.nav_messages:
                        // הודעות
                        startActivity(new Intent(MainActivity.this, MessagesActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
}
