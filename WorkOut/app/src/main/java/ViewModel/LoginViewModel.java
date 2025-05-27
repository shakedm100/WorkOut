package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// If you created LoginUiState.java:
import ViewModel.LoginUiState; // Replace with your actual package

import Model.Repository.ClientRepository; // Assuming this is your repository

public class LoginViewModel extends ViewModel {

    /* OLD - not generic
    private final ClientRepository clientRepository; // Inject this if possible

    // Using a single LiveData for UI State
    private final MutableLiveData<LoginUiState> _loginUiState = new MutableLiveData<>(LoginUiState.idle());
    public LiveData<LoginUiState> loginUiState = _loginUiState;

    public LoginViewModel() {
        // It's better to inject the repository via constructor
        this.clientRepository = new ClientRepository();
    }

    // Constructor for Dependency Injection
     public LoginViewModel(ClientRepository clientRepository) {
         this.clientRepository = clientRepository;
     }

    public void loginClient(String username, String password) {
        _loginUiState.postValue(LoginUiState.loading());

        clientRepository.checkLogin(username, password)
                .addOnSuccessListener(client -> { // Assuming 'client' is the success data, e.g., a user object or token
                    // You might want to extract a token or relevant data from 'client'
                    _loginUiState.postValue(LoginUiState.success("UserLoggedIn")); // maybe pass client.getToken()


                })
                .addOnFailureListener(e -> {
                    _loginUiState.postValue(LoginUiState.error(e.getMessage()));

                });
    }

    public LiveData<LoginUiState> getLoginUiState() {
        return loginUiState;
    }

     */

    // Generic

    private final ClientRepository clientRepository;
    // T is String here, representing a success token or message
    private final MutableLiveData<GenericUiState<String>> _loginUiState =
            new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> loginUiState = _loginUiState;

    public LoginViewModel(ClientRepository clientRepository) { // Assuming DI
        this.clientRepository = clientRepository;
    }

    public void loginClient(String username, String password) {
        _loginUiState.postValue(GenericUiState.loading());

        clientRepository.checkLogin(username, password)
                .addOnSuccessListener(client -> { // Assuming 'client' gives you some success data
                    // String token = client.getToken(); // Example
                    // For login, the success data might be a user object, a session token, or just a confirmation.
                    // Let's assume it's a success message or token as a String.
                    _loginUiState.postValue(GenericUiState.success("Login Successful")); // Or pass token
                })
                .addOnFailureListener(e -> {
                    _loginUiState.postValue(GenericUiState.error(e.getMessage()));
                });
    }

    public LiveData<GenericUiState<String>> getLoginUiState() {
        return loginUiState;
    }
}