package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// If you created LoginUiState.java:

import Model.Business;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository; // Assuming this is your repository
import Model.User;

public class LoginViewModel extends ViewModel
{

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

    public LiveData<GenericUiState<User>> getLoginUiState()
    {
        return loginUiState;
    }
}