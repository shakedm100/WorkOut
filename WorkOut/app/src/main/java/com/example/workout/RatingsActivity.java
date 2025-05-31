package com.example.workout;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import Model.Business;
import Model.Client;
import Model.Rating;
import Model.Repository.BusinessRepository;

public class RatingsActivity extends AppCompatActivity
{

    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitRatingButton;
    private TextView businessNameTextView;
    BusinessRepository businessRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.ratingsActivityToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        businessRepository = new BusinessRepository();
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitRatingButton = findViewById(R.id.submitRatingButton);
        businessNameTextView = findViewById(R.id.rateBusinessText);
        Business business = getIntent().getParcelableExtra("business");
        Client client = getIntent().getParcelableExtra("client");

        if (business == null)
            finish();

        businessNameTextView.setText("Rate " + business.getBusinessName());

        submitRatingButton.setOnClickListener(view ->
        {
            float stars = ratingBar.getRating();
            String comment = commentEditText.getText().toString().trim();
            Rating rating = new Rating(stars, comment, client);
            businessRepository.addRatingToBusiness(business, rating).addOnSuccessListener(task ->
            {
                finish();
            }).addOnFailureListener(e ->
            {
                //TODO: Show error
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            // This is called when the up arrow (←) is tapped in the Toolbar
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
