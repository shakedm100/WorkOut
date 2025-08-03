package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import Model.Business;
import Model.Category;
import Model.Course;
import Model.Repository.CourseRepository;

/**
 * A {@link SearchStrategyInterface} implementation that finds
 * {@link Business} or {@link Course} entities based on a specified {@link Category}.
 * <p>
 * Performs a collectionGroup query on all “courses” subcollections to locate courses
 * matching the given category. For the business search path, it groups matching
 * courses by their parent business, fetches each business document, and attaches
 * the filtered course list.
 * </p>
 */
public class SearchCategoryStrategy implements SearchStrategyInterface<Category>
{
    private final FirebaseFirestore db;
    private final CourseRepository courseRepository;

    /**
     * Constructs a new SearchCategoryStrategy using the Firestore singleton
     * and a default {@link CourseRepository}.
     */
    public SearchCategoryStrategy()
    {
        db = FirebaseFirestore.getInstance();
        courseRepository = new CourseRepository();
    }

    /**
     * Searches for {@link Business} entities that offer at least one {@link Course}
     * in the specified {@code category}.
     * <ol>
     *   <li>Performs a collectionGroup query on “courses” where {@code category} matches.</li>
     *   <li>Groups the resulting Course objects by their parent business reference.</li>
     *   <li>Fetches each Business document and attaches its filtered Course list.</li>
     * </ol>
     *
     * @param category the {@link Category} to filter courses by
     * @return a Task completing with a List of matching {@link Business} objects,
     *         each populated with its relevant {@link Course} subcollection;
     *         or an empty list if none found.
     * @throws RuntimeException if any Firestore operation fails.
     */
    @Override
    public Task<List<Business>> searchBusinesses(Category category)
    {
        return db.collectionGroup("courses")
                .whereEqualTo("category", category).get().onSuccessTask(querySnap -> {
                    // group courses by their parent business ref
                    Map<DocumentReference, List<Course>> coursesByBiz = new HashMap<>();
                    for (DocumentSnapshot cs : querySnap) {
                        Course c = cs.toObject(Course.class);
                        DocumentReference bizRef = cs.getReference()
                                .getParent()   // "courses"
                                .getParent();  // the business doc
                        if (c != null && bizRef != null) {
                            coursesByBiz
                                    .computeIfAbsent(bizRef, __ -> new ArrayList<>())
                                    .add(c);
                        }
                    }

                    if (coursesByBiz.isEmpty()) {
                        return Tasks.forResult(Collections.emptyList());
                    }

                    // 3) for each business ref, fetch the document and attach its courses
                    List<Task<Business>> bizTasks = coursesByBiz.entrySet().stream()
                            .map(entry -> {
                                DocumentReference bizRef = entry.getKey();
                                List<Course> courseList = entry.getValue();

                                // fetch the Business document
                                return bizRef.get().continueWith(bizSnapTask -> {
                                    if (!bizSnapTask.isSuccessful())
                                        throw Objects.requireNonNull(bizSnapTask.getException());

                                    DocumentSnapshot bsnap = bizSnapTask.getResult();
                                    Business b = bsnap.toObject(Business.class);
                                    if (b != null) {
                                        b.setId(bsnap.getId());
                                        b.setCourses((ArrayList<Course>) courseList);
                                    }
                                    return b;
                                });
                            })
                            .collect(Collectors.toList());

                    // 4) wait for *all* those business+courses fetches to complete
                    return Tasks.<Business>whenAllSuccess(bizTasks);
                })

                // 5) ensure the final Task is typed Task<List<Business>>
                .continueWith(finalTask -> {
                    if (!finalTask.isSuccessful())
                        throw Objects.requireNonNull(finalTask.getException());

                    @SuppressWarnings("unchecked")
                    List<Business> result = (List<Business>) finalTask.getResult();
                    return result;
                });
    }

    /**
     * Searches for {@link Course} entities in the specified {@code category}.
     * <ol>
     *   <li>Performs a collectionGroup query on “courses” where {@code category} matches.</li>
     *   <li>Maps each matching document to a {@link Course}, sets its Firestore document ID,</li>
     *   <li>and annotates it with its parent businessId.</li>
     * </ol>
     *
     * @param category the {@link Category} to filter courses by
     * @return a Task completing with a List of matching {@link Course} objects
     * @throws RuntimeException if the Firestore query fails.
     */
    @Override
    public Task<List<Course>> searchCourses(Category category) {
        return db.collectionGroup("courses")
                .whereEqualTo("category", category)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    List<Course> results = new ArrayList<>();
                    for (DocumentSnapshot cs : task.getResult()) {
                        // 1) Turn the document into a Course object
                        Course c = cs.toObject(Course.class);
                        if (c == null) continue;

                        // 2) Set the Course’s own Firestore ID
                        c.setId(cs.getId());

                        // 3) Look up the parent business reference
                        DocumentReference bizRef = cs.getReference()
                                .getParent()   // the “courses” subcollection
                                .getParent();  // the “businesses/{bizId}” parent
                        if (bizRef != null) {
                            // We assume you have a setBusinessId(String) on Course
                            c.setBusinessId(bizRef.getId());
                        }

                        results.add(c);
                    }

                    return results;
                });
    }

}
