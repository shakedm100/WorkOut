package Model.Repository;

import android.location.Geocoder;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.Address;
import Model.City;
import Model.Location;
import ViewModel.GenericUiState;

//import android.location.Address;
import android.util.Log;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * A general-purpose repository for common Firestore operations and utilities:
 * <ul>
 *   <li>Querying cities</li>
 *   <li>Checking username/email availability</li>
 *   <li>Geocoding addresses</li>
 *   <li>String similarity checks</li>
 *   <li>Uploading FCM tokens</li>
 * </ul>
 */
public class GeneralRepository
{

    FirebaseFirestore db;

    /**
     * Default constructor initializes Firestore instance.
     */
    public GeneralRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Returns a hard-coded list of common Israeli mobile-phone prefixes.
     *
     * @return list of valid phone prefixes (e.g. "050", "052", ...)
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

    /**
     * Fetches all {@link City} documents from Firestore.
     *
     * @return a Task that completes with a List of all City objects,
     *         or fails with any Firestore exception.
     */
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

    /**
     * Returns up to three cities whose name begins with the given prefix.
     * <p>
     * If the prefix starts with a non-ASCII character, searches the 'name' field;
     * otherwise, uses 'englishName' (converted to uppercase).
     * </p>
     *
     * @param namePrefix the case-sensitive prefix to search by
     * @return a Task completing with a List of matching City objects,
     *         or {@code null} if {@code namePrefix} is empty.
     * @throws RuntimeException if the Firestore query fails.
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
                    String cityName = city.getEnglishName();
                    String[] parts = cityName.split(" ");

                    for (int i = 0; i < parts.length; i++)
                        parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1).toLowerCase();

                    cityName = String.join(" ", parts);
                    city.setEnglishName(cityName);

                    cities.add(city);
                }
            }

            return cities;
        });
    }

    /**
     * Checks whether a username or email already exists in the given collection.
     *
     * @param collection Firestore collection name (e.g. "clients" or "businesses")
     * @param username   username to check
     * @param email      email to check
     * @return a Task completing with {@code true} if either field exists,
     *         {@code false} otherwise.
     * @throws RuntimeException if the Firestore query fails.
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

    /**
     * Converts a domain {@link Address} to geographic {@link Location} via Android's Geocoder.
     *
     * @param geocoder Android Geocoder instance
     * @param address  domain Address (street + city)
     * @return a Location with longitude and latitude, or {@code null} if not found
     * @throws RuntimeException if geocoding fails
     */
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

    /**
     * Determines if two strings are “similar” within an adjustable Levenshtein threshold.
     *
     * @param a first string
     * @param b second string
     * @return {@code true} if the edit distance ≤ 3 (or 4 for longer strings), {@code false} otherwise
     */
    private boolean areSimilar(String a, String b) {

        int maxDistance = 3;
        if (a.length() > 15 || b.length() > 15)
            maxDistance = 4;

        LevenshteinDistance distance = new LevenshteinDistance(maxDistance); // maxDistance represents two string that are similar in some way
        Integer result = distance.apply(a.toLowerCase(), b.toLowerCase());
        return result != null; // if within maxDistance
    }

    /**
     * A more strict geocoding: only returns a location if both street and city
     * match the Geocoder result within the similarity threshold.
     *
     * @param geocoder Android Geocoder instance
     * @param address  domain Address
     * @return Location if both street and city are similar, or {@code null}
     * @throws RuntimeException if geocoding fails
     */
    public Location convertAddressToLocation2(Geocoder geocoder, Address address) {
        try {
            String streetInput = address.getName();
            String cityInput = address.getCity().getEnglishName();

            String query = streetInput + ", " + cityInput;
            List<android.location.Address> results = geocoder.getFromLocationName(query, 1);

            if (!results.isEmpty()) {
                android.location.Address result = results.get(0);

                String resultStreet = result.getThoroughfare(); // street name
                String resultCity = result.getLocality(); // city name

                // check similarity
                if (resultStreet != null && resultCity != null)
                {
                    // check only the street name
                    if (areSimilar(streetInput.replaceAll("[\\s\\-\\d]", "") + " Street", resultStreet) && areSimilar(cityInput, resultCity))
                    {
                        return new Location(result.getLongitude(), result.getLatitude());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid address" + e.getMessage());
        }

        return null; // Invalid address
    }

    /**
     * Uploads the token to the database.
     * @param token the new token of the current user.
     */
    public void uploadTokenToFirestore(String token) {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null)
        {
            String userId = auth.getCurrentUser().getUid();
            db.collection("clients").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved"))
                    .addOnFailureListener(e -> Log.w("FCM", "Error saving token", e));
        }

        else
        {
            Log.w("FCM", "User not logged in. Cannot upload token.");
        }
    }

    public boolean allLetters(String word)
    {
        String[] words = word.split(" "); // split by space
        boolean isNameValid;
        for (int i = 0; i < words.length; i++)
        {
            isNameValid = words[i].chars().allMatch(Character::isLetter);
            if (!isNameValid)
                return false; // an unvalid name was found
        }

        return true; // all letters are valid
    }
}
