package Model.Repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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


    public Task<Boolean> canRegisterUser(String username, String email)
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
}
