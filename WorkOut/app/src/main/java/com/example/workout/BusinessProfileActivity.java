package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Model.Business;

public class BusinessProfileActivity extends AppCompatActivity
{
    BottomNavigationView bottomNavigationView;
    Business business;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile_business);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        TextView username = findViewById(R.id.userName);

        business = getIntent().getParcelableExtra("business");
        username.setText(business.getBusinessName());

        setUpBottomNavigationView();
    }

    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

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
                // you’re already here
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
                Intent intent = new Intent(this, BusinessCalendarActivity.class);
                intent.putExtra("business", business);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
