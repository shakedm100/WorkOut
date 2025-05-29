package Model.Repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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
import Model.SearchStrategies.SearchStrategyInterface;

public class BusinessRepository
{
    private final FirebaseFirestore db;
    private final String collection = "businesses";

    public BusinessRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Business> insertBusiness(String username, String password, Phone phone, String email,
                                         String businessName, Location location,
                                         String policy)
    {
        Map<String, Object> business = new HashMap<>();
        business.put("username", username);
        business.put("password", password);
        business.put("phone", phone);
        business.put("email", email);
        business.put("businessName", businessName);
        business.put("location", location);
        business.put("ratings", new ArrayList<>()); // No ratings for new business
        business.put("followers", new ArrayList<>()); // No followers for new business
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
            {
                throw Objects.requireNonNull(task.getException());
            }

            DocumentReference ref = task.getResult();
            String id = ref.getId();
            return new Business(id, username, password, phone, email, businessName, new ArrayList<>(), location,
                    new ArrayList<>(), new ArrayList<>(), policy);
        });
    }

    public Task<Boolean> updateBusiness(Business business)
    {
        // Get the business's DocumentReference
        DocumentReference currentBusiness = db.collection(collection).document(business.getId());

        // If a lambda does one action, it can be done with Method Reference instead
        // for better syntax
        return currentBusiness.set(business).continueWith(Task::isSuccessful);
    }

    public Task<Boolean> deleteBusiness(Business business)
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
                    {
                        throw Objects.requireNonNull(task.getException());
                    }

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
                    business.setCourses(new ArrayList<>());
                    CourseRepository courseRepository = new CourseRepository();
                    courseRepository.getAllBusinessesCourses(business);
                    return business;
                });
    }

    public Task<Business> checkLogin(String username, String password) //TODO: Maybe can generalize to user
    {
        return getBusinessByUsername(username).continueWith(task ->
        {
            if (!task.isSuccessful())
            {
                throw Objects.requireNonNull(task.getException());
            }

            Business business = task.getResult();
            if (business == null)
            {
                throw new NoSuchElementException("No such user: " + username);
            }

            if (business.getPassword().equals(password))
            {
                return business;
            }
            throw new IllegalArgumentException("Invalid password");
        });
    }

    /**
     * This function is responsible for all the search logic. It receives a generic
     * SearchInterface that decides how to search and an object that acts as a search filter.
     * @param searchStrategy dictates how to search
     * @param data the relative search data
     * @return a list of businesses that agree with the search terms
     */
    public Task<List<Business>> searchByStrategy(SearchStrategyInterface searchStrategy, Object data)
    {
        return searchStrategy.search(data);
    }

    public Task<Business> addRatingToBusiness(Rating rating, Business business)
    {
        if(!business.addRating(rating))
            throw new RuntimeException("Insert to business failed!");

        return updateBusiness(business).continueWith(task ->
        {
            if(!task.isSuccessful())
            {
                business.deleteRating(rating);
                throw Objects.requireNonNull(task.getException());
            }

            return business;
        });
    }

/*    public Task<Business> addFollowerToBusiness(Client follower, Business business)
    {
        if(!business.addRating(follower))
            throw new RuntimeException("Insert to business failed!");

        return updateBusiness(business).continueWith(task ->
        {
            if(!task.isSuccessful())
            {
                business.deleteRating(follower);
                throw Objects.requireNonNull(task.getException());
            }

            return business;
        });
    }*/
}
