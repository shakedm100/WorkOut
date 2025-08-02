package com.example.workout;

import com.example.workout.Client.CalendarActivity;
import com.example.workout.Client.MapActivity;
import com.example.workout.Client.ProfileActivity;
import com.example.workout.Client.SearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// ViewModel imports
import android.widget.Button;
import android.widget.TextView;
//

import Model.Client;

public class MainActivity extends AppCompatActivity
{

    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Choose the correct XML

        Client client = getIntent().getParcelableExtra("client");

        // Find the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

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
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_search)
            {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_map)
            {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_Calendar)
            {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("client", client);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // ViewModel changes

        // get the instance as the MyViewModel class with the functions

        // listen to new data

        // Update ViewModel data on button click
        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //viewModel.updateText("Hello from ViewModel!");
            }
        });*/
    }
}
