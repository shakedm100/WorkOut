package ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

import java.util.List;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import Model.Repository.GeneralRepository;

public class UpdateClientViewModel extends ViewModel
{

    private final ClientRepository clientRepository;

    private final GeneralRepository generalRepository;

    private final MutableLiveData<GenericUiState<String>> _updateUiState = new MutableLiveData<>(GenericUiState.idle());
    public LiveData<GenericUiState<String>> updateUiState = _updateUiState;

//    public RegisterViewModel(ClientRepository clientRepository, GeneralRepository generalRepository) {
//        this.clientRepository = clientRepository;
//        this.generalRepository = generalRepository;
//    }

    public UpdateClientViewModel()
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
    public void updateClient(String firstName, String lastName, String phonePrefixStr, String phoneNumberStr, City city, String street, Client client)
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

        // Trim inputs after validation
        final String finalFirstName = firstName.trim();
        final String finalLastName = lastName.trim();
        final String finalPhoneNumber = phoneNumberStr.trim();
        final String finalStreet = street.trim();

        _updateUiState.postValue(GenericUiState.loading("Verifying city information..."));

        Phone finalPhone = new Phone(prefix, finalPhoneNumber);
        Address finalAddress = new Address(city, finalStreet);

        // check if any change was made
        if (client.getFirstName().equals(finalFirstName) &&
                client.getLastName().equals(finalLastName) &&
                client.getPhone().equals(finalPhone) &&
                client.getAddress().equals(finalAddress))
        {
            _updateUiState.postValue(GenericUiState.error("No changes were made."));
            return;
        }

        // set the new client details after validation
        client.setFirstName(finalFirstName);
        client.setLastName(finalLastName);
        client.setPhone(finalPhone);
        client.setAddress(finalAddress);

        clientRepository.updateClientByID(client)
                .addOnSuccessListener(updateResult ->
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
}
