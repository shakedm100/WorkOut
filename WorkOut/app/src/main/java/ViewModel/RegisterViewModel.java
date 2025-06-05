package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns all the available phone prefixes.
     * @return all the phone prefixes.
     */
    public List<String> getAllPhonePrefixes()
    {
        return generalRepository.getAllPhonePrefixes();
    }

    /**
     * Returns the best 3 matches of cities by a given string.
     * @param input the partial name (or full name) of the city.
     * @return a list of the best 3 cities matched.
     */
    public Task<List<City>> getBestMatchedCities(String input)
    {
        return generalRepository.getCityByNamePartially(input);
    }

    /**
     * Attempts to register a new User with the given parameters.
     */
    public void registerUser(String email, String password, String username, String firstName, String lastName,
                             String phonePrefixStr, String phoneNumberStr, String birthday, City city, String street, Gender gender) // String cityName
    {

        _registerUiState.postValue(GenericUiState.loading("Validating input..."));

        // basic validation checks
        if (username == null || username.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a username."));
            return;
        }

        if (password == null || password.isEmpty() || password.length() < 6)
        {
            _registerUiState.postValue(GenericUiState.error("Password must be at least 6 characters."));
            return;
        }

        if (firstName == null || firstName.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a first name."));
            return;
        }

        boolean isNameValid = firstName.chars().allMatch(Character::isLetter);
        if (!isNameValid)
        {
            _registerUiState.postValue(GenericUiState.error("First name must contain only letters."));
            return;
        }

        if (lastName == null || lastName.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a last name."));
            return;
        }

        isNameValid = lastName.chars().allMatch(Character::isLetter);
        if (!isNameValid)
        {
            _registerUiState.postValue(GenericUiState.error("Last name must contain only letters."));
            return;
        }

        if (email == null || email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a valid email address."));
            return;
        }

        // check if the username or email are already taken by other user
        generalRepository.canRegisterUser("clients", username, email)
                .addOnSuccessListener(exists -> {
                    if (exists) {
                        _registerUiState.postValue(GenericUiState.error("Username or Email are already taken!"));
                    }
                })
                .addOnFailureListener(e -> {
                    _registerUiState.postValue(GenericUiState.error("Something went wrong!"));
                });

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
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return;
        }
        if (!phoneNumberStr.trim().matches("[0-9]+"))
        {
            _registerUiState.postValue(GenericUiState.error("Phone number must contain only numbers."));
            return;
        }

        if (phoneNumberStr.trim().length() != 7)
        {
            _registerUiState.postValue(GenericUiState.error("Phone number must have 7 digits."));
            return;
        }

        if (birthday == null || birthday.trim().isEmpty())
        {
            _registerUiState.postValue(GenericUiState.error("Please enter a birthday."));
            return;
        }

        if (!birthday.matches("\\d{2}/\\d{2}/\\d{4}"))
        {
            _registerUiState.postValue(GenericUiState.error("Please enter birthday with the following template: DD/MM/YYYY"));
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

        if (city == null)
        {
            _registerUiState.postValue(GenericUiState.error("City is null."));
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
        //final String finalCityName = cityName.trim(); -> changed to object
        final String finalStreet = street.trim();

        _registerUiState.postValue(GenericUiState.loading("Verifying city information..."));

        Phone phone = new Phone(prefix, finalPhoneNumber);
        Address address = new Address(city, finalStreet);

        clientRepository.insertClient(finalUsername, password, phone, finalEmail, finalFirstName, finalLastName, address, gender)
                .addOnSuccessListener(regResult ->
                {
                    _registerUiState.postValue(GenericUiState.success("Registration successful! Welcome " + finalFirstName));
                })
                .addOnFailureListener(insertException ->
                {
                    _registerUiState.postValue(GenericUiState.error("Registration failed: " + insertException.getMessage()));
                });

        /*

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

         */
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