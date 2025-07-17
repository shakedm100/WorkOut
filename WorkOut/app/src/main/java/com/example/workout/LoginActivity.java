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
import ViewModel.LoginViewModel;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

// Base class
import androidx.credentials.CustomCredential; // The wrapper

import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;

import com.google.android.gms.auth.api.identity.AuthorizationClient;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity
{
    private @Nullable String pendingServerAuthCode = null;
    private @Nullable String pendingOAuthAccessTokenForPeopleApi = null;
    private @Nullable String pendingGoogleIdTokenForFirebase = null;

    private static final String TAG = "LoginActivity";
    // request code for the OAuth consent screen
    private static final int REQUEST_AUTHORIZE = 2001;
    private CredentialManager credentialManager;
    private FirebaseAuth auth;
    private LoginViewModel loginViewModel;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private ProgressBar progressBar;
    private TextView loginStatusTextView;
    private MaterialButtonToggleGroup toggleGroup;
    private boolean isRegisterClient;
    private Button loginButton;
    ImageButton googleSignInButton;
    Button registerButton;

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
        ImageButton googleSignInButton = findViewById(R.id.googleRegisterButton);
        googleSignInButton.setOnClickListener(v -> startAuthorizationFlow());
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
    private void setupObservers()
    {
        loginViewModel.getLoginUiState().observe(this, loginUiState ->
        {
            // Handle UI changes based on the state
            switch (loginUiState.getStatus())
            {
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
                    if (loginUiState.getData() instanceof Client)
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
            if (isChecked) // Shouldn't be possible
                return;

            if (checkedId != R.id.toggle_client)
                isRegisterClient = true;
            else
                isRegisterClient = false;
        });

        registerButton.setOnClickListener(task ->
        {
            Intent intent;
            if (isRegisterClient)
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

    private final ActivityResultLauncher<IntentSenderRequest> authorizeLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    this::onAuthorizeResult
            );

    private void enableLoginButtons(boolean enable)
    {
        if (loginButton != null) loginButton.setEnabled(enable);
        if (googleSignInButton != null) googleSignInButton.setEnabled(enable);
        if (registerButton != null) registerButton.setEnabled(enable);
    }

    /**
     * STEP 1: Launch the Google‐Identity OAuth2 consent for People API scopes.
     */
    private void startAuthorizationFlow()
    {
        // Build OAuth2 consent request:
        List<Scope> requestedScopes = Arrays.asList(
                new Scope(Scopes.OPEN_ID),                                  // Standard OpenID scope
                new Scope(Scopes.PROFILE),                                  // Standard OpenID scope
                new Scope(Scopes.EMAIL),                                    // Email address
                new Scope("https://www.googleapis.com/auth/user.addresses.read"), // Address access
                new Scope("https://www.googleapis.com/auth/user.phonenumbers.read"), // Phone number access
                new Scope("https://www.googleapis.com/auth/user.gender.read"), // Gender access
                new Scope("https://www.googleapis.com/auth/user.birthday.read") // Also check if you intend to use birthday
        );

        AuthorizationRequest authorizationRequest = new AuthorizationRequest.Builder()
                .requestOfflineAccess(getString(R.string.server_client_id))
                .setRequestedScopes(requestedScopes)
                .build();

        AuthorizationClient authClient = Identity.getAuthorizationClient(this);

        // launch the consent UI (if needed):
        authClient.authorize(authorizationRequest)
                .addOnSuccessListener(authResult ->
                {
                    if (authResult.hasResolution())
                    {
                        // User consent is required. Launch the intent.
                        try
                        {
                            IntentSenderRequest intentSenderRequest =
                                    new IntentSenderRequest.Builder(authResult.getPendingIntent().getIntentSender()).build();
                            authorizeLauncher.launch(intentSenderRequest);
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG, "Error launching authorization intent sender", e);
                            Toast.makeText(this, "Error starting sign-in process.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            enableLoginButtons(true);
                        }
                    }
                    else
                    {
                        // Consent already granted or not needed.
                        Log.d(TAG, "Authorization successful without needing user resolution.");
                        handleAuthorizationResult(authResult);
                    }
                })
                .addOnFailureListener(e ->
                {
                    Log.e(TAG, "Authorization failed", e);
                    Toast.makeText(this, "Google Authorization Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    enableLoginButtons(true);
                });
    }


    /**
     * STEP 2: Handle the result from the authorization intent (consent screen).
     */
    private void onAuthorizeResult(ActivityResult result)
    {
        if (result.getResultCode() != RESULT_OK)
        {
            Log.w(TAG, "Authorization flow was cancelled or failed. Result code: " + result.getResultCode());
            Toast.makeText(this, "Google Sign-In consent required or failed.", Toast.LENGTH_LONG).show();
            cleanupPendingTokens();
            progressBar.setVisibility(View.GONE);
            enableLoginButtons(true);
            return;
        }
        Intent data = result.getData();
        if (data == null)
        {
            Log.e(TAG, "Authorization intent returned null data.");
            Toast.makeText(this, "Google Sign-In error: No data returned.", Toast.LENGTH_LONG).show();
            cleanupPendingTokens();
            progressBar.setVisibility(View.GONE);
            enableLoginButtons(true);
            return;
        }

        AuthorizationClient authClient = Identity.getAuthorizationClient(this);
        try
        {
            AuthorizationResult authResult = authClient.getAuthorizationResultFromIntent(data);
            handleAuthorizationResult(authResult);
        }
        catch (ApiException e)
        {
            Log.e(TAG, "Failed to get AuthorizationResult from intent", e);
            Toast.makeText(this, "Google Sign-In error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            cleanupPendingTokens();
            progressBar.setVisibility(View.GONE);
            enableLoginButtons(true);
        }
    }

    /**
     * STEP 3: Process the AuthorizationResult to get OAuth2 Access Token.
     * Then, initiate getting the Google ID Token for Firebase.
     */
    private void handleAuthorizationResult(@Nullable AuthorizationResult authResult)
    {
        if (authResult == null)
        {
            Log.e(TAG, "AuthorizationResult was null after successful authorization.");
            Toast.makeText(this, "Google Sign-In error: Authorization result is missing.", Toast.LENGTH_LONG).show();
            cleanupPendingTokens();
            progressBar.setVisibility(View.GONE);
            enableLoginButtons(true);
            return;
        }

        pendingOAuthAccessTokenForPeopleApi = authResult.getAccessToken();
        pendingServerAuthCode = authResult.getServerAuthCode(); // For your backend, if needed

        if (pendingOAuthAccessTokenForPeopleApi != null)
        {
            Log.d(TAG, "OAuth Access Token for People API received. Length: " + pendingOAuthAccessTokenForPeopleApi.length()
                    + (pendingServerAuthCode != null ? " | Server Auth Code also received." : " | No Server Auth Code."));
            // Now that we have the OAuth2 Access Token, request the Google ID token for Firebase
            requestGoogleIdToken();
        }
        else
        {
            Log.e(TAG, "OAuth Access Token for People API was null from AuthorizationResult.");
            Toast.makeText(this, "Failed to get access token for Google services.", Toast.LENGTH_LONG).show();
            cleanupPendingTokens();
            progressBar.setVisibility(View.GONE);
            enableLoginButtons(true);
        }
    }

    /**
     * STEP 4: Request Google ID Token using Credential Manager.
     */
    private void requestGoogleIdToken()
    {
        Log.d(TAG, "Requesting Google ID Token via CredentialManager...");
        // Generate a fresh nonce for replay protection
        byte[] nonceBytes = new byte[16];
        new SecureRandom().nextBytes(nonceBytes);
        // Using URL-safe Base64 encoding for the nonce is common
        String nonce = android.util.Base64.encodeToString(nonceBytes, android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING | android.util.Base64.NO_WRAP);


        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true) // Important: ensures user has completed the AuthorizationClient flow
                .setServerClientId(getString(R.string.server_client_id)) // Your Web client ID for ID token
                .setAutoSelectEnabled(true) // Optional: attempts to auto-select if one clear choice
                .setNonce(nonce) // Include the nonce
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null, // CancellationSignal, optional
                ContextCompat.getMainExecutor(this), // Executes on the main thread
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>()
                {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential(); // This is androidx.credentials.Credential

                        Log.d(TAG, "CredentialManager onResult. Raw Credential type: " + credential.getClass().getName());
                        Log.d(TAG, "Credential.getType() string: " + credential.getType());

                        // Check if the type string matches what we expect for Google ID Tokens
                        if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            // This is the key: Cast the generic CustomCredential to the specific one.
                            // The CredentialManager uses CustomCredential as a container for provider-specific credentials.
                            if (credential instanceof CustomCredential) {
                                CustomCredential customCredential = (CustomCredential) credential;
                                try {
                                    // Attempt to convert the CustomCredential to the specific GoogleIdTokenCredential
                                    // This static method is provided by the Google ID library for use with Credential Manager
                                    GoogleIdTokenCredential googleIdTokenCredential =
                                            GoogleIdTokenCredential.createFrom(customCredential.getData()); // Use .getData()

                                    pendingGoogleIdTokenForFirebase = googleIdTokenCredential.getIdToken();

                                    if (pendingGoogleIdTokenForFirebase != null) {
                                        Log.d(TAG, "Google ID Token for Firebase received via CustomCredential. Length: " + pendingGoogleIdTokenForFirebase.length());
                                        proceedWithFirebaseSignInIfReady();
                                    } else {
                                        Log.e(TAG, "Google ID Token from CustomCredential was null.");
                                        Toast.makeText(LoginActivity.this, "Failed to get Google ID for Firebase.", Toast.LENGTH_SHORT).show();
                                        cleanupPendingTokens();
                                        progressBar.setVisibility(View.GONE);
                                        enableLoginButtons(true);
                                    }
                                } catch (Exception e) { // Catch potential exceptions from createFrom or if data bundle is wrong
                                    Log.e(TAG, "Error creating GoogleIdTokenCredential from CustomCredential: " + e.getMessage(), e);
                                    Toast.makeText(LoginActivity.this, "Google Sign-In error: Could not process token.", Toast.LENGTH_SHORT).show();
                                    cleanupPendingTokens();
                                    progressBar.setVisibility(View.GONE);
                                    enableLoginButtons(true);
                                }
                            } else {
                                // This case should ideally not happen if getType() matched GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                // and the framework is working as expected (wrapping it in CustomCredential)
                                Log.e(TAG, "Credential type matched Google ID Token, but it's not a CustomCredential. Actual type: " + credential.getClass().getName());
                                Toast.makeText(LoginActivity.this, "Google Sign-In error: Unexpected credential structure.", Toast.LENGTH_SHORT).show();
                                cleanupPendingTokens();
                                progressBar.setVisibility(View.GONE);
                                enableLoginButtons(true);
                            }
                        } else {
                            Log.e(TAG, "CredentialManager returned an unexpected credential type. Type: " + credential.getType());
                            Toast.makeText(LoginActivity.this, "Google Sign-In: Unexpected credential type received.", Toast.LENGTH_SHORT).show();
                            cleanupPendingTokens();
                            progressBar.setVisibility(View.GONE);
                            enableLoginButtons(true);
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e)
                    {
                        // Handle errors from Credential Manager, e.g., user cancelled, no matching accounts
                        // Common error types:
                        // e instanceof GetCredentialCancellationException -> User cancelled
                        // e instanceof GetCredentialInterruptedException -> Interrupted
                        // e instanceof GetCredentialUnsupportedException -> Unsupported
                        // e instanceof NoCredentialException -> No credentials available
                        Log.e(TAG, "CredentialManager getCredentialAsync error: " + e.getMessage() + " - " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "Google Sign-In failed (CredMan): " + e.getMessage(), Toast.LENGTH_LONG).show();
                        cleanupPendingTokens();
                        progressBar.setVisibility(View.GONE);
                        enableLoginButtons(true);
                    }
                }
        );
    }

    /**
     * STEP 5: Both tokens (should be) available. Call the repository.
     */
    private void proceedWithFirebaseSignInIfReady()
    {
        if (pendingGoogleIdTokenForFirebase != null && pendingOAuthAccessTokenForPeopleApi != null)
        {
            Log.d(TAG, "Both tokens available! Calling ClientRepository.handleGoogleAuthWithFirebase.");
            Log.d(TAG, "ID Token (for Firebase) starts with: " + (pendingGoogleIdTokenForFirebase.length() > 10 ? pendingGoogleIdTokenForFirebase.substring(0, 10) : pendingGoogleIdTokenForFirebase));
            Log.d(TAG, "Access Token (for People API) starts with: " + (pendingOAuthAccessTokenForPeopleApi.length() > 10 ? pendingOAuthAccessTokenForPeopleApi.substring(0, 10) : pendingOAuthAccessTokenForPeopleApi));

            // Inform the ViewModel to handle this combined Google Sign-In logic
            // The ViewModel will then call the repository
            loginViewModel.handleGoogleSignInWithTokens(
                    pendingGoogleIdTokenForFirebase,
                    pendingOAuthAccessTokenForPeopleApi,
                    pendingServerAuthCode // Pass this if your repository/backend needs the server auth code
            );

            // Note: UI updates (progressBar, button enabling) should be driven by
            // the LiveData from the ViewModel after this call.
            // cleanupPendingTokens() will be called based on ViewModel's LiveData result (SUCCESS/ERROR).

        }
        else
        {
            // This case should ideally not be hit if logic is correct, but good for safety.
            if (pendingGoogleIdTokenForFirebase == null)
            {
                Log.w(TAG, "Proceed called, but Google ID Token is missing.");
            }
            if (pendingOAuthAccessTokenForPeopleApi == null)
            {
                Log.w(TAG, "Proceed called, but OAuth Access Token is missing.");
            }
            if (!isFinishing() && !isDestroyed())
            { // Check activity state before showing toast
                Toast.makeText(LoginActivity.this, "Google Sign-In incomplete, missing tokens.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                enableLoginButtons(true);
            }
            cleanupPendingTokens();
        }
    }

    /**
     * Utility method to reset pending token state.
     */
    private void cleanupPendingTokens()
    {
        Log.d(TAG, "Cleaning up pending tokens.");
        pendingGoogleIdTokenForFirebase = null;
        pendingOAuthAccessTokenForPeopleApi = null;
        pendingServerAuthCode = null;
    }
}
