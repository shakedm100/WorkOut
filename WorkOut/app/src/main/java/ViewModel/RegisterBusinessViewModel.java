package ViewModel;

import android.content.Context;
import android.location.Geocoder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

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
    private BusinessRepository businessRepository;
    private GeneralRepository generalRepository;
    private final MutableLiveData<GenericUiState<String>> registerUiState = new MutableLiveData<>(GenericUiState.idle());

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

    /**
     * Inserts a new business if all the given parameters are valid.
     * The result of the registration will appear on the screen.
     */
    public void registerBusiness(Context context, String username, String password, String phonePrefixStr, String phoneNumber,
                                 String email, String businessName, City city, String addressStr, String policy)
    {
        // basic validation checks
        if (username == null || username.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please enter a username."));
            return;
        }

        if (password == null || password.isEmpty() || password.length() < 6)
        {
            registerUiState.postValue(GenericUiState.error("Password must be at least 6 characters."));
            return;
        }

        if (businessName == null || businessName.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please enter the name of the business."));
            return;
        }

        if (email == null || email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
        {
            registerUiState.postValue(GenericUiState.error("Please enter a valid email address."));
            return;
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
            return;
        }

        PhonePrefix prefix = PhonePrefix.fromString(phonePrefixStr.trim());
        if (prefix == null)
        {
            registerUiState.postValue(GenericUiState.error("Invalid phone prefix selected."));
            return;
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty() || !phoneNumber.trim().matches("\\d+"))
        {
            registerUiState.postValue(GenericUiState.error("Please enter a valid phone number."));
            return;
        }
        if (!phoneNumber.trim().matches("[0-9]+"))
        {
            registerUiState.postValue(GenericUiState.error("Phone number must contain only numbers."));
            return;
        }

        if (phoneNumber.trim().length() != 7)
        {
            registerUiState.postValue(GenericUiState.error("Phone number must have 7 digits."));
            return;
        }

        // phone is valid -> create instance
        PhonePrefix phonePrefix = PhonePrefix.fromString(phonePrefixStr.trim());
        Phone phone = new Phone(phonePrefix, phoneNumber);

        if (city == null)
        {
            registerUiState.postValue(GenericUiState.error("City is null."));
            return;
        }

        if (addressStr == null || addressStr.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please enter a street address."));
            return;
        }

        // address is valid -> create instance
        Address address = new Address(city, addressStr);
        Geocoder geocoder = new Geocoder(context, new Locale("en", "IL"));

        // parse address to location and try to create a valid location
        Location location = generalRepository.convertAddressToLocation(geocoder, address);

        // failed to get the long/lat of the given address
        if (location == null)
        {
            registerUiState.postValue(GenericUiState.error("Invalid address."));
            return;
        }

        if (policy == null || policy.trim().isEmpty())
        {
            registerUiState.postValue(GenericUiState.error("Please declare the policy."));
            return;
        }

        // all fields are valid -> try to insert
        businessRepository.insertBusiness(username, password, phone, email, businessName, location, policy, address)
                .addOnSuccessListener(business ->
                {
                    registerUiState.postValue(GenericUiState.success("Register success!"));
                })
                .addOnFailureListener(insertException ->
                {
                    registerUiState.postValue(GenericUiState.error("Registration failed: " + insertException.getMessage()));
                });
    }

    public LiveData<GenericUiState<String>> getRegisterUiState()
    {
        return registerUiState;
    }
}
