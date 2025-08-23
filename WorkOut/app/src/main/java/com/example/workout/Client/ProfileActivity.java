package com.example.workout.Client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.workout.LoginActivity;
import com.example.workout.MainActivity;
import com.example.workout.MessagesActivity;
import com.example.workout.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import Model.Client;

public class ProfileActivity extends AppCompatActivity
{
    private BottomNavigationView bottomNavigationView;
    private Client current;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        current = getIntent().getParcelableExtra("client");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button logout = findViewById(R.id.clientLogoutButton);
        logout.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("client", current);
            startActivity(intent);
        });

        Button messagesButton = findViewById(R.id.notificationsButton);
        messagesButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, MessagesActivity.class);
            intent.putExtra("client", current);
            startActivity(intent);
        });

        Button accountButton = findViewById(R.id.accountButton);
        accountButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, UpdateClientActivity.class);
            intent.putExtra("client", current);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        TextView userLabel = findViewById(R.id.userName);
        userLabel.setText(current.getUsername());

        setUpBottomNavigationView();
    }
    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_search)
            {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_map)
            {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_Calendar)
            {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}