package Model.Repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Executors;

import Model.Business;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.Address;
import Model.PhonePrefix;
import Model.Rating;

public class ClientRepository
{
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final String collection = "clients";
    private final GeneralRepository generalRepository;
    private final OkHttpClient httpClient; // Declared here

    public ClientRepository()
    {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        generalRepository = new GeneralRepository();
        httpClient = new OkHttpClient();
    }

    public ClientRepository(FirebaseAuth auth, FirebaseFirestore db, GeneralRepository generalRepo)
    {
        this.auth = auth;
        this.db = db;
        this.generalRepository = generalRepo;
        httpClient = new OkHttpClient();
    }


    /**
     * This function is responsible for the logic of client insertion to the database.
     * It returns an instance of the new client if it succeeded with the new client id.
     * If communication with the DB fails it throws an exception.
     * If the username or email already exist it also throws an exception.
     *
     * @param username  user's username
     * @param password  user's password
     * @param phone     user's phone
     * @param email     user's email
     * @param firstName user's firstname
     * @param lastName  user's password
     * @param address   user's address
     * @param gender    user's gender
     * @return a new client instance if succeeded, exception otherwise
     */
    public Task<Client> insertClient(String username, String password, Phone phone, String email, String firstName,
                                     String lastName, Address address, Gender gender)
    {
        // check existence
        return generalRepository.canRegisterUser(collection, username, email)
                // depending on the check, either fail or go add()
                .continueWithTask(checkTask ->
                {
                    if (!checkTask.isSuccessful())
                    {
                        // propagate any error from the existence check
                        throw Objects.requireNonNull(checkTask.getException());
                    }
                    boolean exists = checkTask.getResult();
                    if (exists)
                    {
                        // short-circuit: username taken
                        return Tasks.forException(
                                new IllegalArgumentException("Username already exists"));
                    }
                    return auth.createUserWithEmailAndPassword(email, password);
                    // username free add the new document
                })
                // map the DocumentReference into your Client
                .continueWithTask(authTask ->
                {
                    if (!authTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(authTask.getException());
                    }

                    String uid = authTask.getResult().getUser().getUid();

                    // Prepare user data
                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("phone", phone);
                    user.put("firstName", firstName);
                    user.put("lastName", lastName);
                    user.put("address", address);
                    user.put("gender", gender);

                    return db.collection(collection).document(uid).set(user).continueWith(addTask ->
                    {
                        if (!addTask.isSuccessful())
                        {
                            throw Objects.requireNonNull(addTask.getException());
                        }
                        return new Client(uid, username, phone, email, firstName, lastName, address, gender);
                    });
                });

    }

    /**
     * This method updates client's data excluding email, username and password
     * those will be updated separately if needed
     *
     * @param client the client to update
     * @return Task<True> if succeeded, Task<False> if failed
     */
    public Task<Boolean> updateClientByID(Client client)
    {
        // Get the client's DocumentReference
        DocumentReference currentClient = db.collection(collection).document(client.getId());

        return currentClient.set(client).continueWith(task -> task.isSuccessful());
    }

    public Task<Boolean> deleteClientByID(Client client)
    {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
        {
            // no user signed in
            return Tasks.forException(
                    new IllegalStateException("No user is currently signed in"));
        }
        if (!user.getUid().equals(client.getId()))
        {
            throw new IllegalArgumentException("Error trying to delete a user that is not the current active user");
        }

        DocumentReference currentClient = db.collection(collection).document(client.getId());
        return currentClient.delete().continueWithTask(task ->
        {
            if (!task.isSuccessful())
            {
                throw Objects.requireNonNull(task.getException());
            }

            return user.delete();
        }).continueWith(isSuccessful ->
        {
            if (!isSuccessful.isSuccessful())
            {
                throw Objects.requireNonNull(isSuccessful.getException());
            }
            return Boolean.TRUE;
        });
    }

