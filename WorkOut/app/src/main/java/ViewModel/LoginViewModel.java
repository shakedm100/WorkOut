package ViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

// If you created LoginUiState.java:

import Model.Business;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository; // Assuming this is your repository
import Model.User;

public class LoginViewModel extends ViewModel
{
    private static final String TAG = "LoginViewModel";
    // Generic

    private final ClientRepository clientRepository;
    private final BusinessRepository businessRepository;
    // T is String here, representing a success token or message
    private final MutableLiveData<GenericUiState<User>> _loginUiState =
            new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<User>> loginUiState = _loginUiState;

    //    public LoginViewModel(ClientRepository clientRepository) {
    //        this.clientRepository = clientRepository;
    //    }

    public LoginViewModel()
    {
        this.clientRepository = new ClientRepository();
        this.businessRepository = new BusinessRepository();
    }

    public void loginUser(String username, String password)
    {
        _loginUiState.postValue(GenericUiState.loading());

        clientRepository.checkLogin(username, password)
                .addOnSuccessListener(client ->
                { // Assuming 'client' gives you some success data
                    // String token = client.getToken(); // Example
                    // For login, the success data might be a user object, a session token, or just a confirmation.
                    // Let's assume it's a success message or token as a String.
                    _loginUiState.postValue(GenericUiState.success(client)); // Or pass token
                })
                .addOnFailureListener(e ->
                {
                    businessRepository.checkLogin(username, password)
                            .addOnSuccessListener(business ->
                            {
                                // Business login successful
                                // String sessionInfo = business.getSessionInfo(); // Example
                                _loginUiState.postValue(GenericUiState.success(business)); // Or pass token/business data
                            })
                            .addOnFailureListener(businessError ->
                            {
                                //TODO: Change the error message
                                _loginUiState.postValue(GenericUiState.error(e.getMessage()));
                            });
                });


    }

    // --- Google Sign-In with Tokens ---

    /**
     * Handles the Google Sign-In process after receiving the ID token (for Firebase)
     * and the OAuth2 Access Token (for Google APIs like People API).
     *
     * @param idTokenForFirebase      The Google ID Token for Firebase authentication.
     * @param accessTokenForPeopleApi The OAuth2 Access Token for calling Google APIs.
     * @param serverAuthCode          Optional: The server auth code if your backend needs to exchange it.
     */
    public void handleGoogleSignInWithTokens(
            String idTokenForFirebase,
            String accessTokenForPeopleApi,
            @Nullable String serverAuthCode) {

        _loginUiState.postValue(GenericUiState.loading());
        Log.d(TAG, "handleGoogleSignInWithTokens called.");
        Log.d(TAG, "ID Token (Firebase) starts with: " + (idTokenForFirebase.length() > 10 ? idTokenForFirebase.substring(0, 10) : idTokenForFirebase));
        Log.d(TAG, "Access Token (People API) starts with: " + (accessTokenForPeopleApi.length() > 10 ? accessTokenForPeopleApi.substring(0, 10) : accessTokenForPeopleApi));
        if (serverAuthCode != null) {
            Log.d(TAG, "Server Auth Code starts with: " + (serverAuthCode.length() > 10 ? serverAuthCode.substring(0, 10) : serverAuthCode));
        } else {
            Log.d(TAG, "Server Auth Code is null.");
        }


        // Call the repository method that handles Firebase auth and then People API
        clientRepository.handleGoogleAuthWithFirebase(idTokenForFirebase, accessTokenForPeopleApi, serverAuthCode)
                .addOnSuccessListener(client -> {
                    // Client object should be populated by the repository,
                    // including any first-login flags or details from People API.
                    if (client != null) {
                        Log.d(TAG, "Google Sign-In successful. Client: " + client.getUsername() + ", First Login: " + client.isFirstLogin());
                        _loginUiState.postValue(GenericUiState.success(client));
                    } else {
                        // This case should ideally be handled within the repository and result in a failure
                        Log.e(TAG, "ClientRepository returned null client after Google Sign-In success callback.");
                        _loginUiState.postValue(GenericUiState.error("Google Sign-In failed to retrieve client data."));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "handleGoogleAuthWithFirebase failed in repository: " + e.getMessage(), e);
                    _loginUiState.postValue(GenericUiState.error("Google Sign-In failed: " + e.getMessage()));
                });
    }


    public LiveData<GenericUiState<User>> getLoginUiState()
    {
        return loginUiState;
    }
}