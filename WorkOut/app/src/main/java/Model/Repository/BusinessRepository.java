package Model.Repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import Model.Address;
import Model.Business;
import Model.Client;
import Model.Location;
import Model.Phone;
import Model.Rating;
import Model.SearchStrategies.SearchStrategyInterface;

/**
 * Repository for managing {@link Business} entities in Firestore, including
 * creation, updates, deletions, queries and login flows.
 * <p>
 * Internally uses {@link FirebaseFirestore} for data storage,
 * {@link FirebaseAuth} for authentication, and delegates
 * some checks to a {@link GeneralRepository}.
 * </p>
 */
public class BusinessRepository
{
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final GeneralRepository generalRepository;
    private final String collection = "businesses";

    /**
     * Default constructor initializes with the singleton instances of
     * {@link FirebaseFirestore}, {@link FirebaseAuth} and a new
     * {@link GeneralRepository}.
     */
    public BusinessRepository()
    {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        generalRepository = new GeneralRepository();
    }

    /**
     * Test‐and‐dependency injection constructor.
     * Used primarily for mock testing
     *
     * @param db                 FirebaseFirestore instance to use
     * @param auth               FirebaseAuth instance to use
     * @param generalRepository  GeneralRepository instance for user‐existence checks
     */
    public BusinessRepository(FirebaseFirestore db, FirebaseAuth auth, GeneralRepository generalRepository)
    {
        this.db = db;
        this.auth = auth;
        this.generalRepository = generalRepository;
    }

