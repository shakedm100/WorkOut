package com.example.workout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button categoryButton = findViewById(R.id.searchByCategoryButton);
        categoryButton.setOnClickListener(v -> showCategoryDialog());
    }

    private void showCategoryDialog() {
        String[] categories = getResources().getStringArray(R.array.categories_array);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר קטגוריה");
        builder.setItems(categories, (dialog, which) -> {
            String selectedCategory = categories[which];
            Toast.makeText(getApplicationContext(), "נבחר: " + selectedCategory, Toast.LENGTH_SHORT).show();

            // כאן אפשר להעביר למסך תוצאות או לעדכן UI
        });

        builder.show();
    }
}
