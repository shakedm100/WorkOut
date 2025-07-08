package com.example.workout.Client;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.workout.R;
import com.example.workout.RegisterActivity;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import Model.City;
import Model.Client;
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
    private Button updateButton;
    private ProgressBar progressBarUpdate;
    private TextView updateStateTextView;

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_client);

        client = getIntent().getParcelableExtra("client");

        // Initialize UI elements
        editTextEmail = findViewById(R.id.updateEmailText);
        editTextFirstName = findViewById(R.id.updateFirstNameText);
        editTextLastName = findViewById(R.id.updateLastNameText);
        spinnerPhonePrefix = findViewById(R.id.updatePrefixSpinner);
        editTextPhoneNumber = findViewById(R.id.updatePhoneText);
        cityAutoComplete = findViewById(R.id.UpdateCityAutoComplete);
        editTextStreet = findViewById(R.id.updateAddressText);
        updateButton = findViewById(R.id.updateButton);
        progressBarUpdate = findViewById(R.id.updateProgressBar);
        updateStateTextView = findViewById(R.id.updateStateTextView);

        // set all the existing data to the edit text
        editTextFirstName.setText(client.getFirstName());
        editTextLastName.setText(client.getLastName());
        editTextEmail.setText(client.getEmail());
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

//        List<City> cities = new ArrayList<>();
//        cities.add(client.getAddress().getCity());
//        ArrayAdapter<City> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
//        cityAutoComplete.setAdapter(adapter);
//        City matchedCity = adapter.getItem(0);
        cityAutoComplete.setText(client.getAddress().getCity().getName(), false); // false prevents dropdown from opening
    }
}