    /**
     * Registers a new business user: checks username availability, creates a Firebase
     * Authentication user, and writes the business record to Firestore.
     *
     * @param username      desired unique username
     * @param password      desired password
     * @param phone         business phone number
     * @param email         email address (used for Firebase Auth)
     * @param businessName  display name of the business
     * @param location      geographic location of the business
     * @param policy        business policy text or URL
     * @param address       physical address details of the business
     * @return a Task that completes with the newly created {@link Business} object,
     * or fails with:
     * <ul>
     *  <li>{@link IllegalArgumentException} if username already exists;</li>
     *  <li>any FirebaseAuth or Firestore exception otherwise.</li>
     * </ul>
     */
    public Task<Business> insertBusiness(String username, String password, Phone phone, String email,
                                         String businessName, Location location,
                                         String policy, Address address)
    {
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

            return auth.createUserWithEmailAndPassword(email, password);
            }).continueWithTask(task ->
            {
                if(!task.isSuccessful())
                    throw Objects.requireNonNull(task.getException());

                String uid = task.getResult().getUser().getUid();
                Map<String, Object> business = new HashMap<>();
                business.put("uid", uid);
                business.put("username", username);
                business.put("phone", phone);
                business.put("email", email);
                business.put("businessName", businessName);
                business.put("location", location);
                business.put("ratings", new ArrayList<>()); // No ratings for new business
                business.put("followers", new ArrayList<>()); // No followers for new business
                business.put("policy", policy);
                business.put("address", address);

                return db.collection(collection).document(uid).set(business).continueWith(addTask ->
                {
                    if (!addTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(addTask.getException());
                    }
                    return new Business(uid, username, phone, email, businessName, new ArrayList<>(), location,
                            new ArrayList<>(), new ArrayList<>(), policy, address);
                });
            });
    }

    /**
     * Overwrites an existing business document in Firestore with the provided
     * {@link Business} object.
     *
     * @param business the business data to save (must have a valid id)
     * @return a Task that completes with {@code true} if the update succeeded,
     * or {@code false} if it failed.
     */
    public Task<Boolean> updateBusiness(Business business)
    {
        // Get the business's DocumentReference
        DocumentReference currentBusiness = db.collection(collection).document(business.getId());

        // If a lambda does one action, it can be done with Method Reference instead
        // for better syntax
        return currentBusiness.set(business).continueWith(Task::isSuccessful);
    }

    /**
     * Deletes the currently authenticated business user from both Firestore
     * and Firebase Authentication.
     *
     * @param business the business to delete (its id must match the current user)
     * @return a Task that completes with {@code true} if the deletion succeeded.
     * @throws IllegalStateException    if no user is signed in.
     * @throws IllegalArgumentException if the signed-in user does not match the business id.
     */
    public Task<Boolean> deleteBusiness(Business business)
    {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
        {
            // no user signed in
            return Tasks.forException(
                    new IllegalStateException("No user is currently signed in"));
        }
        if (!user.getUid().equals(business.getId()))
        {
            throw new IllegalArgumentException("Error trying to delete a user that is not the current active user");
        }

        DocumentReference currentClient = db.collection(collection).document(business.getId());
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

    /**
     * Fetches a {@link Business} by its unique username. Also triggers loading
     * of the business’s courses in the background.
     *
     * @param username the username to query
     * @return a Task that completes with the found {@code Business},
     * or fails with {@link IllegalArgumentException} if none found,
     * or any Firestore exception.
     */
    public Task<Business> getBusinessByUsername(String username)
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

    /**
     * Authenticates a business by username and password. First looks up the
     * {@link Business} record to retrieve its email, then signs in via
     * {@link FirebaseAuth}.
     *
     * @param username the business username
     * @param password the password to verify
     * @return a Task that completes with the authenticated {@code Business},
     * or fails with:
     * <ul>
     *  <li>{@link NoSuchElementException} if no such username;</li>
     *  <li>{@link SecurityException} if the returned UID does not match;</li>
     *  <li>any Firebase exception otherwise.</li>
     * </ul>
     */
    public Task<Business> checkLogin(String username, String password)
    {
        return getBusinessByUsername(username).continueWithTask(task ->
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
            business.setId(task.getResult().getId());

            return auth.signInWithEmailAndPassword(business.getEmail(), password).continueWithTask(authTask ->
            {
                if(!authTask.isSuccessful())
                    throw Objects.requireNonNull(authTask.getException());

                FirebaseUser user = authTask.getResult().getUser();
                if (user == null || !user.getUid().equals(business.getId()))
                {
                    return Tasks.forException(
                            new SecurityException("Authenticated UID mismatch"));
                }
                // If we get here, the login succeeded
                return Tasks.forResult(business);
            });
        });
    }

    /**
     * Executes a business search using an arbitrary {@link SearchStrategyInterface}.
     *
     * @param searchStrategy strategy that knows how to perform the query
     * @param data arbitrary filter data passed through to the strategy
     * @return a Task completing with the list of matching businesses.
     */
    public Task<List<Business>> searchByStrategy(SearchStrategyInterface searchStrategy, Object data)
    {
        return searchStrategy.searchBusinesses(data);
    }

    /**
     * Looks up, up to 8 businesses whose name begins with the given prefix
     * (case‐sensitive).
     *
     * @param namePrefix non‐empty prefix of businessName
     * @return a Task completing with the matching list, or {@code null} if the
     * {@code namePrefix} is empty.
     */
    public Task<List<Business>> getBusinessesByNamePartially(String namePrefix)
    {
        if(namePrefix.isEmpty())
            return null;

        Query query = db.collection(collection)
                .orderBy("businessName")
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff") // Signal an ending with UTF-8 encoding
                .limit(8);

        return query.get().continueWith(task ->
        {
            if (!task.isSuccessful())
            {
                throw Objects.requireNonNull(task.getException());
            }

            List<Business> businesses = new ArrayList<>();

            for (DocumentSnapshot snap : task.getResult())
            {
                Business business = snap.toObject(Business.class);
                if (business != null)
                {
                    business.setId(snap.getId());
                    businesses.add(business);
                }
            }

            return businesses;
        });
    }

    /**
     * Retrieves a single {@link Business} document by its Firestore document ID.
     *
     * @param id business document ID
     * @return a Task completing with the found Business,
     * or fails with {@link NoSuchElementException} if not found,
     * or any Firestore exception.
     */
    public Task<Business> getBusinessesById(String id)
    {
        return db.collection(collection).document(id).get().continueWith(task ->
        {
            if (!task.isSuccessful())
            {
                throw Objects.requireNonNull(task.getException());
            }

            DocumentSnapshot snap = task.getResult();
            if (snap == null || !snap.exists())
            {
                throw new NoSuchElementException("No such business: " + id);
            }

            Business business = snap.toObject(Business.class);
            if (business == null)
            {
                throw new IllegalStateException("Failed to map document to Business");
            }

            business.setId(snap.getId());
            return business;
        });
    }

    /**
     * Appends a {@link Rating} to the given {@link Business} and persists the change.
     *
     * @param business the target business (must already contain rating list)
     * @param rating   rating object to add
     * @return a Task completing with the updated Business,
     * or rolls back and throws on failure.
     */
    public Task<Business> addRatingToBusiness(Business business, Rating rating)
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

    /**
     * Removes a {@link Rating} from the given {@link Business} and persists the change.
     *
     * @param business the target business
     * @param rating   rating object to remove
     * @return a Task completing with the updated Business,
     * or rolls back and throws on failure.
     */
    public Task<Business> deleteRatingFromBusiness(Business business, Rating rating)
    {
        if(!business.deleteRating(rating))
            throw new RuntimeException("Delete from business failed!");

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

    /**
     * Updates an existing {@link Rating} in the given {@link Business} and persists.
     *
     * @param business the target business
     * @param rating   rating object with updated values
     * @return a Task completing with the updated Business,
     * or rolls back and throws on failure.
     */
    public Task<Business> updateRatingFromBusiness(Business business, Rating rating)
    {
        if(!business.updateRating(rating))
            throw new RuntimeException("Delete from business failed!");

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
}
