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
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.BusinessRepository;
import Model.Repository.GeneralRepository;


public class RegisterBusinessViewModel extends ViewModel
{
    // create essential repositories and UI feedback to the client variables
    private final BusinessRepository businessRepository;
    private final GeneralRepository generalRepository;
    private final MutableLiveData<GenericUiState<String>> registerUiState = new MutableLiveData<>(GenericUiState.idle());
    private TaskCompletionSource<Boolean> taskCompletionSource;

    /**
     * Constructor for the RegisterBusinessViewModel.
     */
    public RegisterBusinessViewModel()
    {
        businessRepository = new BusinessRepository();
        generalRepository = new GeneralRepository();
    }

    /**
     * Returns all the available phone prefixes.
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

    public boolean checkArguments(String username, String password, String phonePrefixStr, String phoneNumber,
                                  String email, String businessName, City city, String addressStr, String policy, Context context)
    {
        // basic validation checks
        if (username == null || username.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please enter a username."));
            return false;
        }

        if (password == null || password.isEmpty() || password.length() < 6)
        {
            registerUiState.postValue(GenericUiState.error("Password must be at least 6 characters."));
            return false;
        }

        if (businessName == null || businessName.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please enter the name of the business."));
            return false;
        }

        if (email == null || email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
        {
            registerUiState.postValue(GenericUiState.error("Please enter a valid email address."));
            return false;
        }

        // check if the username or email are already taken by other user
        generalRepository.canRegisterUser("businesses", username, email)
                .addOnSuccessListener(exists -> {
                    if (exists) {
                        registerUiState.postValue(GenericUiState.error("Username or Email are already taken!"));
                    }
                })
                .addOnFailureListener(e -> {
                    registerUiState.postValue(GenericUiState.error("Something went wrong!"));
                });

        if (phonePrefixStr.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please select a phone prefix."));
            return false;
        }

        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
        if (prefix == null)
        {
            registerUiState.postValue(GenericUiState.error("Invalid phone prefix selected."));
            return false;
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty() || !phoneNumber.trim().matches("\\d+"))
        {
            registerUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return false;
        }
        if (!phoneNumber.trim().matches("[0-9]+"))
        {
            registerUiState.postValue(GenericUiState.error("Phone number must contain only numbers."));
            return false;
        }

        if (phoneNumber.trim().length() != 7)
        {
            registerUiState.postValue(GenericUiState.error("Phone number must have 7 digits."));
            return false;
        }

        if (city == null)
        {
            registerUiState.postValue(GenericUiState.error("City is null."));
            return false;
        }

        if (addressStr == null || addressStr.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please enter a street address."));
            return false;
        }

        // address is valid -> create instance
        Address address = new Address(city, addressStr);
        Geocoder geocoder = new Geocoder(context, new Locale("en", "IL"));

        // parse address to location and try to create a valid location
        Location location = generalRepository.convertAddressToLocation2(geocoder, address);

        // failed to get the long/lat of the given address
        if (location == null)
        {
            registerUiState.postValue(GenericUiState.error("Invalid address."));
            return false;
        }

        if (policy == null || policy.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please declare the policy."));
            return false;
        }

        return true;
    }

    /**
     * Attempts to register a new business if all the given arguments are valid.
     * @param context the context of the application
     * @param username the username of the business user
     * @param password the password of the business user
     * @param phonePrefixStr the phone prefix of the business
     * @param phoneNumber the phone number of the business
     * @param email the email of the business
     * @param businessName the name of the business
     * @param city the city of the business
     * @param addressStr the street address of the business
     * @param policy the policy of the business
     * @return an asynchronous task that returns true if the insertion was successful, false otherwise
     */
    public Task<Boolean> registerBusiness(Context context, String username, String password, String phonePrefixStr, String phoneNumber,
                                          String email, String businessName, City city, String addressStr, String policy)
    {
        taskCompletionSource = new TaskCompletionSource<>();

        // all fields are valid -> try to insert
        if (!checkArguments(username, password, phonePrefixStr, phoneNumber, email, businessName, city, addressStr, policy, context))
            taskCompletionSource.setResult(false); // at least one argument is not valid -> can not insert

        else
        {
            // phone is valid -> create instance
            PhonePrefix phonePrefix = PhonePrefix.fromString(phonePrefixStr.trim());
            Phone phone = new Phone(phonePrefix, phoneNumber);

            // address is valid -> create instance
            Address address = new Address(city, addressStr);
            Geocoder geocoder = new Geocoder(context, new Locale("en", "IL"));

            // parse address to location and try to create a valid location
            Location location = generalRepository.convertAddressToLocation(geocoder, address);

            businessRepository.insertBusiness(username, password, phone, email, businessName, location, policy, address)
                    .addOnSuccessListener(business ->
                    {
                        registerUiState.postValue(GenericUiState.success("Business registered successfully!"));
                        taskCompletionSource.setResult(true);
                    })
                    .addOnFailureListener(insertException ->
                    {
                        registerUiState.postValue(GenericUiState.error("Registration failed: " + insertException.getMessage()));
                        taskCompletionSource.setResult(false);
                    });
        }

        return taskCompletionSource.getTask();
    }

    /**
     * Get the status of the current UI state.
     * @return the current state of the UI.
     */
    public LiveData<GenericUiState<String>> getRegisterUiState()
    {
        return registerUiState;
    }
}
