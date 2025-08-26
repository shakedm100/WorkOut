package Model.SearchStrategies;

import static com.google.android.gms.tasks.Tasks.await;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import Model.AgeRange;
import Model.Business;
import Model.Course;
import Model.Repository.CourseRepository;
import Model.Repository.GeneralRepository;
import okhttp3.OkHttpClient;

/**
 * A {@link SearchStrategyInterface} implementation that finds
 * {@link Business} or {@link Course} entities whose age ranges overlap
 * a specified {@link AgeRange}.
 * <p>
 * Uses a collectionGroup query on all “courses” subcollections to locate
 * courses whose {@code ageRange} overlaps the provided range, then
 * groups by parent business and fetches full documents.
 * </p>
 */
public class SearchAgeStrategy implements SearchStrategyInterface<AgeRange>
{
    private FirebaseFirestore db;
    private CourseRepository courseRepository;

    /**
     * Constructs a new SearchAgeStrategy using the Firestore singleton
     * and a default {@link CourseRepository}.
     */
    public SearchAgeStrategy()
    {
        db = FirebaseFirestore.getInstance();
        courseRepository = new CourseRepository();
    }

    /**
     * Injection constructor for testing.
     *
     * @param courseRepository the CourseRepository to use
     * @param db               FirebaseFirestore instance to use
     */
    public SearchAgeStrategy(CourseRepository courseRepository, FirebaseFirestore db)
    {
        this.courseRepository = courseRepository;
        this.db = db;
    }

    /**
     * Finds all {@link Business} entities that offer at least one {@link Course}
     * whose {@code ageRange} overlaps the given {@code ageRange}.
     * <ol>
     *   <li>Queries for courses where minAge ≤ provided maxAge and maxAge ≥ provided minAge.</li>
     *   <li>Collects unique business document references from matching courses.</li>
     *   <li>Fetches each Business document.</li>
     *   <li>For each business, loads its full list of courses and attaches them.</li>
     * </ol>
     *
     * @param ageRange the desired age range (min and max)
     * @return a Task completing with a List of matching {@link Business} objects,
     *         each populated with all its {@link Course}s;
     *         or an empty list if none match.
     * @throws RuntimeException if any Firestore operation fails.
     */
    @Override
    public Task<List<Business>> searchBusinesses(AgeRange ageRange)
    {
        return db.collectionGroup("courses")
                .whereLessThanOrEqualTo("ageRange.minAge", ageRange.getMaxAge())
                .whereGreaterThanOrEqualTo("ageRange.maxAge", ageRange.getMinAge())
                .get()

                // Flat-map the matching course docs into their parent‐business refs
                .onSuccessTask(courseSnap ->
                {
                    Set<DocumentReference> bizRefs = new HashSet<>();
                    for (DocumentSnapshot cs : courseSnap)
                    {
                        DocumentReference bizRef = cs.getReference()
                                .getParent()    // “courses”
                                .getParent();   // the business doc
                        if (bizRef != null) bizRefs.add(bizRef);
                    }
                    if (bizRefs.isEmpty())
                    {
                        // no businesses then immediate empty List<Business>
                        return Tasks.forResult(Collections.emptyList());
                    }
                    // fetch each business document
                    List<Task<DocumentSnapshot>> bizFetches = bizRefs.stream()
                            .map(DocumentReference::get)
                            .collect(Collectors.toList());
                    // whenAllSuccess here returns Task<List<DocumentSnapshot>>
                    return Tasks.<DocumentSnapshot>whenAllSuccess(bizFetches);
                })

                // Convert DocumentSnapshots → Business instances
                .onSuccessTask(bizSnapsRaw ->
                {
                    @SuppressWarnings("unchecked")
                    List<DocumentSnapshot> bizSnaps = (List<DocumentSnapshot>) bizSnapsRaw;

                    // map each snapshot to a Business (but courses still missing)
                    List<Business> businesses = bizSnaps.stream()
                            .map(ds ->
                            {
                                Business b = ds.toObject(Business.class);
                                b.setId(ds.getId());
                                return b;
                            })
                            .collect(Collectors.toList());

                    // For each Business, fetch its courses and attach them
                    List<Task<Business>> withCourses = businesses.stream()
                            .map(b -> courseRepository
                                    .getAllBusinessesCourses(b)                  // Task<List<Course>>
                                    .continueWith(cTask ->
                                    {
                                        if (!cTask.isSuccessful()) throw cTask.getException();
                                        b.setCourses((ArrayList<Course>) cTask.getResult());         // populate
                                        return b;                                // now a Task<Business>
                                    })
                            )
                            .collect(Collectors.toList());

                    // whenAllSuccess on the List<Task<Business>> gives Task<List<Business>>
                    return Tasks.<Business>whenAllSuccess(withCourses);
                })

                // Just in case, turn the final raw List<Object> into List<Business>
                .continueWith(finalTask ->
                {
                    if (!finalTask.isSuccessful()) throw finalTask.getException();
                    @SuppressWarnings("unchecked")
                    List<Business> result = (List<Business>) finalTask.getResult();
                    return result;
                });
    }

    /**
     * Finds all {@link Course} entities whose {@code ageRange} overlaps
     * the given {@code ageRange}.
     * <ol>
     *   <li>Queries for courses where minAge ≤ provided maxAge and maxAge ≥ provided minAge.</li>
     *   <li>Maps each matching document to a {@link Course}, setting its ID and
     *       annotating with its parent businessId.</li>
     * </ol>
     *
     * @param ageRange the desired age range (min and max)
     * @return a Task completing with a List of matching {@link Course} objects.
     * @throws RuntimeException if the Firestore query fails.
     */
    @Override
    public Task<List<Course>> searchCourses(AgeRange ageRange)
    {
        return db.collectionGroup("courses")
                // 1) Find all Course docs whose ageRange overlaps
                .whereLessThanOrEqualTo("ageRange.minAge", ageRange.getMaxAge())
                .whereGreaterThanOrEqualTo("ageRange.maxAge", ageRange.getMinAge())
                .get()

                // 2) Convert each DocumentSnapshot → Course
                .continueWith(task ->
                {
                    if (!task.isSuccessful())
                    {
                        throw Objects.requireNonNull(task.getException());
                    }

                    List<Course> results = new ArrayList<>();
                    for (DocumentSnapshot ds : task.getResult().getDocuments()) // task.getResult()
                    {
                        Course course = ds.toObject(Course.class);
                        if (course == null) continue;

                        // 2a) Set the Course’s own ID
                        course.setId(ds.getId());

                        DocumentReference bizRef = ds.getReference()
                                .getParent()   // “courses” subcollection
                                .getParent();  // the business document
                        if (bizRef != null)
                        {
                            course.setBusinessId(bizRef.getId());
                        }

                        results.add(course);
                    }
                    return results;
                });
    }
}

