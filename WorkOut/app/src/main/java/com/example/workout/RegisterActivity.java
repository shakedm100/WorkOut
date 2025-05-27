package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.ads.mediationtestsuite.viewmodels.ViewModelFactory;

import ViewModel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    // Declare UI elements
    private EditText editTextEmail, editTextPassword, editTextUsername, editTextFirstName, editTextLastName;
    private EditText editTextPhoneNumber, editTextStreet;
    private Spinner spinnerPhonePrefix, spinnerCity; // Assuming you have this in your XML: R.id.spinnerPhonePrefix
    // private Spinner spinnerGender; // If you use a spinner for Gender: R.id.spinnerGender
    private RadioGroup radioGroupGender;
    private Button buttonRegister;
    private ProgressBar progressBarRegister;
    private TextView textViewRegisterError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);

        // Initialize ViewModel (Using a Factory is good practice if ViewModel has dependencies)
        // For ClientRepository, it's good to pass it via factory for testability.
        // If your ViewModelFactory isn't set up yet for RegisterViewModel, you might temporarily
        // instantiate ClientRepository directly in the ViewModel for now, but aim to use a factory.
        ClientRepository clientRepository = new ClientRepository(); // Ideally from a DI source
        //ViewModelFactory viewModelFactory = new ViewModelFactory(getApplication(), clientRepository); // Adjust factory if needed
        //registerViewModel = new ViewModelProvider(this, viewModelFactory).get(RegisterViewModel.class);

        // Initialize UI elements (Ensure IDs match your registration_page.xml)
        editTextEmail = findViewById(R.id.emailText);
        editTextPassword = findViewById(R.id.passwordText);
        editTextUsername = findViewById(R.id.usernameText);
        editTextFirstName = findViewById(R.id.firstNameText);
        editTextLastName = findViewById(R.id.lastNameText);
        spinnerPhonePrefix = findViewById(R.id.p); // phone spinner
        editTextPhoneNumber = findViewById(R.id.phoneText);
        spinnerCity = findViewById(R.id.citySpinner);
        editTextStreet = findViewById(R.id.addressText);
        radioGroupGender = findViewById(R.id.radioGrp);

        buttonRegister = findViewById(R.id.registerBtn); // Your button ID for registration
        progressBarRegister = findViewById(R.id.progressBarRegister);
        textViewRegisterError = findViewById(R.id.textViewRegisterError);

        // Setup Observers for LiveData
        setupObservers();

        // Setup Click Listeners for buttons
        setupClickListeners();
    }

    private void setupObservers() {
        registerViewModel.getRegisterUiState().observe(this, registerUiState -> {
            if (registerUiState == null) return; // should not happen if initialized

            switch (registerUiState.getStatus()) {
                case IDLE:
                    progressBarRegister.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    textViewRegisterError.setVisibility(View.GONE);
                    break;
                case LOADING:
                    progressBarRegister.setVisibility(View.VISIBLE);
                    buttonRegister.setEnabled(false);
                    textViewRegisterError.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progressBarRegister.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    textViewRegisterError.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,
                            registerUiState.getSuccessMessage(), Toast.LENGTH_LONG).show();
                    // navigate to login screen after success
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                    startActivity(intent);
                    finish(); // Finish RegisterActivity
                    break;
                case ERROR:
                    progressBarRegister.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    textViewRegisterError.setText(registerUiState.getErrorMessage());
                    textViewRegisterError.setVisibility(View.VISIBLE);
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

            // Example for Spinner - get selected item as string
            // Make sure your spinner is populated with PhonePrefix string values
            String phonePrefixStr = "";
            if (spinnerPhonePrefix.getSelectedItem() != null) {
                phonePrefixStr = spinnerPhonePrefix.getSelectedItem().toString();
            }

            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String city = editTextCity.getText().toString().trim();
            String street = editTextStreet.getText().toString().trim();

            // String genderStr = "";
            // if(spinnerGender.getSelectedItem() != null) {
            //     genderStr = spinnerGender.getSelectedItem().toString();
            // }
            // TODO: Convert genderStr to Gender enum in ViewModel or before calling


            // Perform basic validation here if needed, or rely on ViewModel validation
            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty()
                    || lastName.isEmpty() || phonePrefixStr.isEmpty() || phoneNumber.isEmpty()
                    || city.isEmpty() || street.isEmpty() ) { // TODO: add gender validation and handle spinners
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                // Or set error on specific EditTexts
                // textViewRegisterError.setText("Please fill all required fields.");
                // textViewRegisterError.setVisibility(View.VISIBLE);
                return;
            }

            // Call the ViewModel method to perform registration
            registerViewModel.registerUser(
                    email, password, username, firstName, lastName,
                    phonePrefixStr, phoneNumber, city, street
                    // TODO: add gender
            );
        });

        // TODO: add back to login button
        // Button backToLoginButton = findViewById(R.id.backToLoginButtonRegister);
        // backToLoginButton.setOnClickListener(v -> {
        //     finish(); // Simply finish RegisterActivity to go back to LoginActivity
        // });
    }

    public void saveUserProfileToFirestore(String userId, String username, String email, String profilePictureUrl)
    {
        // Used for testing purposes
        ClientRepository clientRepository = new ClientRepository();
        Client sessionClient = null;
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("1","Oranit"), "Hayarkon");
        clientRepository.insertClient("Alice", "1234", phone, email, "Dvir",
                "Bento", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername()));
    }
}
