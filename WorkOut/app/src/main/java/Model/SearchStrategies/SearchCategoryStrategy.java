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

public class SearchCategoryStrategy implements SearchStrategyInterface<Category>
{
    private final FirebaseFirestore db;
    private final CourseRepository courseRepository;

    public SearchCategoryStrategy()
    {
        db = FirebaseFirestore.getInstance();
        courseRepository = new CourseRepository();
    }

    /**
     * Find all the businesses that have a matching category
     * @param category the category the user wants
     * @return list of businesses that have that category
     */
    @Override
    public Task<List<Business>> search(Category category)
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
}
