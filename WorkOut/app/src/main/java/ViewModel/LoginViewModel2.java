package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import Model.Repository.ClientRepository;

public class LoginViewModel2 extends ViewModel {

    private final ClientRepository clientRepository = new ClientRepository();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    public void loginClient(String username, String password) {
        clientRepository.checkLogin(username, password)
                .addOnSuccessListener(client -> {
                    loginSuccess.postValue(true); // success
                })
                .addOnFailureListener(e -> {
                    loginError.postValue(e.getMessage()); // notify the client
                    loginSuccess.postValue(false); // failed
                });
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }





}
