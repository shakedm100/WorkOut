package com.example.workout;
import static androidx.core.content.ContextCompat.startActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import android.view.MenuItem;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// ViewModel imports
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ViewModel.MyViewModel;
//

public class MainActivity extends AppCompatActivity {

    private MyViewModel viewModel;
    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page); // Choose the correct XML

        // Find the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Setup event listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            }
            else if (id == R.id.nav_ratings) {
                startActivity(new Intent(this, RatingsActivity.class));
                return true;
            }
            else if (id == R.id.nav_messages) {
                startActivity(new Intent(this, MessagesActivity.class));
                return true;
            }
            return false;
        });

        // ViewModel changes

        // get the instance as the MyViewModel class with the functions
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // listen to new data
        viewModel.getTextData().observe(this, text -> textView.setText(text));

        // Update ViewModel data on button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.updateText("Hello from ViewModel!");
            }
        });
    }
}
