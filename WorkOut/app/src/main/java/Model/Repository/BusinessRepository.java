package Model.Repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import Model.Business;
import Model.Client;
import Model.Course;
import Model.Location;
import Model.Phone;
import Model.Rating;

public class BusinessRepository
{
    private final FirebaseFirestore db;
    private final String collection = "businesses";

    public BusinessRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Business> insertBusiness(String username, String password, Phone phone, String email,
                                         String businessName, List<Course> courses, Location location,
                                         String policy)
    {
        Map<String, Object> business = new HashMap<>();
        business.put("username", username);
        business.put("password", password);
        business.put("phone", phone);
        business.put("email", email);
        business.put("businessName", businessName);
        business.put("courses", courses); //TODO: maybe also enter null at insert
        business.put("location", location);
        business.put("ratings", null); // No ratings for new business
        business.put("followers", null); // No followers for new business
        business.put("policy", policy);

        GeneralRepository generalRepository = new GeneralRepository();

        return generalRepository.canRegisterUser(collection, username, email).continueWithTask(checkTask ->
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

            return db.collection(collection).add(business);
        }).continueWith(task ->
        {
            if (!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            DocumentReference ref = task.getResult();
            String id = ref.getId();
            return new Business(id, username, password, phone, email, businessName, courses, location,
                    null, null, policy);
        });
    }

    public Task<Boolean> updateBusinessByID(Business business)
    {
        // Get the business's DocumentReference
        DocumentReference currentBusiness = db.collection(collection).document(business.getId());

        // If a lambda does one action, it can be done with Method Reference instead
        // for better syntax
        return currentBusiness.set(business).continueWith(Task::isSuccessful);
    }

    public Task<Boolean> deleteBusinessByID(Business business)
    {
        DocumentReference currentClient = db.collection(collection).document(business.getId());

        return currentClient.delete().continueWith(Task::isSuccessful);
    }

    // This method gets the business by the username
    public Task<Business> getBusinessByUsername(String username) //TODO: Maybe can generalize to user
    {
        return db.collection(collection).whereEqualTo("username", username)
                .limit(1).get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot == null || snapshot.isEmpty())
                    {
                        // no user found
                        throw new IllegalArgumentException(
                                "No business with username: " + username);
                    }

                    DocumentSnapshot doc = snapshot.getDocuments().get(0);
                    Business business = doc.toObject(Business.class);

                    if (business == null)
                    {
                        throw new IllegalStateException(
                                "Failed to map document to Business");
                    }

                    business.setId(doc.getId());
                    return business;
                });
    }

    public Task<Business> checkLogin(String username, String password) //TODO: Maybe can generalize to user
    {
        return getBusinessByUsername(username).continueWith(task ->
        {
            if (!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            Business business = task.getResult();
            if (business == null)
                throw new NoSuchElementException("No such user: " + username);

            if (business.getPassword().equals(password))
                return business;
            throw new IllegalArgumentException("Invalid password");
        });
    }
}
