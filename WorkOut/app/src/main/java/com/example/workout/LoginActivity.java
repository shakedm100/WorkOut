package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

import com.example.workout.Business.BusinessHomeActivity;
import com.example.workout.Business.BusinessRegisterActivity;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;

import Model.Client;
import Model.Repository.ClientRepository;
import ViewModel.LoginViewModel;
import ViewModel.RegisterBusinessViewModel;

public class LoginActivity extends AppCompatActivity {
    private CredentialManager credentialManager;
    private FirebaseAuth auth;
    private LoginViewModel loginViewModel;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private ProgressBar progressBar;
    private TextView loginStatusTextView;
    private MaterialButtonToggleGroup toggleGroup;
    private boolean isRegisterClient;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // initialize Views
        editTextUsername = findViewById(R.id.usernameTextLogin);
        editTextPassword = findViewById(R.id.passwordTextLogin);
        progressBar = findViewById(R.id.loginProgressBar);
        loginStatusTextView = findViewById(R.id.loginStatusTextView);
        credentialManager = CredentialManager.create(getApplicationContext());
        ImageButton googleAuth = findViewById(R.id.googleRegisterButton);
        googleAuth.setOnClickListener(v -> requestGoogleIdToken());
        registerButton = findViewById(R.id.registerButton);

        isRegisterClient = true;
        toggleGroup = findViewById(R.id.registerToggle);
        toggleGroup.check(R.id.toggle_client);
//        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) ->
//        {
//            if(isChecked) // Shouldn't be possible
//                return;
//
//            if(checkedId == R.id.toggle_client)
//                isRegisterClient = true;
//            else
//                isRegisterClient = false;
//        });
//
//        registerButton.setOnClickListener(task -> {
//            Intent intent;
//            if(isRegisterClient)
//                intent = new Intent(this, RegisterActivity.class);
//            else
//                intent = new Intent(this, BusinessRegisterActivity.class);
//            startActivity(intent);
//        });
//
//        Button registerBusinessButton = findViewById(R.id.registerButton);
//        // TODO: move to suitable function
//        registerBusinessButton.setOnClickListener(task -> {
//            Intent newIntent = new Intent(this, BusinessRegisterActivity.class);
//            startActivity(newIntent);
//        });

        loginButton = findViewById(R.id.loginButton);
        /*loginButton.setOnClickListener(click ->
        {
            checkIfUserAndPassword();
        });*/


        // initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // get LiveData from ViewModel
        setupObservers();

        // create button click listener
        setupButtonClickListeners();
    }

    // show the respond
    private void setupObservers() {
        loginViewModel.getLoginUiState().observe(this, loginUiState -> {
            // Handle UI changes based on the state
            switch (loginUiState.getStatus()) {
                case IDLE:
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    loginStatusTextView.setVisibility(View.GONE);
                    break;
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    loginButton.setEnabled(false);
                    loginStatusTextView.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    loginStatusTextView.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                    Intent intent;
                    if(loginUiState.getData() instanceof Client)
                    {
                        // navigate to home activity when success occurs
                        intent = new Intent(this, MainActivity.class); // move to home page
                        intent.putExtra("client", loginUiState.getData());
                    }
                    else
                    {
                        intent = new Intent(this, BusinessHomeActivity.class); // move to home page
                        intent.putExtra("business", loginUiState.getData());
                    }
                    startActivity(intent);
                    finish(); // Finish LoginActivity so user can't go back
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    loginStatusTextView.setText(loginUiState.getErrorMessage());
                    loginStatusTextView.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setupButtonClickListeners()
    {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) ->
        {
            if(isChecked) // Shouldn't be possible
                return;

            if(checkedId != R.id.toggle_client)
                isRegisterClient = true;
            else
                isRegisterClient = false;
        });

        registerButton.setOnClickListener(task -> {
            Intent intent;
            if(isRegisterClient)
                intent = new Intent(this, RegisterActivity.class);
            else
                intent = new Intent(this, BusinessRegisterActivity.class);

            startActivity(intent);
        });

        loginButton.setOnClickListener(v ->
        {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // validation
            if (username.isEmpty())
            {
                editTextUsername.setError("Username cannot be empty");
                return;
            }

            if (password.isEmpty())
            {
                editTextPassword.setError("Password cannot be empty");
                return;
            }

            // Call the ViewModel method
            loginViewModel.loginUser(username, password);
        });
    }

   /* private void checkIfUserAndPassword()
        {
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            ClientRepository repository = new ClientRepository();
            repository.checkLogin(username, password).addOnSuccessListener(client ->
            {
                // Change Activity to Main
                if(client != null)
                {
                    Intent intent = new Intent(this, MainActivity.class).putExtra("client", client);
                    startActivity(intent);
                }
            });

            BusinessRepository businessRepository = new BusinessRepository();
            businessRepository.checkLogin(username, password).addOnSuccessListener(business ->
            {
                if(business != null)
                {
                    Intent intent = new Intent(this, BusinessHomeActivity.class).putExtra("business", business);
                    startActivity(intent);
                }
            }).addOnFailureListener(e ->
            {
                //TODO: Handle error here, not a business nor a client!
            });
        }*/


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
        if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            GoogleIdTokenCredential gidc = GoogleIdTokenCredential.createFrom(credential.getData());
            // 8) Parse and validate
            gidc = GoogleIdTokenCredential.createFrom(gidc.getData());
            String idToken = gidc.getIdToken();

            ClientRepository clientRepository = new ClientRepository();
            clientRepository.handleGoogleAuthWithFirebase(idToken).addOnSuccessListener(task ->
            {
                Client client = task;
                startActivity(new Intent(this, MainActivity.class));
            }).addOnFailureListener(e ->
            {
                System.out.println(e);
            });
        }
    }
}
