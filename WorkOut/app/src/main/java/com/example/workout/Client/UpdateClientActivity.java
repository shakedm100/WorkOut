package com.example.workout.Client;

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
import androidx.appcompat.widget.Toolbar;

import com.example.workout.MainActivity;
import com.example.workout.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import Model.City;
import Model.Client;
import ViewModel.UpdateClientViewModel;

public class UpdateClientActivity extends AppCompatActivity
{
    // UI elements
    private UpdateClientViewModel updateClientViewModel;
    private EditText editTextFirstName, editTextLastName;
    private EditText editTextPhoneNumber, editTextStreet;
    private Spinner spinnerPhonePrefix;
    private AutoCompleteTextView cityAutoComplete;
    private Button updateButton;
    private ProgressBar progressBarUpdate;
    private TextView updateStateTextView;
    private Toolbar toolbar;
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_client);

        client = getIntent().getParcelableExtra("client");

        updateClientViewModel = new ViewModelProvider(this).get(UpdateClientViewModel.class);

        // Initialize UI elements
        editTextFirstName = findViewById(R.id.updateFirstNameText);
        editTextLastName = findViewById(R.id.updateLastNameText);
        spinnerPhonePrefix = findViewById(R.id.updatePrefixSpinner);
        editTextPhoneNumber = findViewById(R.id.updatePhoneText);
        cityAutoComplete = findViewById(R.id.UpdateCityAutoComplete);
        editTextStreet = findViewById(R.id.updateAddressText);
        updateButton = findViewById(R.id.updateButton);
        progressBarUpdate = findViewById(R.id.updateProgressBar);
        updateStateTextView = findViewById(R.id.updateStateTextView);

        toolbar = findViewById(R.id.updateClientToolbar);
        setSupportActionBar(toolbar);

        // Enable back arrow in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // set all the existing data to the edit text
        editTextFirstName.setText(client.getFirstName());
        editTextLastName.setText(client.getLastName());
        editTextPhoneNumber.setText(client.getPhone().getNumber());
        editTextStreet.setText(client.getAddress().getName());

        // set spinner value
        List<String> phonePrefixes = updateClientViewModel.getAllPhonePrefixes();
        ArrayAdapter<String> adapterPhonePrefixes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phonePrefixes);
        adapterPhonePrefixes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhonePrefix.setAdapter(adapterPhonePrefixes);

        int position = adapterPhonePrefixes.getPosition(client.getPhone().getPrefix().getCode()); // uses equals()
        if (position >= 0)
            spinnerPhonePrefix.setSelection(position);
        else
            spinnerPhonePrefix.setSelection(0);

        // set autocomplete value

        List<City> cities = new ArrayList<>();
        cities.add(client.getAddress().getCity());
        ArrayAdapter<City> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        cityAutoComplete.setAdapter(adapter);
        City matchedCity = adapter.getItem(0);
        cityAutoComplete.setText(matchedCity.getEnglishName(), false); // false prevents dropdown from opening

        // Setup Observers for LiveData
        setupObservers();
        // Setup Click Listeners for buttons
        setupClickListeners();
    }

    private void setupObservers() {
        updateClientViewModel.getUpdateUiState().observe(this, updateUiState -> {
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
                    // navigate to the main screen after success
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("client", client); // add the client
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

            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();

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

            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String street = editTextStreet.getText().toString().trim();

            // Call the ViewModel method to perform update
            updateClientViewModel.updateClient(this.getApplicationContext(), firstName, lastName, phonePrefixStr, phoneNumber,
                    selectedCity, street, client).addOnSuccessListener(task -> {
                        if(task)
                        {
                            Toast.makeText(this, "Successfully updated details", Toast.LENGTH_LONG).show();
                        }
                        }).addOnFailureListener(e ->
                        {
                            Toast.makeText(this, "Failed to update details", Toast.LENGTH_LONG).show();
                        });
            });

        // get city autocomplete values when letters are typed
        cityAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence string, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence input, int start, int before, int count) {
                if (input.length() >= 2) {
                    updateClientViewModel.getBestMatchedCities(input.toString())
                            .addOnSuccessListener(cities -> {
                                ArrayAdapter<City> adapterCities = new ArrayAdapter<>(
                                        UpdateClientActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        cities
                                );
                                cityAutoComplete.setAdapter(adapterCities);
                                adapterCities.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UpdateClientActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Handle arrow click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
