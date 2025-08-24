package com.example.workout.Client;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.workout.MainActivity;
import com.example.workout.R;

import Model.Business;
import Model.City;
import Model.Client;
import Model.Rating;
import Model.Repository.BusinessRepository;
import ViewModel.ClientRatingViewModel;

public class RatingsActivity extends AppCompatActivity
{
    // create all the UI elements and necessary variables
    private ClientRatingViewModel clientRatingViewModel;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitRatingButton;
    private TextView businessNameTextView, ratingsStatusTextView;
    private ProgressBar ratingsProgressBar;
    private Client client;
    private Business business;
    private Rating currentRating;

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

        clientRatingViewModel = new ClientRatingViewModel();

        currentRating = null;
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitRatingButton = findViewById(R.id.submitRatingButton);
        businessNameTextView = findViewById(R.id.rateBusinessText);
        ratingsStatusTextView = findViewById(R.id.ratingsStatusTextView);
        ratingsProgressBar = findViewById(R.id.ratingsStatusProgressBar);
        business = getIntent().getParcelableExtra("business");
        client = getIntent().getParcelableExtra("client");

        if (business == null || client == null)
            finish();

        businessNameTextView.setText("Rate " + business.getBusinessName());

        clientRatingViewModel.checkIfRatingExists(business, client).addOnSuccessListener(task ->
        {
            // Rating exists
            if(task != null)
            {
                currentRating = task;
                commentEditText.setText(currentRating.getComment());
                ratingBar.setRating(currentRating.getStars());
            }
        });

        // observers
        setupObservers();

        // events for buttons
        setupClickListeners();
    }

    private void setupObservers()
    {
        clientRatingViewModel.getRatingsUiState().observe(this, ratingUiState ->
        {
            if (ratingUiState == null) return;
            Intent intent;

            switch (ratingUiState.getStatus())
            {
                case IDLE:
                    ratingsProgressBar.setVisibility(View.GONE);
                    submitRatingButton.setEnabled(true);
                    ratingsStatusTextView.setVisibility(View.GONE);
                    break;
                case LOADING:
                    ratingsProgressBar.setVisibility(View.VISIBLE);
                    submitRatingButton.setEnabled(false);
                    ratingsStatusTextView.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    ratingsProgressBar.setVisibility(View.GONE);
                    submitRatingButton.setEnabled(false);
                    ratingsStatusTextView.setVisibility(View.GONE);
                    // navigate to main screen after success
                    intent = new Intent(RatingsActivity.this, MainActivity.class);
                    intent.putExtra("client", client);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // can't go back
                    break;
                case ERROR:
                    ratingsProgressBar.setVisibility(View.GONE);
                    submitRatingButton.setEnabled(true);
                    ratingsStatusTextView.setText(ratingUiState.getErrorMessage());
                    ratingsStatusTextView.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setupClickListeners()
    {
        submitRatingButton.setOnClickListener(view ->
        {
            float stars = ratingBar.getRating();
            String comment = commentEditText.getText().toString().trim();
            //Rating rating = new Rating(stars, comment, client);
            if(currentRating == null)
                clientRatingViewModel.addRating(stars, comment, client, business)
                        .addOnSuccessListener(task -> {
                            if(task)
                            {
                                Toast.makeText(this, "Successfully added a review", Toast.LENGTH_LONG).show();
                            }
                            })
                        .addOnFailureListener(e ->
                            {
                                Toast.makeText(this, "Failed to add a review", Toast.LENGTH_LONG).show();
                            });
            else
                clientRatingViewModel.updateRating(business, new Rating(stars, comment, client))
                        .addOnSuccessListener(task -> {
                            if(task)
                            {
                                Toast.makeText(this, "Successfully updated a review", Toast.LENGTH_LONG).show();
                            }
                            })
                        .addOnFailureListener(e ->
                            {
                                Toast.makeText(this, "Failed to update a review", Toast.LENGTH_LONG).show();
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