    public Task<Client> checkLogin(String username, String password)
    {
        return getClientByUsername(username)
                .continueWithTask(fetchTask ->
                {
                    if (!fetchTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(fetchTask.getException());
                    }
                    Client client = fetchTask.getResult();
                    if (client == null)
                    {
                        // no such user
                        return Tasks.forException(
                                new NoSuchElementException("No such user: " + username));
                    }

                    client.setId(fetchTask.getResult().getId());
                    // Check the password using FirebaseAuth
                    return auth.signInWithEmailAndPassword(client.getEmail(), password)
                            .continueWithTask(authTask ->
                            {
                                if (!authTask.isSuccessful())
                                {
                                    throw Objects.requireNonNull(authTask.getException());
                                }
                                FirebaseUser user = authTask.getResult().getUser();
                                if (user == null || !user.getUid().equals(client.getId()))
                                {
                                    return Tasks.forException(
                                            new SecurityException("Authenticated UID mismatch"));
                                }
                                // If we get here, the login succeeded
                                return Tasks.forResult(client);
                            });
                });
    }

    public Task<Client> getClientByUsername(String username)
    {
        return db.collection(collection)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWith(task ->
                {
                    if (!task.isSuccessful())
                    {
                        // bubble up Firestore errors
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot snap = task.getResult();
                    if (snap == null || snap.isEmpty())
                    {
                        // no user found
                        throw new IllegalArgumentException(
                                "No client with username: " + username);
                    }

                    // grab the first (and only) document
                    DocumentSnapshot doc = snap.getDocuments().get(0);

                    // This should work if your Client class has a no-arg constructor
                    // and getters/setters for every field, in short POJO convention
                    Client client = doc.toObject(Client.class);
                    if (client == null)
                    {
                        throw new IllegalStateException(
                                "Failed to map document to Client");
                    }
                    client.setId(doc.getId());
                    return client;

                    /*// If this doesn't work we can try the other method in the comments
                    String id = doc.getId();
                    String user = doc.getString("username");
                    String password = doc.getString("password");
                    //Phone phone = doc.toObject(Phone.class);     // or doc.get("phone", Phone.class);
                    Phone phone = new Phone(PhonePrefix.PREFIX_052, "5427435"); // For testing purposes
                    String email = doc.getString("email");
                    String firstName = doc.getString("firstName");
                    String lastName = doc.getString("lastName");
                    //Address address = doc.toObject(Address.class);
                    Address address = new Address(new City("1", "Oranit"), "Hayarkon"); // For testing purposes
                    //Gender gender = doc.get("gender", Gender.class);
                    Gender gender = Gender.Male; // For testing purposes
                    return new Client(id, user, password, phone, email, firstName, lastName, address, gender);*/
                });
    }

    /**
     * Handles Google Sign-In: Authenticates with Firebase, fetches profile from People API,
     * and creates or updates the client in Firestore.
     *
     * @param idTokenForFirebase      The Google ID token for Firebase sign-in.
     * @param accessTokenForPeopleApi The Google OAuth2 access token for People API.
     * @param serverAuthCode          Optional: Server auth code (can be used by a backend, not directly used here for People API).
     * @return Task<Client> that resolves to the Client object.
     */
    public Task<Client> handleGoogleAuthWithFirebase(String idTokenForFirebase,
            String accessTokenForPeopleApi, @Nullable String serverAuthCode)
    { // serverAuthCode might be used if your backend calls People API

        Log.d("ClientRepository", "handleGoogleAuthWithFirebase called.");
        Log.d("ClientRepository", "ID Token (Firebase) starts with: " + (idTokenForFirebase.length() > 10 ? idTokenForFirebase.substring(0, 10) : idTokenForFirebase));
        Log.d("ClientRepository", "Access Token (People API) starts with: " + (accessTokenForPeopleApi.length() > 10 ? accessTokenForPeopleApi.substring(0, 10) : accessTokenForPeopleApi));


        // Step 1: Authenticate with Firebase using the Google ID Token
        AuthCredential credential = GoogleAuthProvider.getCredential(idTokenForFirebase, null);
        return auth.signInWithCredential(credential)
                .continueWithTask(authTask ->
                {
                    if (!authTask.isSuccessful() || authTask.getResult() == null)
                    {
                        Log.e("ClientRepository", "Firebase Authentication failed.", authTask.getException());
                        throw Objects.requireNonNull(authTask.getException());
                    }
                    FirebaseUser firebaseUser = authTask.getResult().getUser();
                    if (firebaseUser == null)
                    {
                        Log.e("ClientRepository", "FirebaseUser is null after successful authentication.");
                        throw new IllegalStateException("FirebaseUser is null after successful authentication.");
                    }
                    Log.d("ClientRepository", "Firebase Authentication successful. UID: " + firebaseUser.getUid() + ", Email: " + firebaseUser.getEmail());

                    // Step 2: Fetch profile from Google People API (off the main thread)
                    Task<PeopleProfile> peopleApiTask = fetchPeopleProfile(accessTokenForPeopleApi);

                    // Step 3: Once People API data is fetched, proceed to check/create/update client in Firestore
                    return peopleApiTask.continueWithTask(profileTask ->
                    {
                        if (!profileTask.isSuccessful() || profileTask.getResult() == null)
                        {
                            Log.e("ClientRepository", "Failed to fetch profile from People API.", profileTask.getException());
                            // Decide if you want to proceed with just Firebase data or fail
                            // For now, let's proceed with Firebase data and log the error.
                            // If People API is critical, throw profileTask.getException()
                            // Create a "default" or empty profile if API fails but Firebase auth succeeded.
                            PeopleProfile fallbackProfile = new PeopleProfile("", "", "", firebaseUser.getEmail(),
                                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                            return processClientData(firebaseUser, fallbackProfile);
                        }
                        Log.d("ClientRepository", "People API profile fetched successfully.");
                        PeopleProfile profile = profileTask.getResult();
                        return processClientData(firebaseUser, profile);
                    });
                });
    }

    /**
     * Helper to fetch data from Google People API.
     */
    private Task<PeopleProfile> fetchPeopleProfile(String accessToken)
    {
        return Tasks.call(Executors.newSingleThreadExecutor(), () ->
        {
            Log.d("ClientRepository", "Fetching People API profile...");
            Request req = new Request.Builder()
                    .url("https://people.googleapis.com/v1/people/me"
                            + "?personFields=names,emailAddresses,phoneNumbers,addresses,genders,birthdays,metadata") // Added metadata for primary flags
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Log.d("ClientRepository", "Requesting People API URL: " + req.url().toString());

            try (Response resp = httpClient.newCall(req).execute())
            {
                String responseBodyString = resp.body() != null ? resp.body().string() : null;

                if (!resp.isSuccessful() || responseBodyString == null)
                {
                    Log.e("ClientRepository", "People API Request Failed!");
                    Log.e("ClientRepository", "URL: " + req.url().toString());
                    Log.e("ClientRepository", "Response Code: " + resp.code());
                    Log.e("ClientRepository", "Response Message: " + resp.message());
                    Log.e("ClientRepository", "Response Body: " + responseBodyString);
                    throw new IOException("People API failed: " + resp.code());
                }
                Log.d("ClientRepository", "People API Response: " + responseBodyString);
                JSONObject root = new JSONObject(responseBodyString);
                String displayName = "";
                String givenName = "";
                String familyName = "";
                if (root.has("names"))
                {
                    JSONArray namesArray = root.getJSONArray("names");
                    for (int i = 0; i < namesArray.length(); i++)
                    {
                        JSONObject nameObj = namesArray.getJSONObject(i);
                        // Prioritize primary name
                        if (nameObj.has("metadata") && nameObj.getJSONObject("metadata").optBoolean("primary", false))
                        {
                            displayName = nameObj.optString("displayName", "");
                            givenName = nameObj.optString("givenName", "");
                            familyName = nameObj.optString("familyName", "");
                            break;
                        }
                    }
                    // Fallback if no primary name found
                    if (displayName.isEmpty() && namesArray.length() > 0)
                    {
                        JSONObject firstAvailableName = namesArray.getJSONObject(0);
                        displayName = firstAvailableName.optString("displayName", "");
                        givenName = firstAvailableName.optString("givenName", "");
                        familyName = firstAvailableName.optString("familyName", "");
                    }
                }
                String emailFromPeopleApi = "";
                if (root.has("emailAddresses"))
                {
                    JSONArray emailsArray = root.getJSONArray("emailAddresses");
                    for (int i = 0; i < emailsArray.length(); i++)
                    {
                        JSONObject emailObj = emailsArray.getJSONObject(i);
                        if (emailObj.has("metadata") && emailObj.getJSONObject("metadata").optBoolean("primary", false))
                        {
                            emailFromPeopleApi = emailObj.optString("value", "");
                            break;
                        }
                    }
                    if (emailFromPeopleApi.isEmpty() && emailsArray.length() > 0)
                    {
                        emailFromPeopleApi = emailsArray.getJSONObject(0).optString("value", "");
                    }
                }
                List<Phone> phones = new ArrayList<>();
                if (root.has("phoneNumbers"))
                {
                    JSONArray phonesArray = root.getJSONArray("phoneNumbers");
                    for (int i = 0; i < phonesArray.length(); i++)
                    {
                        JSONObject phoneObj = phonesArray.getJSONObject(i);
                        if (phoneObj.has("metadata") && phoneObj.getJSONObject("metadata").optBoolean("primary", false))
                        {
                            String num = "0" + phoneObj.optString("value");
                            if (!num.isEmpty())
                            {
                                phones.add(new Phone(PhonePrefix.inferPreFix(num), num.replaceAll("[^0-9]", ""))); // Basic sanitization
                            }
                            break; // Take primary
                        }
                    }
                    // Fallback if no primary
                    if (phones.isEmpty() && phonesArray.length() > 0)
                    {
                        String num = phonesArray.getJSONObject(0).optString("value");
                        if (!num.isEmpty())
                        {
                            phones.add(new Phone(PhonePrefix.fromString(num), num.replaceAll("[^0-9]", "")));
                        }
                    }
                }
                List<Address> addresses = new ArrayList<>();
                if (root.has("addresses"))
                {
                    JSONArray addressesArray = root.getJSONArray("addresses");
                    for (int i = 0; i < addressesArray.length(); i++)
                    {
                        JSONObject addressObj = addressesArray.getJSONObject(i);
                        if (addressObj.has("metadata") && addressObj.getJSONObject("metadata").optBoolean("primary", false))
                        {
                            addresses.add(new Address(
                                    new City(addressObj.optString("city")), // Assuming City constructor takes string
                                    addressObj.optString("streetAddress")
                            ));
                            break; // Take primary
                        }
                    }
                    // Fallback
                    if (addresses.isEmpty() && addressesArray.length() > 0)
                    {
                        JSONObject firstAddress = addressesArray.getJSONObject(0);
                        addresses.add(new Address(
                                new City(firstAddress.optString("city")),
                                firstAddress.optString("streetAddress")
                        ));
                    }
                }
                List<Gender> genders = new ArrayList<>();
                if (root.has("genders"))
                {
                    JSONArray gendersArray = root.getJSONArray("genders");
                    if (gendersArray.length() > 0)
                    { // People API typically returns one gender
                        String g = gendersArray.getJSONObject(0).optString("value", "").toUpperCase();
                        try
                        {
                            if (!g.isEmpty())
                                genders.add(Gender.fromString(g));
                        }
                        catch (IllegalArgumentException ignored)
                        {
                            Log.w("ClientRepository", "Unknown gender value from People API: " + g);
                            Log.w("ClientRepository", "Assigning default - Male");
                            genders.add(Gender.Male);
                        }
                    }
                }
                return new PeopleProfile(displayName, givenName, familyName, emailFromPeopleApi, phones, addresses, genders);
            }
            catch (Exception e)
            { // Catch broader exceptions for JSON parsing etc.
                Log.e("ClientRepository", "Error processing People API response or making request.", e);
                throw e; // Re-throw to fail the task
            }
        });
    }

    /**
     * Helper to process FirebaseUser and PeopleProfile data to create/update Client in Firestore.
     */
    private Task<Client> processClientData(FirebaseUser firebaseUser, PeopleProfile profile)
    {
        String uid = firebaseUser.getUid();
        DocumentReference clientDocRef = db.collection(collection).document(uid);

        return clientDocRef.get().continueWithTask(task ->
        {
            if (!task.isSuccessful())
            {
                Log.e("ClientRepository", "Failed to get client document from Firestore (UID: " + uid + ").", task.getException());
                throw Objects.requireNonNull(task.getException());
            }
            DocumentSnapshot document = task.getResult();
            Client client;
            boolean wasInitiallyFirstLogin; // Flag to check the original state from DB

            if (document.exists())
            {
                Log.d("ClientRepository", "Client exists in Firestore. UID: " + uid);
                client = document.toObject(Client.class);
                if (client == null)
                {
                    // This can happen if Firestore data doesn't match Client POJO
                    Log.e("ClientRepository", "Failed to map existing Firestore document to Client object (UID: " + uid + "). Creating new Client instance for update.");
                    client = new Client(); // Create a new one to avoid nulls but use existing UID
                    client.setId(uid);
                    wasInitiallyFirstLogin = true; // Treat as first login if mapping failed, to ensure profile completion flow
                }
                else
                {
                    client.setId(uid); // Ensure ID is set from FirebaseUser, overriding if different (shouldn't be)
                    wasInitiallyFirstLogin = client.isFirstLogin(); // Get the state from DB
                    Log.d("ClientRepository", "Existing client (UID: " + uid + "). Initial 'isFirstLogin' from DB: " + wasInitiallyFirstLogin);
                }

                // Update existing client with fresh data
                // Use Firebase email as primary, fallback to People API email if Firebase one is null/empty
                client.setEmail(firebaseUser.getEmail() != null && !firebaseUser.getEmail().isEmpty() ? firebaseUser.getEmail() : profile.emailFromPeopleApi);
                // Username: based on email.
                client.setUsername(client.getEmail()); // Ensure username is updated if email changed

                // Names: Prioritize People API given/family if available, then display name, then existing.
                if (!profile.givenName.isEmpty() || !profile.familyName.isEmpty())
                {
                    client.setFirstName(!profile.givenName.isEmpty() ? profile.givenName : "");
                    client.setLastName(!profile.familyName.isEmpty() ? profile.familyName : "");
                }
                else if (!profile.displayName.isEmpty())
                {
                    String[] names = profile.displayName.split(" ", 2);
                    client.setFirstName(names.length > 0 ? names[0] : profile.displayName);
                    client.setLastName(names.length > 1 ? names[1] : "");
                }
                else
                {
                    // Keep existing names if People API provided nothing for names
                    if (client.getFirstName() == null) client.setFirstName("");
                    if (client.getLastName() == null) client.setLastName("");
                }

                if (!profile.phones.isEmpty())
                {
                    client.setPhone(profile.phones.get(0)); // Set primary phone from People API
                }
                else if (client.getPhone() == null && wasInitiallyFirstLogin)
                {
                    // If no phone from People API and it was a first login, keep existing (null) or set to a default if necessary
                }

                if (!profile.addresses.isEmpty())
                {
                    client.setAddress(profile.addresses.get(0)); // Set primary address from People API
                }
                else if (client.getAddress() == null && wasInitiallyFirstLogin)
                {
                    // If no address from People API and it was a first login
                }

                if (!profile.genders.isEmpty())
                {
                    client.setGender(profile.genders.get(0)); // Set primary gender from People API
                }
                else if (client.getGender() == null && wasInitiallyFirstLogin)
                {
                    // If no gender from People API and it was a first login
                }

                // Password field is not managed here for Google Sign-In users
                // If it was their first login (or treated as such due to mapping error),
                // and we've now populated/updated data, it's no longer a "first login"
                // for the purpose of app navigation (e.g., redirecting to "complete profile").
                if (wasInitiallyFirstLogin)
                {
                    client.setFirstLogin(false);
                    Log.d("ClientRepository", "Updated client (UID: " + uid + "). 'isFirstLogin' set to false.");
                }
                // If wasInitiallyFirstLogin was already false, client.isFirstLogin() remains false.

                Client finalClient = client;
                return clientDocRef.set(client, SetOptions.merge())
                        .continueWith(updateTask ->
                        {
                            if (!updateTask.isSuccessful())
                            {
                                Log.w("ClientRepository", "Failed to update existing client (UID: " + uid + ") in Firestore.", updateTask.getException());
                                // Proceed with the client object even if Firestore update fails for this attempt
                            }
                            else
                            {
                                Log.d("ClientRepository", "Successfully updated existing client (UID: " + uid + ") in Firestore.");
                            }
                            return finalClient; // Return the (potentially updated) client object
                        });
            }
            else
            {
                // User is new, create a new Client object
                Log.d("ClientRepository", "Client does not exist in Firestore. Creating new. UID: " + uid);
                client = new Client();
                client.setId(uid); // Firebase UID as the document ID

                // Use Firebase email as primary, fallback to People API email
                client.setEmail(firebaseUser.getEmail() != null && !firebaseUser.getEmail().isEmpty() ? firebaseUser.getEmail() : profile.emailFromPeopleApi);
                client.setUsername(client.getEmail()); // Username based on email

                // Populate from PeopleProfile
                if (!profile.givenName.isEmpty() || !profile.familyName.isEmpty())
                {
                    client.setFirstName(!profile.givenName.isEmpty() ? profile.givenName : "");
                    client.setLastName(!profile.familyName.isEmpty() ? profile.familyName : "");
                }
                else if (!profile.displayName.isEmpty())
                {
                    String[] names = profile.displayName.split(" ", 2);
                    client.setFirstName(names.length > 0 ? names[0] : profile.displayName);
                    client.setLastName(names.length > 1 ? names[1] : "");
                }
                else
                {
                    client.setFirstName(""); // Default if no name info
                    client.setLastName("");
                }

                if (!profile.phones.isEmpty()) client.setPhone(profile.phones.get(0));
                if (!profile.addresses.isEmpty()) client.setAddress(profile.addresses.get(0));
                if (!profile.genders.isEmpty()) client.setGender(profile.genders.get(0));

                client.setFirstLogin(true); // Mark as first login for app flow
                Log.d("ClientRepository", "New client (UID: " + uid + "). 'isFirstLogin' set to true.");

                Client finalClient1 = client;
                return clientDocRef.set(client)
                        .continueWith(creationTask ->
                        {
                            if (!creationTask.isSuccessful())
                            {
                                Log.e("ClientRepository", "Failed to create new client (UID: " + uid + ") in Firestore.", creationTask.getException());
                                throw Objects.requireNonNull(creationTask.getException());
                            }
                            Log.d("ClientRepository", "Successfully created new client (UID: " + uid + ") in Firestore.");
                            return finalClient1; // Return the newly created client object
                        });
            }
        });
    }


    /**
     * Simple holder for the People API result.
     */
    private static class PeopleProfile
    {
        String displayName;
        String givenName; // Added
        String familyName; // Added
        String emailFromPeopleApi; // Added
        List<Phone> phones;
        List<Address> addresses;
        List<Gender> genders;
        // String photoUrl; // Optional

        PeopleProfile(String displayName, String givenName, String familyName, String emailFromPeopleApi,
                      List<Phone> phones, List<Address> addresses, List<Gender> genders)
        {
            this.displayName = displayName;
            this.givenName = givenName;
            this.familyName = familyName;
            this.emailFromPeopleApi = emailFromPeopleApi;
            this.phones = phones;
            this.addresses = addresses;
            this.genders = genders;
        }
    }

    // TODO: add a rating by client function
    public void addRatingByClient(Rating rating, Business business)
    {
        business.addRating(rating);
    }


}
