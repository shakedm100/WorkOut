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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workout.LoginActivity;
import com.example.workout.MainActivity;
import com.example.workout.R;
import com.example.workout.RegisterActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import ViewModel.RegisterViewModel;
import ViewModel.UpdateClientViewModel;

public class UpdateClientActivity extends AppCompatActivity
{
    // UI elements
    private UpdateClientViewModel updateClientViewModel;
    private EditText editTextEmail, editTextFirstName, editTextLastName;
    private EditText editTextPhoneNumber, editTextStreet;
    private Spinner spinnerPhonePrefix;
    private AutoCompleteTextView cityAutoComplete;
    private Button updateButton, cancelButton;
    private ProgressBar progressBarUpdate;
    private TextView updateStateTextView;

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_client);

        client = getIntent().getParcelableExtra("client");

        updateClientViewModel = new ViewModelProvider(this).get(UpdateClientViewModel.class);

        // Initialize UI elements
        //editTextEmail = findViewById(R.id.updateEmailText);
        editTextFirstName = findViewById(R.id.updateFirstNameText);
        editTextLastName = findViewById(R.id.updateLastNameText);
        spinnerPhonePrefix = findViewById(R.id.updatePrefixSpinner);
        editTextPhoneNumber = findViewById(R.id.updatePhoneText);
        cityAutoComplete = findViewById(R.id.UpdateCityAutoComplete);
        editTextStreet = findViewById(R.id.updateAddressText);
        updateButton = findViewById(R.id.updateButton);
        progressBarUpdate = findViewById(R.id.updateProgressBar);
        updateStateTextView = findViewById(R.id.updateStateTextView);
        cancelButton = findViewById(R.id.cancelUpdateButton);

        // set all the existing data to the edit text
        editTextFirstName.setText(client.getFirstName());
        editTextLastName.setText(client.getLastName());
        //editTextEmail.setText(client.getEmail());
        editTextPhoneNumber.setText(client.getPhone().getNumber());
        editTextStreet.setText(client.getAddress().getName());

        // set spinner value
        List<String> phonePrefixes = updateClientViewModel.getAllPhonePrefixes();
        ArrayAdapter<String> adapterPhonePrefixes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phonePrefixes);
        adapterPhonePrefixes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhonePrefix.setAdapter(adapterPhonePrefixes);

        int position = adapterPhonePrefixes.getPosition(client.getPhone().getNumber()); // uses equals()
        if (position >= 0)
        {
            spinnerPhonePrefix.setSelection(position);
        }
        else
        {
            // TODO: handle error
        }

        // set autocomplete value

        List<City> cities = new ArrayList<>();
        cities.add(client.getAddress().getCity());
        ArrayAdapter<City> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        cityAutoComplete.setAdapter(adapter);
        City matchedCity = adapter.getItem(0);
        //City c = client.getAddress().getCity();
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
                    updateStateTextView.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progressBarUpdate.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    updateStateTextView.setVisibility(View.GONE);
                    Toast.makeText(UpdateClientActivity.this,
                            "Update Successful!", Toast.LENGTH_LONG).show();
                    // navigate to login screen after success
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra("client", client); // add the client
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish update -> can't go back
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

    private void setupClickListeners() {
        updateButton.setOnClickListener(v -> {
            // Retrieve data from all EditText and Spinner fields
            //String email = editTextEmail.getText().toString().trim();
            //String password = editTextPassword.getText().toString().trim();
            //String username = editTextUsername.getText().toString().trim();
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();

            String phonePrefixStr = "";
            if (spinnerPhonePrefix.getSelectedItem() != null) {
                phonePrefixStr = spinnerPhonePrefix.getSelectedItem().toString();
            }

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
            updateClientViewModel.updateClient(firstName, lastName, phonePrefixStr, phoneNumber, selectedCity, street, client);
        });

        // get back to the profile page
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("client", client);
            startActivity(intent);
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
                            .addOnFailureListener(e -> { // TODO: remove it?
                                Toast.makeText(UpdateClientActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
