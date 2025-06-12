package Model.Repository;

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
import com.google.firebase.firestore.Filter;

import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.Address;
import Model.PhonePrefix;


public class ClientRepository
{
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final String collection = "clients";

    public ClientRepository()
    {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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
        GeneralRepository generalRepository = new GeneralRepository();
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

    public Task<Client> handleGoogleAuthWithFirebase(String idToken)
    {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.signInWithCredential(firebaseCredential).continueWith(task ->
        {
            if (!task.isSuccessful())
            {
                throw Objects.requireNonNull(task.getException());
            }

            FirebaseUser googleClient = task.getResult().getUser();
            if (googleClient == null)
            {
                throw new IllegalStateException("FirebaseUser was null");
            }
            String firstName = "";
            String lastName = "";
            if (googleClient.getDisplayName() != null)
            {
                String[] firstAndLast = googleClient.getDisplayName().split(" ", 2);
                firstName = firstAndLast[0];
                if (firstAndLast.length > 1)
                {
                    lastName = firstAndLast[1];
                }
            }

            String phoneNumber = googleClient.getPhoneNumber();
            Phone phone = null;
            if (PhonePrefix.fromString(phoneNumber) != null)
            {
                phone = new Phone(PhonePrefix.fromString(phoneNumber), phoneNumber);
            }
            return new Client(googleClient.getUid(), "", phone,
                    googleClient.getEmail(), firstName, lastName, null, null);
        });
    }


}
