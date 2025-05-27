package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// If you created LoginUiState.java:
import ViewModel.LoginUiState; // Replace with your actual package

import Model.Repository.ClientRepository; // Assuming this is your repository

public class LoginViewModel extends ViewModel {

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
}