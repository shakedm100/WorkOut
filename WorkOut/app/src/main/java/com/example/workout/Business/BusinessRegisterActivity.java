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

import com.example.workout.LoginActivity;
import com.example.workout.R;

import java.util.List;

import Model.City;

import ViewModel.RegisterBusinessViewModel;

public class BusinessRegisterActivity extends AppCompatActivity
{
    private RegisterBusinessViewModel registerBusinessViewModel;
    private EditText editTextEmail, editTextPassword, editTextUsername, editNameText, editTextAddress, editTextPhoneNumber, editPolicyText;
    private Spinner spinnerPhonePrefix;
    private AutoCompleteTextView cityAutoComplete;
    private ProgressBar statusProgressBar;
    private Button buttonRegister;
    private TextView businessRegisterStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_business);

        editTextEmail = findViewById(R.id.emailText);
        editTextPassword = findViewById(R.id.passwordText);
        editTextUsername = findViewById(R.id.usernameText);
        editNameText = findViewById(R.id.nameText);
        spinnerPhonePrefix = findViewById(R.id.businessPrefixSpinner);
        editTextPhoneNumber = findViewById(R.id.phoneText);
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        editTextAddress = findViewById(R.id.addressText);
        buttonRegister = findViewById(R.id.registrationButton);
        statusProgressBar = findViewById(R.id.progressBar);
        businessRegisterStatusTextView = findViewById(R.id.businessRegisterStatusTextView);
        editPolicyText = findViewById(R.id.policyText);

        // ApplicationProvider.getApplicationContext();
        //         registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        registerBusinessViewModel = new ViewModelProvider(this).get(RegisterBusinessViewModel.class);

        List<String> phonePrefixes = registerBusinessViewModel.getAllPhonePrefixes();
        ArrayAdapter<String> adapterPhonePrefixes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phonePrefixes);
        adapterPhonePrefixes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhonePrefix.setAdapter(adapterPhonePrefixes);

        cityAutoComplete.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence string, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence input, int start, int before, int count)
            {
                if (input.length() >= 3)
                {
                    registerBusinessViewModel.getBestMatchedCities(input.toString())
                            .addOnSuccessListener(cities ->
                            {
                                ArrayAdapter<City> adapterCities = new ArrayAdapter<>(
                                        BusinessRegisterActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        cities
                                );
                                cityAutoComplete.setAdapter(adapterCities);
                                adapterCities.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e ->
                            {
                                Toast.makeText(BusinessRegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        // observers
        setupObservers();

        // events for buttons
        setupClickListeners();
    }

    private void setupObservers()
    {
        registerBusinessViewModel.getRegisterUiState().observe(this, registerUiState ->
        {
            if (registerUiState == null) return; // should not happen if initialized
            Intent intent;

            switch (registerUiState.getStatus())
            {
                case IDLE:
                    statusProgressBar.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    businessRegisterStatusTextView.setVisibility(View.GONE);
                    break;
                case LOADING:
                    statusProgressBar.setVisibility(View.VISIBLE);
                    buttonRegister.setEnabled(false);
                    businessRegisterStatusTextView.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    statusProgressBar.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    businessRegisterStatusTextView.setVisibility(View.GONE);
                    Toast.makeText(BusinessRegisterActivity.this,
                            "Registration Successful!", Toast.LENGTH_LONG).show();
                    // navigate to login screen after success
                    intent = new Intent(BusinessRegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish RegisterActivity -> can't go back
                    break;
                case ERROR:
                    statusProgressBar.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    businessRegisterStatusTextView.setText(registerUiState.getErrorMessage());
                    businessRegisterStatusTextView.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setupClickListeners()
    {
        buttonRegister.setOnClickListener(v ->
        {
            // extract data
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();
            String name = editNameText.getText().toString().trim();
            String addressStr = editTextAddress.getText().toString().trim();
            String phoneNumberStr = editTextPhoneNumber.getText().toString().trim();
            String policy = editPolicyText.getText().toString().trim();

            String phonePrefixStr = "";
            if (spinnerPhonePrefix.getSelectedItem() != null)
                phonePrefixStr = spinnerPhonePrefix.getSelectedItem().toString();

            String cityName = cityAutoComplete.getText().toString().trim();
            City selectedCity = null;
            if (cityAutoComplete.getAdapter() != null)
            {
                for (int i = 0; i < cityAutoComplete.getAdapter().getCount(); i++)
                {
                    City city = (City) cityAutoComplete.getAdapter().getItem(i);
                    String match = city.getEnglishName().trim();
                    if (match.equals(cityName))
                    {
                        selectedCity = city;
                        break;
                    }
                }

            }

            // try to register
            registerBusinessViewModel.registerBusiness(this.getApplicationContext(),
                    username, password, phonePrefixStr, phoneNumberStr, email, name, selectedCity, addressStr, policy
            );
        });

        // TODO: add back to login button
        // Button backToLoginButton = findViewById(R.id.backToLoginButtonRegister);
        // backToLoginButton.setOnClickListener(v -> {
        //     finish(); // Simply finish RegisterActivity to go back to LoginActivity
        // });
    }
}
