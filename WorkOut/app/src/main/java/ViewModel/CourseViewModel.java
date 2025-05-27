package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import Model.Repository.ClientRepository;

// TODO: change to the correct repository and add the desired functions
public class CourseViewModel extends ViewModel {

    private final ClientRepository clientRepository; // CHANGE

    private final MutableLiveData<CourseUiState> _courseUiState = new MutableLiveData<>(CourseUiState.idle());
    public LiveData<CourseUiState> loginUiState = _courseUiState;

    public CourseViewModel() {
        // It's better to inject the repository via constructor
        this.clientRepository = new ClientRepository();
    }

    // Constructor for Dependency Injection
    public CourseViewModel(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void insertCourse(String username, String password) {
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
