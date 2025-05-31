package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        TextView businessName = findViewById(R.id.businessNameHomePageTextView);
        businessName.setText(business.getBusinessName());

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
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
                return true;
            }
            return false;
        });


    }
}
