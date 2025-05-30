package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import Model.Business;

public class CoursesActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classes_business);

        Business business = getIntent().getParcelableExtra("business");

        //TODO: Need to show all the business's courses

        FloatingActionButton addUpdateCourse = findViewById(R.id.fabAddClass);
        addUpdateCourse.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, AddOrEditClassActivity.class);
            intent.putExtra("business", business);
            intent.putExtra("course", "null"); // No course to update
            startActivity(intent);
        });
    }
}
