package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// If you created LoginUiState.java:

import Model.Repository.ClientRepository; // Assuming this is your repository

public class LoginViewModel extends ViewModel {

    // Generic

    private final ClientRepository clientRepository;
    // T is String here, representing a success token or message
    private final MutableLiveData<GenericUiState<String>> _loginUiState =
            new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> loginUiState = _loginUiState;

//    public LoginViewModel(ClientRepository clientRepository) {
//        this.clientRepository = clientRepository;
//    }

    public LoginViewModel() {
        this.clientRepository = new ClientRepository();
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