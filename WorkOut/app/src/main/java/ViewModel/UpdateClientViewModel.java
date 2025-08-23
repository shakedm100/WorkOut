package ViewModel;

import android.content.Context;
import android.location.Geocoder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;
import java.util.Locale;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Location;
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
    private TaskCompletionSource<Boolean> taskCompletionSource;


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

    public boolean checkArguments(String firstName, String lastName, String phonePrefixStr, String phoneNumberStr, City city, String street, Client client)
    {
        // basic validation checks
        if (firstName == null || firstName.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a first name."));
            return false;
        }

        if (lastName == null || lastName.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a last name."));
            return false;
        }

        if (!generalRepository.allLetters(firstName))
        {
            _updateUiState.postValue(GenericUiState.error("First name must contain only letters."));
            return false;
        }

        if (!generalRepository.allLetters(lastName))
        {
            _updateUiState.postValue(GenericUiState.error("Last name must contain only letters."));
            return false;
        }

        if (phonePrefixStr == null || phonePrefixStr.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please select a phone prefix."));
            return false;
        }

        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
        if (prefix == null)
        {
            _updateUiState.postValue(GenericUiState.error("Invalid phone prefix selected."));
            return false;
        }

        if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty() || !phoneNumberStr.trim().matches("\\d+"))
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return false;
        }
        if (!phoneNumberStr.trim().matches("[0-9]+"))
        {
            _updateUiState.postValue(GenericUiState.error("Phone number must contain only numbers."));
            return false;
        }

        if (phoneNumberStr.trim().length() != 7)
        {
            _updateUiState.postValue(GenericUiState.error("Phone number must have 7 digits."));
            return false;
        }

        if (city == null)
        {
            _updateUiState.postValue(GenericUiState.error("City is null."));
            return false;
        }
        if (street == null || street.trim().isEmpty())
        {
            _updateUiState.postValue(GenericUiState.error("Please enter a street address."));
            return false;
        }

        if (client == null)
        {
            _updateUiState.postValue(GenericUiState.error("Client can not be null!"));
            return false;
        }

        return true;
    }
    /**
     * Attempts to register a new User with the given parameters.
     */
    public Task<Boolean> updateClient(Context context, String firstName, String lastName, String phonePrefixStr, String phoneNumberStr, City city, String street, Client client)
    {
        taskCompletionSource = new TaskCompletionSource<>();
        _updateUiState.postValue(GenericUiState.loading("Validating input..."));

        if (!checkArguments(firstName, lastName, phonePrefixStr, phoneNumberStr, city, street, client))
            taskCompletionSource.setResult(false);

        else
        {
            // Trim inputs after validation
            PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
            final String finalFirstName = firstName.trim();
            final String finalLastName = lastName.trim();
            final String finalPhoneNumber = phoneNumberStr.trim();
            final String finalStreet = street.trim();

            _updateUiState.postValue(GenericUiState.loading("Verifying city information..."));

            Phone finalPhone = new Phone(prefix, finalPhoneNumber);
            Address finalAddress = new Address(city, finalStreet);

            // parse address to location and try to create a valid location
            Geocoder geocoder = new Geocoder(context, new Locale("en", "IL"));
            Location location = generalRepository.convertAddressToLocation2(geocoder, finalAddress);

            // failed to get the long/lat of the given address
            if (location == null)
            {
                _updateUiState.postValue(GenericUiState.error("Invalid address."));
                taskCompletionSource.setResult(false);
                return taskCompletionSource.getTask();
            }

            // check if any changes were made
            if (client.getFirstName().equals(finalFirstName) &&
                    client.getLastName().equals(finalLastName) &&
                    client.getPhone().equals(finalPhone) &&
                    client.getAddress().equals(finalAddress))
            {
                _updateUiState.postValue(GenericUiState.error("No changes were made."));
                taskCompletionSource.setResult(false);
            }

            if (!taskCompletionSource.getTask().isComplete())
            {
                // set the new client details after validation
                client.setFirstName(finalFirstName);
                client.setLastName(finalLastName);
                client.setPhone(finalPhone);
                client.setAddress(finalAddress);

                clientRepository.updateClientByID(client)
                        .addOnSuccessListener(updateResult ->
                        {
                            _updateUiState.postValue(GenericUiState.success("Update successful!"));
                            taskCompletionSource.setResult(true);
                        })
                        .addOnFailureListener(updateException ->
                        {
                            _updateUiState.postValue(GenericUiState.error("Update failed: " + updateException.getMessage()));
                            taskCompletionSource.setResult(false);
                        });
            }
        }

        return taskCompletionSource.getTask();
    }

    /**
     * Get the status of the current UI state.
     * @return the current state of the UI.
     */
    public LiveData<GenericUiState<String>> getUpdateUiState()
    {
        return updateUiState;
    }
}
