package Model.Repository;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.Address;

public class ClientRepository
{
    private FirebaseFirestore db;

    public ClientRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Client> insertClient(String username, String password, Phone phone, String email,
                        String firstName, String lastName, Address address, Gender gender)
    {
        if (db == null)
            db = FirebaseFirestore.getInstance(); // Make sure you initialize FirebaseFirestore

        // Create a new user map
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        // Add a new document with the user's UID as the document ID
        return db.collection("users").add(user)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return new Client(username,password, password, phone, email, firstName, lastName, address, gender);
                });
    }

    public Task<Client> getClientByUsername(String username) {
        if (db == null)
            db = FirebaseFirestore.getInstance();

        // Query the "users" collection where the username matches
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1) // Only get one result
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot query = task.getResult();
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);

                        // Extract fields and return a new Client object
                        String user = doc.getString("username");
                        //String email = doc.getString("email");

                        // You may need to add more fields as needed
                        return new Client(user, "", "", null, "", "", "", null, null);
                    } else {
                        return null; // Username not found
                    }
                });
    }

    public Task<Boolean> updateUserByUsername(String username, Map<String, Object> updates) {
        if (db == null)
            db = FirebaseFirestore.getInstance();

        // Find the user by username and update the first match
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot query = task.getResult();
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        return doc.getReference().update(updates)
                                .continueWith(updateTask -> updateTask.isSuccessful());
                    } else {
                        return Tasks.forResult(false); // User not found
                    }
                });
    }

    public Task<Boolean> deleteUserByUsername(String username) {
        if (db == null)
            db = FirebaseFirestore.getInstance();

        // Find the user by username and delete the first match
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot query = task.getResult();
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        return doc.getReference().delete()
                                .continueWith(deleteTask -> deleteTask.isSuccessful());
                    } else {
                        return Tasks.forResult(false); // User not found
                    }
                });
    }



    /*
    public Client selectClientByID(String username)
    {
        if (db == null)
            db = FirebaseFirestore.getInstance(); // Make sure you initialize FirebaseFirestore

        DocumentReference docRef = db.collection("users").document(username); // get client from "users" where username = given username

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) { // if document found
                String name = documentSnapshot.getString("name");

                Log.d("Firestore", "Client name: " + name + " found");
            } else {
                Log.d("Firestore", "No such client found");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error fetching client", e);
        });

    }



    public Task<Client> getClientByUsername(String username, ClientCallBack callback) {

        if (db == null)
            db = FirebaseFirestore.getInstance(); // Make sure you initialize FirebaseFirestore

        db.collection("users") // users collection
                .whereEqualTo("username", username) // where username field = given username
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Client client = doc.toObject(Client.class); // get the client
                        callback.onClientFound(client);
                    } else {
                        callback.onClientNotFound();
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateClientByUsername(String username, Map<String, Object> updatedFields, ClientCallBack callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get first matching document
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        // Update it
                        document.getReference()
                                .update(updatedFields)
                                .addOnSuccessListener(unused -> callback.onComplete(true))
                                .addOnFailureListener(e -> callback.onComplete(false));
                    } else {
                        callback.onComplete(false); // No client with that name
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }




    public boolean updateClient(Client client)
    {
        if (db == null)
            db = FirebaseFirestore.getInstance(); // Make sure you initialize FirebaseFirestore

        final boolean found = false;

        DocumentReference docRef = db.collection("users").document("SF");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        found = true;
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

 */
}
