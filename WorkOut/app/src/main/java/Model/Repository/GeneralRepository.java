package Model.Repository;

import android.location.Geocoder;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.Address;
import Model.City;
import Model.Location;
import Model.PhonePrefix;

public class GeneralRepository
{

    FirebaseFirestore db;

    public GeneralRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    // DVIR CHANGES - added a function for the prefixes of phone

    /**
     * Returns all the possible prefixes for a phone number.
     * @return a Task which will return a list of the prefixes.
     */
    public List<String> getAllPhonePrefixes()
    {
        List<String> phonePrefixes = new ArrayList<>();
        phonePrefixes.add("050");
        phonePrefixes.add("052");
        phonePrefixes.add("053");
        phonePrefixes.add("054");
        phonePrefixes.add("055");
        phonePrefixes.add("058");

        return phonePrefixes;
    }

    public Task<List<City>> getAllCities()
    {
        Query q = db.collection("cities");

        return q.get().continueWith(task ->
        {
            if (!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            List<City> cities = new ArrayList<>();
            for (DocumentSnapshot snap : task.getResult())
            {
                City city = snap.toObject(City.class);
                if (city != null)
                    cities.add(city);
            }

            return cities;
        });
    }

    // END OF DVIR CHANGES

    /**
     * Returns up to 3 cities whose names start with the given partialName.
     *
     * @param namePrefix the name prefix to search for
     * @return a Task that completes with a List<City>
     */
    public Task<List<City>> getCityByNamePartially(String namePrefix)
    {
        // Firestore strings sort lexicographically, so we can do:
        // orderBy("name").startAt(partialName).endAt(partialName + "\uf8ff")
        // startAt and endAt tell the db what range to look at
        // So basically the code states for the db to look at all the range starting at namePrefix
        // And namePrefix + one unicode char
        // namePrefix.trim(); TODO: consider add this, my resolve future errors (exmaple: "ORANIT " with space) -> couldn't find any
        if (namePrefix.isEmpty())
            return null;

        Query q;
        if (namePrefix.charAt(0) > 128)
        {
            q = db.collection("cities")
                    .orderBy("name")
                    .startAt(namePrefix)
                    .endAt(namePrefix + "\uf8ff") // Signal an ending with UTF-8 encoding
                    .limit(3);
        } else
        {
            namePrefix = namePrefix.toUpperCase();
            q = db.collection("cities")
                    .orderBy("englishName")
                    .startAt(namePrefix)
                    .endAt(namePrefix + "\uf8ff") // Signal an ending with UTF-8 encoding
                    .limit(3);
        }
        return q.get().continueWith(task ->
        {
            if (!task.isSuccessful())
            {
                throw Objects.requireNonNull(task.getException());
            }

            List<City> cities = new ArrayList<>();
            for (DocumentSnapshot snap : task.getResult())
            {
                City city = snap.toObject(City.class);
                if (city != null)
                {
                    cities.add(city);
                }
            }

            return cities;
        });
    }

    /**
     * This method receives username and email and check if one of them already exists
     * in the database, if one of them exist it returns true async
     * and if not it returns false async
     * throws an error if communication failed
     *
     * @param username the user's username
     * @param email    the user's email
     * @return Task<false> if the user's username & email don't exit. true otherwise
     */
    public Task<Boolean> canRegisterUser(String collection, String username, String email)
    {
        return db.collection(collection)
                .where(Filter.or(Filter.equalTo("username", username),
                        Filter.equalTo("email", email)))
                .limit(1)
                .get()
                .continueWith(task ->
                {
                    if (!task.isSuccessful())
                    {
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot snap = task.getResult();
                    // true -> we found at least one document with either username or email
                    return snap != null && !snap.isEmpty();
                });
    }

    public Location convertAddressToLocation(Geocoder geocoder, Address address)
    {
        try
        {
            String location = address.getName() + ", " + address.getCity().getName();
            List<android.location.Address> theirAddress = geocoder.getFromLocationName(location, 1);
            if (!theirAddress.isEmpty())
            {
                return new Location(theirAddress.get(0).getLongitude()
                        , theirAddress.get(0).getLatitude());
            }

            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Invalid address");
        }
    }

    public Address convertLocationToAddress(Geocoder geocoder, Location location)
    {
        try
        {
            List<android.location.Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (!addresses.isEmpty())
            {
                String[] split = addresses.get(0).getFeatureName().split("/");
                String name = "";
                if(split.length > 0)
                {
                    name = split[0];
                    City city = new City(addresses.get(0).getLocality());
                    return new Address(city, name);
                }
            }

            return null;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Invalid location");
        }
    }
}
