package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.Scopes;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import ViewModel.MyViewModel;
//import Model.Repository.TestClientRepository;

// ViewModel imports
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ViewModel.MyViewModel;
//

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity; // Or androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider;

// Assuming LoginUiState and LoginViewModel are in the ViewModel package
import ViewModel.LoginUiState; // Update with your actual package
import ViewModel.LoginViewModel; // Update with your actual package
// import com.example.workout.R; // For accessing R.id values

import android.util.Log;

public class LoginActivity extends AppCompatActivity {
    private CredentialManager credentialManager;
    private FirebaseAuth auth;
    private LoginViewModel loginViewModel;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        credentialManager = CredentialManager.create(getApplicationContext());
        ImageButton googleAuth = findViewById(R.id.googleRegisterButton);
        googleAuth.setOnClickListener(v -> requestGoogleIdToken());

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(task -> {
            Intent newIntent = new Intent(this, RegisterActivity.class);
            startActivity(newIntent);
        });


        // Initialize Views
        editTextUsername = findViewById(R.id.usernameTextLogin);
        editTextPassword = findViewById(R.id.passwordTextLogin);
        buttonLogin = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        textViewError = findViewById(R.id.errorTextView);

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observe LiveData from ViewModel
        setupObservers();

        // Set up button click listener
        setupButtonClickListeners();

    }

    // show the respond
    private void setupObservers() {
        loginViewModel.getLoginUiState().observe(this, loginUiState -> {
            // Handle UI changes based on the state
            switch (loginUiState.getStatus()) {
                case IDLE:
                    progressBar.setVisibility(View.GONE);
                    buttonLogin.setEnabled(true);
                    textViewError.setVisibility(View.GONE);
                    break;
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    buttonLogin.setEnabled(false);
                    textViewError.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    buttonLogin.setEnabled(true);
                    textViewError.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Successful! Data: " + loginUiState.getData(), Toast.LENGTH_LONG).show();
                    // TODO: Navigate to the next screen and create it
                     Intent intent = new Intent(LoginActivity.this, HomeActivity.class); // move to home page
                     startActivity(intent);
                     finish(); // Optional: finish LoginActivity so user can't go back
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    buttonLogin.setEnabled(true);
                    textViewError.setText(loginUiState.getErrorMessage());
                    textViewError.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setupButtonClickListeners() {
        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Basic validation
            if (username.isEmpty()) {
                editTextUsername.setError("Username cannot be empty");
                return;
            }
            if (password.isEmpty()) {
                editTextPassword.setError("Password cannot be empty");
                return;
            }

            // Call the ViewModel method
            loginViewModel.loginClient(username, password);
        });
    }

    public void saveUserProfileToFirestore(String userId, String username, String email, String profilePictureUrl) {}

    private void testInsertion()
    {
        ClientRepository clientRepository = new ClientRepository();
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("2","Rosh Ha'Ayin"), "Haim Hertzog");
        clientRepository.insertClient("shakedm100", "1234", phone, "shaked1mi@gmail.com", "Shaked",
                "Michael", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername()));
    }

    private void checkIfUserAndPassword()
    {
        EditText userText = findViewById(R.id.usernameTextLogin);
        String username = userText.getText().toString();
        EditText passwordText = findViewById(R.id.passwordTextLogin);
        String password = passwordText.getText().toString();

        ClientRepository repository = new ClientRepository();
        Client current;
        repository.checkLogin(username, password).addOnSuccessListener(client ->
        {
            // Change Activity to Main
            if(client != null)
            {
                startActivity(new Intent(this, MainActivity.class));
            }
        })
            .addOnFailureListener(e -> {
            // Login failed (bad credentials or Firestore error)
                System.out.println(e.toString());
            // TODO: Add error handling
        });
    }


    private void requestGoogleIdToken()
    {

        // Build the Google ID request
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                // show _all_ Google accounts on the device, not just pre-authorized ones:
                .setFilterByAuthorizedAccounts(false)
                // still requires a server client ID so you get back an ID token you can verify:
                .setServerClientId(getString(R.string.server_client_id))
                // keep this false so the user always gets the choice dialog
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // CORRECTED: only one generic parameter here
        credentialManager.getCredentialAsync(
                this,
                request,
                /* cancellationSignal= */ null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse,GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse response) {
                        handleCredential(response.getCredential());
                    }
                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Sign-in error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void handleCredential(@NonNull Credential credential) {
        // 7) Check for Google ID token credential type
        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential gidc = (GoogleIdTokenCredential) credential;
            // 8) Parse and validate
            gidc = GoogleIdTokenCredential.createFrom(gidc.getData());
            String idToken = gidc.getIdToken();

            // 9) Send the token to your backend via repository
            new Thread(() -> {
                try {
                    //AuthResponse auth = userRepository.authenticateWithGoogle(idToken);
                    runOnUiThread(() -> {
                        // TODO: store auth.getJwt(), nav to main app screen
                        Toast.makeText(this,
                                "Welcome, " ,
                                Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "Auth error: " + ex.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                }
            }).start();

        } else {
            // not a Google ID credential
            Toast.makeText(this,
                    "Unexpected credential type: " + credential.getType(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
