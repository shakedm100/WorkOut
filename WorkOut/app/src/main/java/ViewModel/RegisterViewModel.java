package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import Model.Repository.ClientRepository; // Assuming this handles user creation
import Model.Address;
import Model.City;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;

import Model.Repository.ClientRepository;
// Import other necessary models like User, Task, etc.

public class RegisterViewModel extends ViewModel {

    private final Model.Repository.ClientRepository clientRepository;
    private final MutableLiveData<RegisterUiState> _registerUiState = new MutableLiveData<>(RegisterUiState.idle());
    public LiveData<RegisterUiState> registerUiState = _registerUiState;

    public RegisterViewModel(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void registerUser(String email, String password, String username, String firstName, String lastName,
                             String phonePrefixStr, String phoneNumberStr, String cityName, String street) {

        _registerUiState.postValue(RegisterUiState.loading());

        // Basic validation (can be expanded)
        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            _registerUiState.postValue(RegisterUiState.error("Please fill all required fields."));
            return;
        }

        // Convert string inputs to model objects
        // Add error handling for parsing, e.g., if PhonePrefix is invalid
        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr); // You'll need to implement fromString or similar in PhonePrefix
        Phone phone = new Phone(prefix, phoneNumberStr);
        City city = new City(null, cityName); // Assuming city might not have an ID initially
        com.google.android.engage.common.datamodel.Address address = new com.google.android.engage.common.datamodel.Address(city, street);

        // Call your repository method to perform the registration
        // The parameters for insertClient might need adjustment based on your actual method signature
        clientRepository.insertClient(username, password, phone, email, firstName, lastName, address, Gender.Male /* TODO: Get gender from UI */)
                .addOnSuccessListener(client -> { // Assuming 'client' is the created user or a success indicator
                    _registerUiState.postValue(RegisterUiState.success("Registration successful! Client ID: " + client.getId()));
                })
                .addOnFailureListener(e -> {
                    _registerUiState.postValue(RegisterUiState.error("Registration failed: " + e.getMessage()));
                });
    }

    public LiveData<RegisterUiState> getRegisterUiState() {
        return registerUiState;
    }
}