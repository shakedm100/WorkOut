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
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.CourseType;
import Model.Location;
import Model.Repository.BusinessRepository;
import Model.SearchStrategies.SearchAgeStrategy;
import Model.SearchStrategies.SearchCategoryStrategy;
import Model.SearchStrategies.SearchCourseTypeStrategy;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

public class SearchActivity extends AppCompatActivity
{
    SearchStrategyInterface searchStrategy;
    BusinessRepository businessRepository;
    RangeSlider ageSlider;
    AgeRange ageRange;
    CourseType chosenCourseType;
    EditText editTextCategory;
    EditText editTextLocation;
    final int LOCATION_PERMISSION_REQUEST = 1001;

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

        editTextCategory = findViewById(R.id.categoryAuto);
        editTextLocation = findViewById(R.id.locationAuto);
        businessRepository = new BusinessRepository();
        ageRange = new AgeRange(0,99);

        /*
         * /////////////////////////
         * CourseType Spinner Setup
         * /////////////////////////
         */

        Spinner typeSpinner = findViewById(R.id.typeSpinner);
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
        typeSpinner.setAdapter(adapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
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

        /*
         * /////////////////////////
         * Range Slider setup
         * /////////////////////////
         */

        ViewGroup container = findViewById(R.id.slider_container);

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

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(event -> executeAndGoToResults());
    }

    private void executeAndGoToResults()
    {
        executeSearch().addOnSuccessListener(task ->
        {
            // TODO: Go to search results page here!
            /*Intent intent = new Intent(this, )*/
        }).addOnFailureListener(task ->
        {
            // TODO: Show error to user
        });
    }

    /**
     * This method takes all the searchable elements and finds all corresponding businesses and
     * shows them, it searches only fields in the activity page that were changed by the user
     */
    private Task<List<Business>> executeSearch()
    {
        // TODO: Currently it's just a template how it can be implemented
        List<Task<List<Business>>> tasks = new ArrayList<>();

        if (!editTextCategory.getText().toString().trim().isEmpty())
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
        if (chosenCourseType != null)
        {
            tasks.add(searchCourseType());
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
                    // allDone.getResult() is List<Object>, each is a List<Business>
                    Map<String, Business> byId = new LinkedHashMap<>();
                    for (Object o : allDone.getResult())
                    {
                        @SuppressWarnings("unchecked")
                        List<Business> list = (List<Business>) o;
                        for (Business b : list)
                        {
                            byId.putIfAbsent(b.getId(), b);
                        }
                    }
                    return new ArrayList<>(byId.values());
                });
    }

    private Task<List<Business>> searchCategory()
    {
        // Category search handling
        String categoryText = editTextCategory.getText().toString();
        if (!categoryText.isEmpty())
        {
            Category category = Category.fromString(categoryText);
            searchStrategy = new SearchCategoryStrategy();
            return businessRepository.searchByStrategy(searchStrategy, category);
        }
        return null;
    }

    private Task<List<Business>> searchRadius()
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
                            return businessRepository.searchByStrategy(searchStrategy, locatinTask.getResult());
                        });
            }
            catch (Exception e)
            {
                throw new RuntimeException("Permission denied?"); // TODO: Show error on screen
            }
        }

        return null;
    }

    private Task<List<Business>> searchAgeRange()
    {
        searchStrategy = new SearchAgeStrategy();
        return businessRepository.searchByStrategy(searchStrategy, ageRange);
    }

    private Task<List<Business>> searchCourseType()
    {
        try
        {
            searchStrategy = new SearchCourseTypeStrategy();
            return businessRepository.searchByStrategy(searchStrategy, chosenCourseType);
        }
        catch (Exception e)
        {
            System.out.println("ERRORRRRRRRRRRR");
            return null;
        }
    }
}