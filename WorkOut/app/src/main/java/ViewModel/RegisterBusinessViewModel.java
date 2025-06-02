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
    public void registerBusiness(Context context, String name, String password, PhonePrefix phonePrefix, String phoneNumber, String email,
                                 String businessName, City city, String addressStr, String policy)
    {
        Phone phone = new Phone(phonePrefix, phoneNumber);
        Address address = new Address(city, addressStr);
        Geocoder geocoder = new Geocoder(context, new Locale("en", "IL"));
        Location location = generalRepository.convertAddressToLocation(geocoder, address);

        // TODO: add validations

        businessRepository.insertBusiness(name, password, phone, email, businessName, location, policy)
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
