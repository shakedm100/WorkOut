package Model.Repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
