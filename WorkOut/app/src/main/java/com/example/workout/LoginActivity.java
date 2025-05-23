package com.example.workout;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;

public class LoginActivity extends AppCompatActivity {
    private CredentialManager credentialManager;
    private FirebaseAuth auth;
    //Bla
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(click ->
        {
            checkIfUserAndPassword();
        });
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


    private void requestGoogleIdToken() {
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
