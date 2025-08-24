package com.example.workout.Business;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.workout.Client.UpdateClientActivity;
import com.example.workout.LoginActivity;
import com.example.workout.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import Model.Business;

public class BusinessProfileActivity extends AppCompatActivity
{
    private BottomNavigationView bottomNavigationView;
    private Business business;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_business);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button logout = findViewById(R.id.businessLogoutButton);
        logout.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        TextView username = findViewById(R.id.userName);

        business = getIntent().getParcelableExtra("business");
        if (business != null)
            username.setText(business.getBusinessName());
        else
            username.setText("Business");

        Button accountButton = findViewById(R.id.accountButton);
        accountButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, BusinessUpdateActivity.class);
            intent.putExtra("business", business);
            startActivity(intent);
        });

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
