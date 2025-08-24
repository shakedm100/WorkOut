package ViewModel.Business;

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
import Model.Business;
import Model.City;
import Model.Client;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.BusinessRepository;
import Model.Repository.ClientRepository;
import Model.Repository.GeneralRepository;
import ViewModel.GenericUiState;

public class BusinessUpdateDetailsViewModel extends ViewModel
{
    private final MutableLiveData<GenericUiState<String>> updateUiState = new MutableLiveData<>(GenericUiState.idle());
    private TaskCompletionSource<Boolean> taskCompletionSource;
    private GeneralRepository generalRepository;
    private BusinessRepository businessRepository;

    public BusinessUpdateDetailsViewModel()
    {
        this.businessRepository = new BusinessRepository();
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

    public boolean checkArguments(String name, String phonePrefixStr, String phoneNumberStr, City city,
                                  String street, String policy, Business business)
    {
        // basic validation checks
        if (name == null || name.trim().isEmpty())
        {
            updateUiState.postValue(GenericUiState.error("Please enter a name for the business."));
            return false;
        }

        if (phonePrefixStr == null || phonePrefixStr.trim().isEmpty())
        {
            updateUiState.postValue(GenericUiState.error("Please select a phone prefix."));
            return false;
        }

        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
        if (prefix == null)
        {
            updateUiState.postValue(GenericUiState.error("Invalid phone prefix selected."));
            return false;
        }

        if (phoneNumberStr == null || phoneNumberStr.trim().isEmpty() || !phoneNumberStr.trim().matches("\\d+"))
        {
            updateUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return false;
        }
        if (!phoneNumberStr.trim().matches("[0-9]+"))
        {
            updateUiState.postValue(GenericUiState.error("Phone number must contain only numbers."));
            return false;
        }

        if (phoneNumberStr.trim().length() != 7)
        {
            updateUiState.postValue(GenericUiState.error("Phone number must have 7 digits."));
            return false;
        }

        if (city == null)
        {
            updateUiState.postValue(GenericUiState.error("City is null."));
            return false;
        }
        if (street == null || street.trim().isEmpty())
        {
            updateUiState.postValue(GenericUiState.error("Please enter a street address."));
            return false;
        }

        if (policy == null || policy.trim().isEmpty())
        {
            updateUiState.postValue(GenericUiState.error("Please enter a policy."));
            return false;
        }

        if (business == null)
        {
            updateUiState.postValue(GenericUiState.error("Business can not be null!"));
            return false;
        }

        return true;
    }
    /**
     * Attempts to register a new User with the given parameters.
     */
    public Task<Boolean> updateBusiness(Context context, String name,
                                        String phonePrefixStr, String phoneNumberStr, City city,
                                        String street, String policy, Business business)
    {
        taskCompletionSource = new TaskCompletionSource<>();
        updateUiState.postValue(GenericUiState.loading("Validating input..."));

        if (!checkArguments(name, phonePrefixStr, phoneNumberStr, city, street, policy, business))
            taskCompletionSource.setResult(false);

        else
        {
            // Trim inputs after validation
            PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
            final String finalName = name.trim();
            final String finalPhoneNumber = phoneNumberStr.trim();
            final String finalStreet = street.trim();
            final String finalPolicy = policy.trim();
            final Phone finalPhone = new Phone(prefix, finalPhoneNumber);
            final Address finalAddress = new Address(city, finalStreet);

            updateUiState.postValue(GenericUiState.loading("Verifying city information..."));

            // parse address to location and try to create a valid location
            Geocoder geocoder = new Geocoder(context, new Locale("en", "IL"));
            Location location = generalRepository.convertAddressToLocation2(geocoder, finalAddress);

            // failed to get the long/lat of the given address
            if (location == null)
            {
                updateUiState.postValue(GenericUiState.error("Invalid address."));
                taskCompletionSource.setResult(false);
                return taskCompletionSource.getTask();
            }

            // check if any changes were made
            if (business.getBusinessName().equals(finalName) &&
                    business.getPhone().equals(finalPhone) &&
                    business.getAddress().equals(finalAddress) &&
                    business.getPolicy().equals(finalPolicy))
            {
                updateUiState.postValue(GenericUiState.error("No changes were made."));
                taskCompletionSource.setResult(false);
            }

            if (!taskCompletionSource.getTask().isComplete())
            {
                // set the new client details after validation
                business.setBusinessName(finalName);
                business.setPhone(finalPhone);
                business.setAddress(finalAddress);
                business.setPolicy(finalPolicy);

                businessRepository.updateBusiness(business)
                        .addOnSuccessListener(updateResult ->
                        {
                            updateUiState.postValue(GenericUiState.success("Update successful!"));
                            taskCompletionSource.setResult(true);
                        })
                        .addOnFailureListener(updateException ->
                        {
                            updateUiState.postValue(GenericUiState.error("Update failed: " + updateException.getMessage()));
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
