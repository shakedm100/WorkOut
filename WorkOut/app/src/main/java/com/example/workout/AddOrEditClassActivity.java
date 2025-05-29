package com.example.workout;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class AddOrEditClassActivity extends AppCompatActivity {

    private AutoCompleteTextView classNameInput, classTypeInput, categoryInput;
    private Spinner timeSpinner, ageRangeSpinner;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_class);

        // קישור ל־views
        classNameInput = findViewById(R.id.autoClassName);
        classTypeInput = findViewById(R.id.autoClassType);
        categoryInput = findViewById(R.id.autoCategory);
        timeSpinner = findViewById(R.id.spinnerTime);
        ageRangeSpinner = findViewById(R.id.spinnerAgeRange);
        saveButton = findViewById(R.id.buttonSave);

        // ערכים מוצעים לבחירה
        String[] classTypes = {"Yoga", "Dance", "Zumba", "Crossfit"};
        String[] categories = {"Fitness", "Relaxation", "Martial Arts"};
        String[] timeOptions = {"09:00 - 10:00", "10:30 - 11:30", "13:00 - 14:00", "18:00 - 19:00"};
        String[] ageRanges = {"3-5", "6-10", "11-15", "16+"};

        // אוטוקומפליט
        classTypeInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classTypes));
        categoryInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories));

        // ספינרים
        timeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeOptions));
        ageRangeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ageRanges));

        // לחיצה על שמירה
        saveButton.setOnClickListener(v -> {
            String className = classNameInput.getText().toString().trim();
            String classType = classTypeInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String time = timeSpinner.getSelectedItem().toString();
            String ageRange = ageRangeSpinner.getSelectedItem().toString();

            // כאן תוכל לשמור את הנתונים למסד הנתונים או לרשימה סטטית זמנית
            // לדוגמה:
            // ClassModel newClass = new ClassModel(className, classType, category, time, ageRange);
            // CourseRepository.addClass(day, newClass);

            finish(); // סגור את האקטיביטי
        });
    }
}

