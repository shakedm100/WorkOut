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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import Model.City;
import Model.Gender;

import android.widget.Spinner;
import android.widget.Toast;

import com.example.workout.LoginActivity;
import com.example.workout.R;

import java.util.List;

import ViewModel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    // UI elements
    private RegisterViewModel registerViewModel;
    private EditText editTextEmail, editTextPassword, editTextUsername, editTextFirstName, editTextLastName;
    private EditText editTextPhoneNumber, editTextStreet, editBirthDate;
    private Spinner spinnerPhonePrefix;
    private AutoCompleteTextView cityAutoComplete;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale, radioFemale;
    private Button buttonRegister;
    private ProgressBar progressBarRegister;
    private TextView textViewRegisterState;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_client);

        // Initialize UI elements (Ensure IDs match your registration_page.xml)
        editTextEmail = findViewById(R.id.emailText);
        editTextPassword = findViewById(R.id.passwordText);
        editTextUsername = findViewById(R.id.usernameText);
        editTextFirstName = findViewById(R.id.firstNameText);
        editTextLastName = findViewById(R.id.lastNameText);
        spinnerPhonePrefix = findViewById(R.id.prefixSpinner);
        editTextPhoneNumber = findViewById(R.id.phoneText);
        editBirthDate = findViewById(R.id.RegisterBirthDateText);
        cityAutoComplete = findViewById(R.id.RegisterCityAutoComplete);
        editTextStreet = findViewById(R.id.addressText);
        radioGroupGender = findViewById(R.id.radioGrp);
        radioMale = findViewById(R.id.radioM);
        radioFemale = findViewById(R.id.radioF);
        buttonRegister = findViewById(R.id.registrationButton);
        progressBarRegister = findViewById(R.id.registerProgressBar);
        textViewRegisterState = findViewById(R.id.stateTextView);

        toolbar = findViewById(R.id.registerToolbar);
        setSupportActionBar(toolbar);

        // Enable back arrow in the toolbar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // set spinner values
        List<String> phonePrefixes = registerViewModel.getAllPhonePrefixes();
        ArrayAdapter<String> adapterPhonePrefixes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phonePrefixes);
        adapterPhonePrefixes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhonePrefix.setAdapter(adapterPhonePrefixes);

        // set autocomplete values
        // TextWatcher basically listens to the user's inputs
        // this will trigger the firebase and activate the query each time the user inputs at least 3 chars
        cityAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence string, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence input, int start, int before, int count) {
                if (input.length() >= 3) {
                    registerViewModel.getBestMatchedCities(input.toString())
                            .addOnSuccessListener(cities -> {
                                ArrayAdapter<City> adapterCities = new ArrayAdapter<>(
                                        RegisterActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        cities
                                );
                                cityAutoComplete.setAdapter(adapterCities);
                                adapterCities.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Setup Observers for LiveData
        setupObservers();

        // Setup Click Listeners for buttons
        setupClickListeners();
    }

    private void setupObservers() {
        registerViewModel.getRegisterUiState().observe(this, registerUiState -> {
            if (registerUiState == null) return; // should not happen if initialized
            Intent intent;

            switch (registerUiState.getStatus()) {
                case IDLE:
                    progressBarRegister.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    textViewRegisterState.setVisibility(View.GONE);
                    break;
                case LOADING:
                    progressBarRegister.setVisibility(View.VISIBLE);
                    buttonRegister.setEnabled(false);
                    textViewRegisterState.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progressBarRegister.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    textViewRegisterState.setVisibility(View.GONE);
                    // navigate to login screen after success
                    intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish RegisterActivity -> can't go back
                    break;
                case ERROR:
                    progressBarRegister.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    textViewRegisterState.setText(registerUiState.getErrorMessage());
                    textViewRegisterState.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> {
            // Retrieve data from all EditText and Spinner fields
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String birthdate = editBirthDate.getText().toString().trim();

            // Example for Spinner - get selected item as string
            // Make sure your spinner is populated with PhonePrefix string values
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

            Gender gender = null;
            if (radioMale.isChecked())
                gender = Gender.Male;

            else
                gender = Gender.Female;

            // Call the ViewModel method to perform registration
            registerViewModel.registerUser(email, password, username, firstName, lastName,
                    phonePrefixStr, phoneNumber, birthdate, selectedCity, street, gender, this.getApplicationContext())
                    .addOnSuccessListener(task -> {
                        if(task)
                        {
                            Toast.makeText(this, "Successfully registered", Toast.LENGTH_LONG).show();
                        }
                        }).addOnFailureListener(e ->
                        {
                            Toast.makeText(this, "Failed to register: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
        });

        // Handle arrow click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
