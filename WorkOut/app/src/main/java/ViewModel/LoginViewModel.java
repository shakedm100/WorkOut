package ViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;

// If you created LoginUiState.java:

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import Model.Address;
import Model.Business;
import Model.City;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
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
    private final MutableLiveData<GenericUiState<User>> _loginUiState = new MutableLiveData<>(GenericUiState.idle());
    private TaskCompletionSource<Boolean> taskCompletionSource;

    public LoginViewModel()
    {
        this.clientRepository = new ClientRepository();
        this.businessRepository = new BusinessRepository();
    }

    public Task<Boolean> loginUser(String username, String password)
    {
        taskCompletionSource = new TaskCompletionSource<>();
        _loginUiState.postValue(GenericUiState.loading());

        // validation
        if (username.isEmpty())
        {
            _loginUiState.postValue(GenericUiState.error("Username can not be empty"));
            taskCompletionSource.setResult(false);
            return taskCompletionSource.getTask();
        }

        if (password.isEmpty())
        {
            _loginUiState.postValue(GenericUiState.error("Password can not be empty"));
            taskCompletionSource.setResult(false);
        }

        if (!taskCompletionSource.getTask().isComplete())
        {
            clientRepository.checkLogin(username, password)
                    .addOnSuccessListener(client ->
                    {
                        _loginUiState.postValue(GenericUiState.success(client));
                        taskCompletionSource.setResult(true);
                    })
                    .addOnFailureListener(e ->
                    {
                        businessRepository.checkLogin(username, password)
                                .addOnSuccessListener(business ->
                                {
                                    _loginUiState.postValue(GenericUiState.success(business));
                                    taskCompletionSource.setResult(true);
                                })
                                .addOnFailureListener(businessError ->
                                {
                                    _loginUiState.postValue(GenericUiState.error("No user was found"));
                                    taskCompletionSource.setResult(false);
                                });
                    });
        }

        return taskCompletionSource.getTask();
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
            @Nullable String serverAuthCode)
    {

        _loginUiState.postValue(GenericUiState.loading());
        Log.d(TAG, "handleGoogleSignInWithTokens called.");
        Log.d(TAG, "ID Token (Firebase) starts with: " + (idTokenForFirebase.length() > 10 ? idTokenForFirebase.substring(0, 10) : idTokenForFirebase));
        Log.d(TAG, "Access Token (People API) starts with: " + (accessTokenForPeopleApi.length() > 10 ? accessTokenForPeopleApi.substring(0, 10) : accessTokenForPeopleApi));
        if (serverAuthCode != null)
        {
            Log.d(TAG, "Server Auth Code starts with: " + (serverAuthCode.length() > 10 ? serverAuthCode.substring(0, 10) : serverAuthCode));
        }
        else
        {
            Log.d(TAG, "Server Auth Code is null.");
        }


        // Call the repository method that handles Firebase auth and then People API
        clientRepository.handleGoogleAuthWithFirebase(idTokenForFirebase, accessTokenForPeopleApi, serverAuthCode)
                .addOnSuccessListener(client ->
                {
                    // Client object should be populated by the repository,
                    // including any first-login flags or details from People API.
                    if (client != null)
                    {
                        // Google's API can be unreliable with retrieving more then basic data
                        // Removing all null options to avoid null exception
                        if(client.getFirstName() == null)
                            client.setFirstName("");
                        if(client.getLastName() == null)
                            client.setLastName("");
                        if(client.getGender() == null)
                            client.setGender(Gender.Male);
                        if(client.getPhone() == null)
                        {
                            PhonePrefix prefix = PhonePrefix.inferPreFix("054");
                            Phone phone = new Phone(prefix, "0000000");
                            client.setPhone(phone);
                        }
                        if(client.getAddress() == null)
                        {
                            Address address = new Address(new City("No available city"), "");
                            client.setAddress(address);
                        }

                        Log.d(TAG, "Google Sign-In successful. Client: " + client.getUsername() + ", First Login: " + client.isFirstLogin());
                        _loginUiState.postValue(GenericUiState.success(client));
                    }
                    else
                    {
                        // This case should ideally be handled within the repository and result in a failure
                        Log.e(TAG, "ClientRepository returned null client after Google Sign-In success callback.");
                        _loginUiState.postValue(GenericUiState.error("Google Sign-In failed to retrieve client data."));
                    }
                })
                .addOnFailureListener(e ->
                {
                    Log.e(TAG, "handleGoogleAuthWithFirebase failed in repository: " + e.getMessage(), e);
                    _loginUiState.postValue(GenericUiState.error("Google Sign-In failed: " + e.getMessage()));
                });
    }


    public LiveData<GenericUiState<User>> getLoginUiState()
    {
        return _loginUiState;
    }
}