package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Repository.CourseRepository;
import Model.Schedule;

public class AddOrEditClassActivity extends AppCompatActivity
{

    private EditText courseNameEditText, capacityEditText, descriptionEditText;
    private Spinner dayOfWeekSpinner, courseTypeSpinner, categorySpinner;
    private Button saveButton, startTimeButton, endTimeButton;
    private RangeSlider ageSlider;
    private AgeRange ageRange;
    ViewGroup container;
    Course currentCourse;
    Business currentBusiness;
    private CourseRepository courseRepository;
    private LocalTime startTime, endTime;

    // Dummy comment
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_class);

        try
        {
            currentCourse = this.getIntent().getParcelableExtra("course");
        }
        catch (Exception e)
        {
            currentCourse = null; // Sent "null"
        }
        currentBusiness = this.getIntent().getParcelableExtra("business");

        courseRepository = new CourseRepository();
        ageRange = new AgeRange(0, 99);
        startTime = null;
        endTime = null;

        // Link to views
        courseNameEditText = findViewById(R.id.autoClassName);
        courseTypeSpinner = findViewById(R.id.autoCourseType);
        categorySpinner = findViewById(R.id.autoCategory);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        saveButton = findViewById(R.id.buttonSave);
        container = findViewById(R.id.update_course_slider_container);
        capacityEditText = findViewById(R.id.capacityEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        ageSlider = new RangeSlider(this);
        startTimeButton = findViewById(R.id.startTimeButton);
        endTimeButton = findViewById(R.id.endTimeButton);

        createCourseTypeSpinner();
        createCategorySpinner();
        createAgeSlider();
        createDayOfWeekSpinner();

        if (currentCourse != null) // Then it's update course
        {
            courseNameEditText.setHint(currentCourse.getName());

            ArrayList<Float> values = new ArrayList<>();
            values.add((float) currentCourse.getAgeRange().getMinAge());
            values.add((float) currentCourse.getAgeRange().getMaxAge());
            ageSlider.setValues(values); // Set the current ages to [minAge, maxAge]

            capacityEditText.setText(currentCourse.getCapacity());
            descriptionEditText.setText(currentCourse.getDescription());
        }

        // Press on save
        saveButton.setOnClickListener(v ->
        {
            boolean check = buildCourseFromFieldsAndQuery();
            if (check)
                finish();
            else
            {
                //TODO: Show error
            }
        });

        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H).setHour(12).setMinute(0);

        MaterialTimePicker startPicker = builder.setTitleText("Select start time").build();

        startPicker.addOnPositiveButtonClickListener(v ->
        {
            int hours = startPicker.getHour();
            int minutes = startPicker.getMinute();
            startTime = LocalTime.of(hours, minutes);
        });

        MaterialTimePicker endPicker = builder
                .setTitleText("Select end time")
                .build();

        endPicker.addOnPositiveButtonClickListener(v ->
        {
            int hours = endPicker.getHour();
            int minutes = endPicker.getMinute();
            endTime = LocalTime.of(hours, minutes);
        });

        findViewById(R.id.startTimeButton).setOnClickListener(v ->
        {
            startPicker.show(getSupportFragmentManager(), "START_PICKER");
        });
        findViewById(R.id.endTimeButton).setOnClickListener(v ->
        {
            endPicker.show(getSupportFragmentManager(), "END_PICKER");
        });
    }

    private void createCourseTypeSpinner()
    {
        CourseType[] types = CourseType.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++)
        {
            typeNames[i] = types[i].name();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(typeNames));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseTypeSpinner.setAdapter(adapter);

        if (currentCourse != null)
        {
            String currentCourseType = currentCourse.getType().toString();
            int index = adapter.getPosition(currentCourseType);
            courseTypeSpinner.setSelection(index);
        }
    }

    private void createCategorySpinner()
    {
        Category[] types = Category.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++)
        {
            typeNames[i] = types[i].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(typeNames));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (currentCourse != null)
        {
            String currentCategory = currentCourse.getCategory().toString();
            int index = adapter.getPosition(currentCategory);
            categorySpinner.setSelection(index);
        }
    }

    private void createDayOfWeekSpinner()
    {
        Day[] types = Day.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++)
        {
            typeNames[i] = types[i].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(typeNames));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        if (currentCourse != null)
        {
            String currentDay = currentCourse.getSchedule().getDay().toString();
            int index = adapter.getPosition(currentDay);
            dayOfWeekSpinner.setSelection(index);
        }
    }

    private void createAgeSlider()
    {
        // Configure its properties
        ageSlider.setValueFrom(0f);
        ageSlider.setValueTo(99f);
        ageSlider.setStepSize(1f);
        // Initial edges at [0, 99]
        ageSlider.setValues(Arrays.asList(0f, 99f));
        // Show labels above edges
        ageSlider.setLabelFormatter(value -> String.format(Locale.getDefault(), "%d", (int) value));

        // Hook up your listener
        ageSlider.addOnChangeListener((slider, value, fromUser) ->
        {
            List<Float> vals = slider.getValues();
            int minAge = vals.get(0).intValue();
            int maxAge = vals.get(1).intValue();
            ageRange = new AgeRange(minAge, maxAge);
        });

        // Add some LayoutParams so it measures properly
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(16, 16, 16, 16);
        ageSlider.setLayoutParams(lp);

        // Change the god-awful purple
        ColorStateList blueTint = ColorStateList.valueOf(Color.BLUE);

        // tint the “filled” part of the track and the thumbs
        ageSlider.setTrackActiveTintList(blueTint);
        ageSlider.setThumbTintList(blueTint);

        // (optional) tint the unfilled track, if you want contrast
        ageSlider.setTrackInactiveTintList(ColorStateList.valueOf(Color.LTGRAY));

        // Finally—add it into your container
        container.addView(ageSlider);
    }

    private boolean buildCourseFromFieldsAndQuery()
    {
        CourseType courseType = CourseType.valueOf(courseTypeSpinner.getSelectedItem().toString());
        String courseName = courseNameEditText.getText().toString().trim();
        int capacity = Integer.parseInt(capacityEditText.getText().toString());
        // Use the page's age range
        // TODO: Need to implement Schedule
        Schedule schedule = new Schedule(Day.Saturday, Timestamp.now());
        Category category = Category.valueOf(categorySpinner.getSelectedItem().toString());
        String description = descriptionEditText.getText().toString();
        if (currentCourse == null) // Insert
        {
            try
            {
                currentCourse = await(courseRepository.insertCourse(currentBusiness, courseName, schedule,
                        capacity, courseType, ageRange, category, description), 8, TimeUnit.SECONDS);
                return true;
            }
            catch (Exception e)
            {
                // TODO: Show error on screen
                return false;
            }
        }
        else // Update
        {
            if (courseType != currentCourse.getType())
                currentCourse.setType(courseType);
            if (!courseName.equals(currentCourse.getName()))
                currentCourse.setName(courseName);
            if (capacity != currentCourse.getCapacity())
                currentCourse.setCapacity(capacity);
            if (!schedule.equals(currentCourse.getSchedule()))
                currentCourse.setSchedule(schedule);
            if (category != currentCourse.getCategory())
                currentCourse.setCategory(category);
            if (!description.equals(currentCourse.getDescription()))
                currentCourse.setDescription(description);

            try
            {
                return await(courseRepository.updateCourse(currentCourse, currentBusiness), 8, TimeUnit.SECONDS);
            }
            catch (Exception e)
            {
                // TODO: Show error on screen
                return false;
            }
        }
    }
}
