package com.example.workout;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Model.Business;
import Model.Client;

public class BusinessHomeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.business_home);

        Business business = getIntent().getParcelableExtra("business");

        // Find the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Setup event listener
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            /*else if (id == R.id.nav_search) {
                startActivity(new Intent(this, ReviewsActivity.class));
                return true;
            }*/
            else if (id == R.id.nav_ratings)
            {
                Intent intent = new Intent(this, CoursesActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_messages)
            {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            }
            return false;
        });


    }
}
