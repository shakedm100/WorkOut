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

import com.google.android.gms.common.Scopes;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import Model.Address;
import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.City;
import Model.Client;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Gender;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Rating;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository;
import Model.Schedule;

public class LoginActivity extends AppCompatActivity {
    private CredentialManager credentialManager;
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
           // TODO: Add error handling, not a client and not a business!
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
