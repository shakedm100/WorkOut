package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import Model.Business;
import Model.CourseType;
import Model.Course;
import Model.Repository.CourseRepository;

/**
 * A {@link SearchStrategyInterface} implementation that finds
 * {@link Business} or {@link Course} entities based on a specified {@link CourseType}.
 * <p>
 * Uses a collectionGroup query on the “courses” subcollections to locate all courses
 * matching the given type, then groups them by parent business for the business search
 * path.
 * </p>
 */
public class SearchCourseTypeStrategy implements SearchStrategyInterface<CourseType>
{
    private FirebaseFirestore db;

    /**
     * Constructs a new SearchCourseTypeStrategy using the Firestore singleton.
     */
    public SearchCourseTypeStrategy()
    {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Injection constructor for testing.
     *
     * @param db FirebaseFirestore instance to use
     */
    public SearchCourseTypeStrategy(FirebaseFirestore db)
    {
        this.db = db;
    }

    /**
     * Searches for {@link Business} entities that offer at least one
     * {@link Course} of the given {@code type}.
     * <ol>
     *   <li>Performs a collectionGroup query on “courses” where {@code type} matches.</li>
     *   <li>Groups resulting Course objects by their parent business reference.</li>
     *   <li>Fetches each Business document and attaches the filtered Course list.</li>
     * </ol>
     *
     * @param type the {@link CourseType} to filter by
     * @return a Task completing with a List of matching {@link Business} objects,
     *         each populated with its relevant {@link Course} subcollection;
     *         or an empty list if none found.
     * @throws RuntimeException if any Firestore operation fails.
     */
    @Override
    public Task<List<Business>> searchBusinesses(CourseType type)
    {
        return db.collectionGroup("courses")
                .whereEqualTo("type", type)
                .get()

                // 2) flat-map into business refs → Business objects with courses
                .onSuccessTask(querySnap ->
                {
                    // group courses by their parent business ref
                    Map<DocumentReference, List<Course>> coursesByBiz = new HashMap<>();
                    for (DocumentSnapshot cs : querySnap.getDocuments()) // added getDocs
                    {
                        Course c = cs.toObject(Course.class);
                        DocumentReference bizRef = cs.getReference()
                                .getParent()   // "courses"
                                .getParent();  // the business doc
                        if (c != null && bizRef != null)
                        {
                            coursesByBiz
                                    .computeIfAbsent(bizRef, __ -> new ArrayList<>())
                                    .add(c);
                        }
                    }

                    if (coursesByBiz.isEmpty())
                    {
                        return Tasks.forResult(Collections.emptyList());
                    }

                    // 3) for each business ref, fetch the document and attach its courses
                    List<Task<Business>> bizTasks = coursesByBiz.entrySet().stream()
                            .map(entry ->
                            {
                                DocumentReference bizRef = entry.getKey();
                                List<Course> courseList = entry.getValue();

                                // fetch the Business document
                                return bizRef.get().continueWith(bizSnapTask ->
                                {
                                    if (!bizSnapTask.isSuccessful())
                                        throw Objects.requireNonNull(bizSnapTask.getException());

                                    DocumentSnapshot bsnap = bizSnapTask.getResult();
                                    Business b = bsnap.toObject(Business.class);
                                    if (b != null)
                                    {
                                        b.setId(bsnap.getId());
                                        b.setCourses((ArrayList<Course>) courseList);
                                    }
                                    return b;
                                });
                            })
                            .collect(Collectors.toList());

                    // 4) wait for all those business+courses fetches to complete
                    return Tasks.<Business>whenAllSuccess(bizTasks);
                })

                // 5) ensure the final Task is typed Task<List<Business>>
                .continueWith(finalTask ->
                {
                    if (!finalTask.isSuccessful())
                        throw Objects.requireNonNull(finalTask.getException());

                    @SuppressWarnings("unchecked")
                    List<Business> result = (List<Business>) finalTask.getResult();
                    return result;
                });
    }

    /**
     * Searches for {@link Course} entities of the specified {@code type}.
     * <ol>
     *   <li>Performs a collectionGroup query on “courses” where {@code type} matches.</li>
     *   <li>Maps each matching document to a {@link Course}, sets its document ID,</li>
     *   <li>and annotates with its parent businessId.</li>
     * </ol>
     *
     * @param type the {@link CourseType} to filter by
     * @return a Task completing with a List of matching {@link Course} objects.
     * @throws RuntimeException if the Firestore query fails.
     */
    @Override
    public Task<List<Course>> searchCourses(CourseType type) {
        return db.collectionGroup("courses")
                .whereEqualTo("type", type)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    List<Course> results = new ArrayList<>();
                    for (DocumentSnapshot cs : task.getResult().getDocuments()) // getDocuments
                    {
                        // 1) Convert to Course
                        Course c = cs.toObject(Course.class);
                        if (c == null) continue;

                        // 2) Set the Course’s own document ID
                        c.setId(cs.getId());

                        // 3) Find the parent business reference and set its ID on the Course
                        DocumentReference bizRef = cs.getReference()
                                .getParent()   // “courses” collection
                                .getParent();  // “businesses/{bizId}” doc
                        if (bizRef != null) {
                            c.setBusinessId(bizRef.getId());
                        }

                        results.add(c);
                    }
                    return results;
                });
    }

}
