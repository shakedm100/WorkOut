package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

import java.util.List;

import Model.Address;
import Model.City;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import Model.Repository.GeneralRepository;

public class UpdateClientViewModel extends ViewModel
{

    private final Model.Repository.ClientRepository clientRepository;

    private final GeneralRepository generalRepository;

    private final MutableLiveData<GenericUiState<String>> _updateUiState = new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> updateUiState = _updateUiState;

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

        _updateUiState.postValue(GenericUiState.loading("Validating input..."));

        // basic validation checks
        if (firstName == null || firstName.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a first name."));
            return;
        }

        boolean isNameValid = firstName.chars().allMatch(Character::isLetter);
        if (!isNameValid)
        {
            _updateUiState.postValue(GenericUiState.error("First name must contain only letters."));
            return;
        }

        if (lastName == null || lastName.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a last name."));
            return;
        }

        isNameValid = lastName.chars().allMatch(Character::isLetter);
        if (!isNameValid)
        {
            _updateUiState.postValue(GenericUiState.error("Last name must contain only letters."));
            return;
        }

        if (email == null || email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a valid email address."));
            return;
        }

        // check if the username or email are already taken by other user
        generalRepository.canRegisterUser("clients", username, email)
                .addOnSuccessListener(exists -> {
                    if (exists) {
                        _updateUiState.postValue(GenericUiState.error("Username or Email are already taken!"));
                    }
                })
                .addOnFailureListener(e -> {
                    _updateUiState.postValue(GenericUiState.error("Something went wrong!"));
                });

        if (phonePrefixStr == null || phonePrefixStr.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please select a phone prefix."));
            return;
        }

        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
        if (prefix == null)
        {
            _updateUiState.postValue(GenericUiState.error("Invalid phone prefix selected."));
            return;
        }

        if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty() || !phoneNumberStr.trim().matches("\\d+"))
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return;
        }
        if (!phoneNumberStr.trim().matches("[0-9]+"))
        {
            _updateUiState.postValue(GenericUiState.error("Phone number must contain only numbers."));
            return;
        }

        if (phoneNumberStr.trim().length() != 7)
        {
            _updateUiState.postValue(GenericUiState.error("Phone number must have 7 digits."));
            return;
        }

        if (city == null)
        {
            _updateUiState.postValue(GenericUiState.error("City is null."));
            return;
        }
        if (street == null || street.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a street address."));
            return;
        }
        if (gender == null)
        {
            _updateUiState.postValue(GenericUiState.error("Please select a gender."));
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

        _updateUiState.postValue(GenericUiState.loading("Verifying city information..."));

        Phone phone = new Phone(prefix, finalPhoneNumber);
        Address address = new Address(city, finalStreet);

        // TODO: CHANGE TO UPDATE!!!
        clientRepository.insertClient(finalUsername, password, phone, finalEmail, finalFirstName, finalLastName, address, gender)
                .addOnSuccessListener(regResult ->
                {
                    _updateUiState.postValue(GenericUiState.success("Update successful!" + finalFirstName));
                })
                .addOnFailureListener(updateException ->
                {
                    _updateUiState.postValue(GenericUiState.error("Update failed: " + updateException.getMessage()));
                });
    }

    public LiveData<GenericUiState<String>> getUpdateUiState()
    {
        return updateUiState;
    }

    // Optional: Method to reset the state if needed from the Activity/Fragment
    public void resetUpdateState()
    {
        _updateUiState.postValue(GenericUiState.idle());
    }
}
