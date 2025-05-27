package com.example.workout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitRatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitRatingButton = findViewById(R.id.submitRatingButton);

        submitRatingButton.setOnClickListener(view -> {
            float rating = ratingBar.getRating();
            String comment = commentEditText.getText().toString().trim();

            // כאן אפשר לשלוח את הנתונים ל־Firebase או להדפיס
            Toast.makeText(this,
                    "דירוג: " + rating + "\nתגובה: " + comment,
                    Toast.LENGTH_LONG).show();

            // TODO: שליחה ל-Firebase
        });
    }
}
