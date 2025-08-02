package com.example.workout.Business;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.FullScreenImageActivity;
import com.example.workout.R;

import java.util.Arrays;
import java.util.List;

import Adapters.GalleryAdapter;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_business);

        recyclerView = findViewById(R.id.galleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 עמודות

        List<Integer> imageList = Arrays.asList(
                R.drawable.image1,
                R.drawable.image2,
                R.drawable.image3
        );

        adapter = new GalleryAdapter(imageList, this::openFullScreenImage);
        recyclerView.setAdapter(adapter);
    }

    private void openFullScreenImage(int imageResId) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("imageResId", imageResId);
        startActivity(intent);
    }
}
