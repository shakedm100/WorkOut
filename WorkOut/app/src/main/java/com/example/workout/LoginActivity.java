package com.example.workout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;

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

import android.util.Log;

public class LoginActivity extends AppCompatActivity {
    private CredentialManager credentialManager;
    private FirebaseAuth auth;

    // ViewModel vars
    private MyViewModel viewModel;
    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        credentialManager = CredentialManager.create(getApplicationContext());
        ImageButton googleAuth = findViewById(R.id.googleRegisterButton);
        googleAuth.setOnClickListener(v -> requestGoogleIdToken());

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> saveUserProfileToFirestore("1", "shakedm100", "shaked1@gmail.com", null));
        //registerButton.setOnClickListener(v -> fetchClientFromFirestore());

        /*
        // TESTS
        TestClientRepository testRepo = new TestClientRepository();

        // Test GET
        testRepo.testGetClientByUsername("dvir");

        // Test UPDATE
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "updated@example.com");
        testRepo.testUpdateClientByUsername("dvir", updates);

        // Test DELETE
        testRepo.testDeleteClientByUsername("john123");

         */

        // ViewModel changes

        // get the instance as the MyViewModel class with the functions
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // listen to new data
        viewModel.getTextData().observe(this, text -> textView.setText(text));

        // Update ViewModel data on button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.updateText("Hello from ViewModel!");
            }
        });

    }

    public void saveUserProfileToFirestore(String userId, String username, String email, String profilePictureUrl) {

        ClientRepository clientRepository = new ClientRepository();
        Client sessionClient = null;
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("1","Oranit"), "Hayarkon");
        clientRepository.insertClient("dvir", "1234", phone, email, "Dvir",
                "Bento", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername()));
    }

    /**
     * Fetch a client from the Firestore by username
     */
    public void fetchClientFromFirestore(String username)
    {
        ClientRepository clientRepository = new ClientRepository();
        clientRepository.getClientByUsername("dvir");
    }

    public void updateClientByUsername(String username)
    {
        ClientRepository clientRepository = new ClientRepository();

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "newemail@example.com");

        clientRepository.updateUserByUsername(username, updates)
                .addOnSuccessListener(success -> {
                    if (success) {
                        Log.d("Firestore", "User updated successfully");
                    } else {
                        Log.d("Firestore", "User not found");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Update failed", e));
    }

    public void deleteClientByUsername(String username)
    {
        ClientRepository clientRepository = new ClientRepository();

        clientRepository.deleteUserByUsername(username)
                .addOnSuccessListener(success -> {
                    if (success) {
                        Log.d("Firestore", "User deleted successfully");
                    } else {
                        Log.d("Firestore", "User not found");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Deletion failed", e));
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
