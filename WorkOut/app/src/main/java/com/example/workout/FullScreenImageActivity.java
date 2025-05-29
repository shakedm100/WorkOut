package com.example.workout;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreenImage = new ImageView(this);
        fullScreenImage.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        fullScreenImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        setContentView(fullScreenImage);

        int imageResId = getIntent().getIntExtra("imageResId", -1);
        if (imageResId != -1) {
            fullScreenImage.setImageResource(imageResId);
        }
    }
}
