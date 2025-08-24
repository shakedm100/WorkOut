package com.example.workout.Business;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.workout.Client.ProfileActivity;
import com.example.workout.Client.UpdateClientActivity;
import com.example.workout.MainActivity;
import com.example.workout.R;

import java.util.ArrayList;
import java.util.List;

import Model.Business;
import Model.City;
import ViewModel.Business.BusinessUpdateDetailsViewModel;

public class BusinessUpdateActivity extends AppCompatActivity
{
    private Business business;

    // UI elements
    private BusinessUpdateDetailsViewModel updateBusinessDetailsViewModel;
    private EditText editTextName, editTextPhoneNumber, editTextStreet, editTextPolicy;
    private Spinner spinnerPhonePrefix;
    private AutoCompleteTextView cityAutoComplete;
    private Button updateButton;
    private ProgressBar progressBarUpdate;
    private TextView updateStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_business_details);

        business = getIntent().getParcelableExtra("business");

        updateBusinessDetailsViewModel = new ViewModelProvider(this).get(BusinessUpdateDetailsViewModel.class);

        // Initialize UI elements
        editTextName = findViewById(R.id.nameText);
        spinnerPhonePrefix = findViewById(R.id.businessPrefixSpinner);
        editTextPhoneNumber = findViewById(R.id.phoneText);
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        editTextStreet = findViewById(R.id.addressText);
        editTextPolicy = findViewById(R.id.policyText);
        updateButton = findViewById(R.id.updateButton);
        progressBarUpdate = findViewById(R.id.progressBar);
        updateStateTextView = findViewById(R.id.businessUpdateStatusTextView);

        // set all the existing data to the edit text
        editTextName.setText(business.getBusinessName());
        editTextPhoneNumber.setText(business.getPhone().getNumber());
        editTextStreet.setText(business.getAddress().getName());
        editTextPolicy.setText(business.getPolicy());

        // set spinner value
        List<String> phonePrefixes = updateBusinessDetailsViewModel.getAllPhonePrefixes();
        ArrayAdapter<String> adapterPhonePrefixes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phonePrefixes);
        adapterPhonePrefixes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhonePrefix.setAdapter(adapterPhonePrefixes);

        int position = adapterPhonePrefixes.getPosition(business.getPhone().getPrefix().getCode()); // uses equals()
        if (position >= 0)
            spinnerPhonePrefix.setSelection(position);
        else
            spinnerPhonePrefix.setSelection(0);

        // set autocomplete value
        List<City> cities = new ArrayList<>();
        cities.add(business.getAddress().getCity());
        ArrayAdapter<City> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        cityAutoComplete.setAdapter(adapter);
        City matchedCity = adapter.getItem(0);
        if (matchedCity != null) // added if
            cityAutoComplete.setText(matchedCity.getEnglishName(), false); // false prevents dropdown from opening

        // Setup Observers for LiveData
        setupObservers();

        // Setup Click Listeners for buttons
        setupClickListeners();
    }

    private void setupObservers() {
        updateBusinessDetailsViewModel.getUpdateUiState().observe(this, updateUiState -> {
            if (updateUiState == null) return;
            Intent intent;
            switch (updateUiState.getStatus()) {
                case IDLE:
                    progressBarUpdate.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    updateStateTextView.setVisibility(View.GONE);
                    break;
                case LOADING:
                    progressBarUpdate.setVisibility(View.VISIBLE);
                    updateButton.setEnabled(false);
                    updateStateTextView.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBarUpdate.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    updateStateTextView.setVisibility(View.GONE);
                    // navigate to the home screen after success
                    intent = new Intent(this, BusinessHomeActivity.class);
                    intent.putExtra("business", business); // add the business
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish update -> back to the previous screen
                    break;
                case ERROR:
                    progressBarUpdate.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    updateStateTextView.setText(updateUiState.getErrorMessage());
                    updateStateTextView.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setupClickListeners()
    {
        updateButton.setOnClickListener(v -> {

            // Trim strings
            String name = editTextName.getText().toString().trim();
            String policy = editTextPolicy.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String street = editTextStreet.getText().toString().trim();

            String phonePrefixStr = "";
            if (spinnerPhonePrefix.getSelectedItem() != null)
                phonePrefixStr = spinnerPhonePrefix.getSelectedItem().toString();

            String cityName = cityAutoComplete.getText().toString().trim();
            City selectedCity = null;
            if (cityAutoComplete.getAdapter() != null)
            {
                for (int i = 0; i < cityAutoComplete.getAdapter().getCount(); i++) {
                    City city = (City) cityAutoComplete.getAdapter().getItem(i);
                    String match = city.getEnglishName().trim();
                    if (match.equals(cityName)) {
                        selectedCity = city;
                        break;
                    }
                }
            }

            // Call the ViewModel method to perform update
            updateBusinessDetailsViewModel.updateBusiness(this.getApplicationContext(), name, phonePrefixStr, phoneNumber,
                    selectedCity, street, policy, business)
                    .addOnSuccessListener(task -> {
                        if(task)
                            Toast.makeText(this, "Successfully updated business details", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e ->
                    {
                        Toast.makeText(this, "Failed to update business details", Toast.LENGTH_LONG).show();
                    });
                });

//        // get back to the profile page
//        cancelButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, ProfileActivity.class);
//            intent.putExtra("client", client);
//            startActivity(intent);
//        });

        // get city autocomplete values when letters are typed
        cityAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence string, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence input, int start, int before, int count) {
                if (input.length() >= 2) {
                    updateBusinessDetailsViewModel.getBestMatchedCities(input.toString())
                            .addOnSuccessListener(cities -> {
                                ArrayAdapter<City> adapterCities = new ArrayAdapter<>(
                                        BusinessUpdateActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        cities
                                );
                                cityAutoComplete.setAdapter(adapterCities);
                                adapterCities.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(BusinessUpdateActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
