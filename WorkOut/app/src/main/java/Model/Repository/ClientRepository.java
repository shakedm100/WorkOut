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

    public ClientRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * This function is responsible for the logic of client insertion to the database.
     * It returns an instance of the new client if it succeeded with the new client id.
     * If communication with the DB fails it throws an exception.
     * If the username or email already exist it also throws an exception.
     * @param username user's username
     * @param password user's password
     * @param phone user's phone
     * @param email user's email
     * @param firstName user's firstname
     * @param lastName user's password
     * @param address user's address
     * @param gender user's gender
     * @return a new client instance if succeeded, exception otherwise
     */
    public Task<Client> insertClient(String username, String password, Phone phone, String email, String firstName,
                                     String lastName, Address address, Gender gender)
    {
        // prepare your user data
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("email", email);
        //user.put("phone", phone);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        //user.put("address", address);
        //user.put("gender", gender);

        // 1) check existence
        return canRegisterUser(username, email)
                // 2) depending on the check, either fail or go add()
                .continueWithTask(checkTask -> {
                    if (!checkTask.isSuccessful()) {
                        // propagate any error from the existence check
                        throw Objects.requireNonNull(checkTask.getException());
                    }
                    boolean exists = checkTask.getResult();
                    if (exists) {
                        // short-circuit: username taken
                        return Tasks.forException(
                                new IllegalArgumentException("Username already exists"));
                    }
                    // username free → add the new document
                    return db.collection("clients").add(user);
                })
                // 3) map the DocumentReference into your Client
                .continueWith(addTask -> {
                    if (!addTask.isSuccessful()) {
                        throw Objects.requireNonNull(addTask.getException());
                    }
                    DocumentReference ref = addTask.getResult();
                    String id = ref.getId();
                    return new Client(id, username, password, phone, email, firstName, lastName, address, gender);
                });
    }


    /**
     * This method receives username and email and check if one of them already exists
     * in the database, if one of them exist it returns true async
     * and if not it returns false async
     * throws an error if communication failed
     * @param username the user's username
     * @param email the user's email
     * @return Task<false> if the user's username & email don't exit. true otherwise
     */
    private Task<Boolean> canRegisterUser(String username, String email)
    {
        return db.collection("clients")
                .where(
                        Filter.or(
                                Filter.equalTo("username", username),
                                Filter.equalTo("email", email)
                        )
                )
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot snap = task.getResult();
                    // true -> we found at least one document with either username or email
                    return snap != null && !snap.isEmpty();
                });
    }

    public Task<Boolean> updateClientByID(Client client)
    {
        // Get the client's DocumentReference
        DocumentReference currentClient = db.collection("clients").document(client.getId());

        return currentClient.set(client).continueWith(task -> task.isSuccessful());
    }

    public Task<Boolean> deleteClientByID(Client client)
    {
        DocumentReference currentClient = db.collection("clients").document(client.getId());

        return currentClient.delete().continueWith(task -> task.isSuccessful());
    }

    public Task<Client> checkLogin(String username, String password)
    {
        return getClientByUsername(username).continueWith(task -> {
            if(!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            Client client = task.getResult();
            if(client == null)
                throw new NoSuchElementException("No such user: " + username);

            if(client.getPassword().equals(password))
                return client;
            throw new IllegalArgumentException("Invalid password");
        });
    }

    public Task<Client> getClientByUsername(String username)
    {
        return db.collection("clients")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        // bubble up Firestore errors
                        throw Objects.requireNonNull(task.getException());
                    }
                    QuerySnapshot snap = task.getResult();
                    if (snap == null || snap.isEmpty()) {
                        // no user found
                        throw new IllegalArgumentException(
                                "No client with username: " + username);
                    }

                    // grab the first (and only) document
                    DocumentSnapshot doc = snap.getDocuments().get(0);

                    // If this doesn't work we can try the other method in the comments
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
                    return new Client(id, user, password, phone, email, firstName, lastName, address, gender);
        });
    }

    public Task<Client> handleGoogleAuthWithFirebase(String idToken)
    {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.signInWithCredential(firebaseCredential).continueWith(task ->
        {
            if(!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            FirebaseUser googleClient = task.getResult().getUser();
            if (googleClient == null) {
                throw new IllegalStateException("FirebaseUser was null");
            }
            String firstName = "";
            String lastName = "";
            if(googleClient.getDisplayName() != null)
            {
                String[] firstAndLast = googleClient.getDisplayName().split(" ", 2);
                firstName = firstAndLast[0];
                if(firstAndLast.length > 1)
                    lastName = firstAndLast[1];
            }

            String phoneNumber = googleClient.getPhoneNumber();
            Phone phone = null;
            if(PhonePrefix.fromString(phoneNumber) != null)
                phone = new Phone(PhonePrefix.fromString(phoneNumber), phoneNumber);
            return new Client(googleClient.getUid(), "", "", phone,
                    googleClient.getEmail(), firstName, lastName, null, null);
        });
    }


}
