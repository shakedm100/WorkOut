package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

import Model.Repository.ClientRepository; // Assuming this handles user creation
import Model.Address;
import Model.City;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;

import Model.Repository.ClientRepository;
import Model.Repository.GeneralRepository;

public class RegisterViewModel extends ViewModel
{

    private final Model.Repository.ClientRepository clientRepository;

    private final GeneralRepository generalRepository;

    private final MutableLiveData<GenericUiState<String>> _registerUiState = new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> registerUiState = _registerUiState;

//    public RegisterViewModel(ClientRepository clientRepository, GeneralRepository generalRepository) {
//        this.clientRepository = clientRepository;
//        this.generalRepository = generalRepository;
//    }

    public RegisterViewModel()
    {
        this.clientRepository = new ClientRepository();
        this.generalRepository = new GeneralRepository();
    }

    public void registerUser(String email, String password, String username, String firstName, String lastName,
                             String phonePrefixStr, String phoneNumberStr, String birthday, String cityName, String street, Gender gender)
    {

        _registerUiState.postValue(GenericUiState.loading("Validating input..."));

        // validation
        if (username == null || username.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a username."));
            return;
        }

        if (password == null || password.isEmpty() || password.length() < 6)
        { // Example: password length check
            _registerUiState.postValue(GenericUiState.error("Password must be at least 6 characters."));
            return;
        }

        if (firstName == null || firstName.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a first name."));
            return;
        }
        if (lastName == null || lastName.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a last name."));
            return;
        }

        if (email == null || email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a valid email address."));
            return;
        }

        if (phonePrefixStr == null || phonePrefixStr.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please select a phone prefix."));
            return;
        }

        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
        if (prefix == null)
        {
            _registerUiState.postValue(GenericUiState.error("Invalid phone prefix selected."));
            return;
        }

        if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty() || !phoneNumberStr.trim().matches("\\d+"))
        { // Basic check for digits
            _registerUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return;
        }

        if (birthday == null || birthday.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a birthday."));
            return;
        }

        if (!birthday.matches("\\d{2}-\\d{2}-\\d{4}"))
        {
            _registerUiState.postValue(GenericUiState.error("Please enter birthday with the following template: DD-MM-YYYY"));
            return;
        }

        if (!birthday.matches("\\d{2}/\\d{2}/\\d{4}"))
        {
            String[] parts = birthday.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            // check validation
            if (day <= 0 || month <= 0 || year <= 0)
            {
                _registerUiState.postValue(GenericUiState.error("All the values must be positive!"));
                return;
            }

            if (day > 31 || month > 12 || year > 2025 || year < 1900)
            {
                _registerUiState.postValue(GenericUiState.error("Day must be between 1 and 31, month between 1 and 12, and year between 1900 and 2025!"));
                return;
            }

            if (month == 2 || month == 4 || month == 6 || month == 9 || month == 11)
            {
                if (year%4 == 0)
                {
                    if (day > 29)
                    {
                        _registerUiState.postValue(GenericUiState.error("Day can be up to 29!"));
                        return;
                    }

                    else if (day > 28)
                    {
                        _registerUiState.postValue(GenericUiState.error("Day can be up to 28!"));
                        return;
                    }
                }

                if (day > 30)
                {
                    _registerUiState.postValue(GenericUiState.error("Day can be up to 30!"));
                    return;
                }
            }
        }

        if (cityName == null || cityName.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a city name."));
            return;
        }
        if (street == null || street.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a street address."));
            return;
        }
        if (gender == null)
        {
            _registerUiState.postValue(GenericUiState.error("Please select a gender."));
            return;
        }

        // Trim inputs after validation
        final String finalEmail = email.trim();
        final String finalUsername = username.trim();
        final String finalFirstName = firstName.trim();
        final String finalLastName = lastName.trim();
        final String finalPhoneNumber = phoneNumberStr.trim();
        final String finalCityName = cityName.trim();
        final String finalStreet = street.trim();

        _registerUiState.postValue(GenericUiState.loading("Verifying city information..."));

        // Asynchronous City Fetching
        generalRepository.getCityByNamePartially(finalCityName)
                .addOnSuccessListener(cityObject ->
                {
                    if (cityObject == null || cityObject.get(0).getId() == null || cityObject.get(0).getId().trim().isEmpty())
                    {
                        _registerUiState.postValue(GenericUiState.error("City '" + finalCityName + "' not found or is invalid. Please use a valid city."));
                        return;
                    }

                    // valid city -> register
                    _registerUiState.postValue(GenericUiState.loading("Finalizing registration..."));

                    Phone phone = new Phone(prefix, finalPhoneNumber);
                    Address address = new Address(cityObject.get(0), finalStreet); // Use the fetched cityObject

                    clientRepository.insertClient(finalUsername, password, phone, finalEmail, finalFirstName, finalLastName, address, gender)
                            .addOnSuccessListener(result ->
                            { // 'result' could be Void, DocumentReference, or Client
                                // String successMessage = "Registration successful!";
                                // if (result instanceof Client) {
                                //    successMessage = "Registration successful! Client ID: " + ((Client) result).getId();
                                // } else if (result instanceof com.google.firebase.firestore.DocumentReference) {
                                //    successMessage = "Registration successful! Client ID: " + ((com.google.firebase.firestore.DocumentReference) result).getId();
                                // }
                                _registerUiState.postValue(GenericUiState.success("Registration successful! Welcome " + finalFirstName));
                            })
                            .addOnFailureListener(insertException ->
                            {
                                _registerUiState.postValue(GenericUiState.error("Registration failed: " + insertException.getMessage()));
                            });
                })
                .addOnFailureListener(cityFetchException ->
                {
                    _registerUiState.postValue(GenericUiState.error("Could not verify city: " + cityFetchException.getMessage()));
                });
    }

    public LiveData<GenericUiState<String>> getRegisterUiState()
    {
        return registerUiState;
    }

    // Optional: Method to reset the state if needed from the Activity/Fragment
    public void resetRegisterState()
    {
        _registerUiState.postValue(GenericUiState.idle());
    }

}