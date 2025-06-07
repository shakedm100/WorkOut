package com.example.workout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Model.Address;
import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.City;
import Model.Client;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Gender;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.CourseRepository;
import Model.SearchStrategies.SearchAgeStrategy;
import Model.SearchStrategies.SearchCategoryStrategy;
import Model.SearchStrategies.SearchCourseTypeStrategy;
import Model.SearchStrategies.SearchDateStrategy;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

public class SearchActivity extends AppCompatActivity
{
    private SearchStrategyInterface searchStrategy;
    private CourseRepository courseRepository;
    private RangeSlider ageSlider;
    private AgeRange ageRange;
    private CourseType chosenCourseType;
    private Spinner categorySpinner, courseTypeSpinner, dayOfWeekSpinner;
    private EditText editTextLocation;
    private ViewGroup container;
    private Button startTimeButton, endTimeButton;
    private LocalTime startTime, endTime;
    private Timestamp[] times;
    private final int LOCATION_PERMISSION_REQUEST = 1001;
    private BottomNavigationView bottomNavigationView;
    private Client current;
    private MaterialButtonToggleGroup toggleGroup;
    private Button searchButton;
    private boolean isList, isTest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        isTest = false;
        categorySpinner = findViewById(R.id.categorySearchSpinner);
        editTextLocation = findViewById(R.id.locationSearchEditText);
        courseTypeSpinner = findViewById(R.id.typeSpinner);
        courseRepository = new CourseRepository();
        container = findViewById(R.id.search_slider_container);
        ageRange = new AgeRange(0, 99);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSearchSpinner);
        startTime = LocalTime.of(0, 0);
        endTime = LocalTime.of(23, 59);
        times = new Timestamp[2];
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_search);
        startTimeButton = findViewById(R.id.startTimeButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        toggleGroup = findViewById(R.id.viewModeToggle);
        searchButton = findViewById(R.id.searchButton);

        isList = true;
        toggleGroup.check(R.id.toggle_list);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) ->
        {
           if(!isChecked)
               return;

           if(checkedId == R.id.toggle_map)
               isList = false;
           else
               isList = true;
        });

        current = getIntent().getParcelableExtra("client");

        setUpCourseTypeSpinner();
        setUpCategorySpinner();
        setUpAgeSlider();
        setUpDayOfWeekSpinner();
        setUpBottomNavigationView();
        setUpTimePicker();

        searchButton.setOnClickListener(event -> executeAndGoToResults());
    }

    private void setUpTimePicker()
    {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H).setHour(0).setMinute(0);
        MaterialTimePicker startPicker = builder.setTitleText("Select start time").build();

        builder = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(23).setMinute(59);
        MaterialTimePicker endPicker = builder.setTitleText("Select end time").build();

        startPicker.addOnPositiveButtonClickListener(v ->
        {
            int hours = startPicker.getHour();
            int minutes = startPicker.getMinute();
            startTime = LocalTime.of(hours, minutes);
        });

        endPicker.addOnPositiveButtonClickListener(v ->
        {
            int hours = endPicker.getHour();
            int minutes = endPicker.getMinute();
            endTime = LocalTime.of(hours, minutes);
        });

        startTimeButton.setOnClickListener(v ->
        {
            startPicker.show(getSupportFragmentManager(), "START_PICKER");
        });
        endTimeButton.setOnClickListener(v ->
        {
            endPicker.show(getSupportFragmentManager(), "END_PICKER");
        });
    }

    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_search)
            {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_ratings)
            {
                Intent intent = new Intent(this, RatingsActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_messages)
            {
                Intent intent = new Intent(this, MessagesActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void executeAndGoToResults()
    {
        executeSearch().addOnSuccessListener(courses ->
        {
            ArrayList<Course> courseArrayList = new ArrayList<>(courses);
            Intent intent;
            TextView tempTextView = findViewById(R.id.searchTestTextView);

            if(isList)
            {
                intent = new Intent(this, SearchResultsActivity.class);
                if(isTest)
                    tempTextView.setText("Successfully searching and showing list");
            }
            else
            {
                intent = new Intent(this, MapActivity.class);
                if(isTest)
                    tempTextView.setText("Successfully searching and showing map");
            }

            intent.putParcelableArrayListExtra("course_list", courseArrayList);
            intent.putExtra("client", current);
            if(!isTest)
                startActivity(intent);
        }).addOnFailureListener(task ->
        {
            // TODO: Show error to user
        });
    }

    private void setUpCourseTypeSpinner()
    {
        CourseType[] types = CourseType.values();
        String[] typeNames = new String[types.length + 1];
        typeNames[0] = "Choose Course Type";
        for (int i = 1; i <= types.length; i++)
        {
            typeNames[i] = types[i - 1].name();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(typeNames)
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseTypeSpinner.setAdapter(adapter);

        courseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                String picked = (String) adapterView.getItemAtPosition(i);
                if (!picked.equals("Choose Course Type"))
                    chosenCourseType = CourseType.fromString(picked);
                else
                    chosenCourseType = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                chosenCourseType = null;
            }
        });
    }

    private void setUpCategorySpinner()
    {
        Category[] types = Category.values();
        String[] typeNames = new String[types.length + 1];
        typeNames[0] = "Choose Category";
        for (int i = 1; i <= types.length; i++)
        {
            typeNames[i] = types[i - 1].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(typeNames));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setUpAgeSlider()
    {
        // Create the RangeSlider
        ageSlider = new RangeSlider(this);

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

    private void setUpDayOfWeekSpinner()
    {
        Day[] types = Day.values();
        String[] typeNames = new String[types.length + 1];
        typeNames[0] = "Choose Day";
        for (int i = 1; i <= types.length; i++)
        {
            typeNames[i] = types[i - 1].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(typeNames));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);
    }

    /**
     * This method takes all the searchable elements and finds all corresponding businesses and
     * shows them, it searches only fields in the activity page that were changed by the user
     */
    private Task<List<Course>> executeSearch()
    {
        List<Task<List<Course>>> tasks = new ArrayList<>();

        String category = categorySpinner.getSelectedItem().toString().trim();
        String courseType = courseTypeSpinner.getSelectedItem().toString().trim();
        String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString().trim();

        if(startTime.getHour() != 0 || startTime.getMinute() != 0 ||
        endTime.getHour() != 23 || endTime.getMinute() != 59)
        {
            LocalDate epochDate = LocalDate.of(1970, 1, 1);
            LocalDateTime combined = LocalDateTime.of(epochDate, startTime);
            Instant instant = combined.toInstant(ZoneOffset.UTC);
            long secondsSinceEpoch = instant.getEpochSecond();
            int nanos = instant.getNano();
            Timestamp starTimeTimestamp = new Timestamp(secondsSinceEpoch, nanos);

            combined = LocalDateTime.of(epochDate, endTime);
            instant = combined.toInstant(ZoneOffset.UTC);
            secondsSinceEpoch = instant.getEpochSecond();
            nanos = instant.getNano();
            Timestamp endTimeTimestamp = new Timestamp(secondsSinceEpoch, nanos);

            times[0] = starTimeTimestamp;
            times[1] = endTimeTimestamp;
        }

        if (!category.equals("Choose Category"))
        {
            tasks.add(searchCategory());
        }
        if (!editTextLocation.getText().toString().trim().isEmpty())
        {
            tasks.add(searchRadius());
        }
        if (ageRange.getMinAge() != 0 || ageRange.getMaxAge() != 99)
        {
            tasks.add(searchAgeRange());
        }
        if (!courseType.equals("Choose Course Type"))
        {
            tasks.add(searchCourseType());
        }
        if(times[0] != null && times[1] != null && !dayOfWeek.equals("Choose Day"))
        {
            tasks.add(searchDayTime());
        }


        // If no filters, return empty immediately
        if (tasks.isEmpty())
        {
            return Tasks.forResult(Collections.emptyList());
        }

        // Wait for all to finish successfully
        return Tasks.whenAllSuccess(tasks)
                .continueWith(allDone ->
                {
                    // allDone.getResult() is List<Object>, each is a List<Course>
                    Map<String, Course> byId = new LinkedHashMap<>();
                    for (Object o : allDone.getResult())
                    {
                        @SuppressWarnings("unchecked")
                        List<Course> list = (List<Course>) o;
                        for (Course course : list)
                        {
                            byId.putIfAbsent(course.getId(), course);
                        }
                    }
                    return new ArrayList<>(byId.values());
                });
    }

    private Task<List<Course>> searchCategory()
    {
        // Category search handling
        String categoryText = categorySpinner.getSelectedItem().toString();
        if (!categoryText.isEmpty())
        {
            Category category = Category.fromString(categoryText);
            searchStrategy = new SearchCategoryStrategy();
            return courseRepository.searchByStrategy(searchStrategy, category);
        }
        return null;
    }

    private Task<List<Course>> searchRadius()
    {
        // Location search handling
        String locationText = editTextLocation.getText().toString().trim();
        double radius = Double.parseDouble(locationText);
        if (!locationText.isEmpty())
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                // Request location permission if needed
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            }
            try
            {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                return fusedLocationClient.getLastLocation()
                        .continueWithTask(locatinTask ->
                        {
                            Location location = new Location(locatinTask.getResult().getLatitude(),
                                    locatinTask.getResult().getLongitude());
                            searchStrategy = new SearchRadiusStrategy(radius);
                            return courseRepository.searchByStrategy(searchStrategy, locatinTask.getResult());
                        });
            }
            catch (Exception e)
            {
                throw new RuntimeException("Permission denied?"); // TODO: Show error on screen
            }
        }

        return null;
    }

    private Task<List<Course>> searchAgeRange()
    {
        searchStrategy = new SearchAgeStrategy();
        return courseRepository.searchByStrategy(searchStrategy, ageRange);
    }

    private Task<List<Course>> searchCourseType()
    {
        try
        {
            searchStrategy = new SearchCourseTypeStrategy();
            return courseRepository.searchByStrategy(searchStrategy, chosenCourseType);
        }
        catch (Exception e)
        {
            System.out.println("ERRORRRRRRRRRRR");
            return null;
        }
    }

    private Task<List<Course>> searchDayTime()
    {
        try
        {
            searchStrategy = new SearchDateStrategy();
            return courseRepository.searchByStrategy(searchStrategy, times);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTestingConditions(CourseRepository repo, Course course)
    {
        this.courseRepository = repo;
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("Rosh Ha'Ayin"), "Haim Hertzog");
        current = new Client("esdrg","shakedm100", "1234", phone,
                "shaked1mi@gmail.com", "Shaked","Michael", address, Gender.Male);
        isTest = true;
    }
}