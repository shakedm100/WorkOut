package Model.SearchStrategies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Model.Business;
import Model.Course;
import Model.Day;
import Model.Repository.CourseRepository;

public class SearchDayOfWeekStrategy implements SearchStrategyInterface<Day>
{
    private FirebaseFirestore db;
    private CourseRepository courseRepository;

    /**
     * Constructs a new SearchAgeStrategy using the Firestore singleton
     */
    public SearchDayOfWeekStrategy()
    {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Injection constructor for testing.
     *
     * @param db FirebaseFirestore instance to use
     */
    public SearchDayOfWeekStrategy(FirebaseFirestore db)
    {
        this.db = db;
    }

    @Override
    public Task<List<Business>> searchBusinesses(Day day)
    {
        return db.collection("businesses")
                .get()
                .onSuccessTask(bizSnap ->
                {
                    List<Task<Business>> bizTasks = new ArrayList<>();

                    for (DocumentSnapshot bizDoc : bizSnap)
                    {
                        String bizId = bizDoc.getId();
                        Business business = bizDoc.toObject(Business.class);
                        if (business == null) continue;
                        business.setId(bizId);

                        // For each business, check its courses for a day match
                        Task<Business> t = db.collection("businesses")
                                .document(bizId)
                                .collection("courses")
                                .get()
                                .continueWith(courseSnapTask ->
                                {
                                    if (!courseSnapTask.isSuccessful())
                                    {
                                        throw Objects.requireNonNull(courseSnapTask.getException());
                                    }

                                    boolean hasMatch = false;
                                    List<Course> allCourses = new ArrayList<>();

                                    for (DocumentSnapshot cs : courseSnapTask.getResult())
                                    {
                                        Course c = cs.toObject(Course.class);
                                        if (c == null) continue;

                                        c.setId(cs.getId());
                                        c.setBusinessId(bizId);
                                        allCourses.add(c);

                                        if (c.getSchedule() != null &&
                                                day.equals(c.getSchedule().getDay()))
                                        {
                                            hasMatch = true;
                                        }
                                    }

                                    if (hasMatch)
                                    {
                                        // attach all courses (not only the ones on that day)
                                        business.setCourses(new ArrayList<>(allCourses));
                                        return business;
                                    }
                                    else
                                    {
                                        return null; // skip this business
                                    }
                                });

                        bizTasks.add(t);
                    }

                    // Combine per‑business tasks
                    return Tasks.whenAllSuccess(bizTasks);
                })
                .continueWith(finalTask ->
                {
                    if (!finalTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(finalTask.getException());
                    }

                    @SuppressWarnings("unchecked")
                    List<Business> maybeList = (List<Business>) (List<?>) finalTask.getResult();

                    // Filter out nulls (businesses without a matching course)
                    List<Business> result = new ArrayList<>();
                    for (Business b : maybeList)
                    {
                        if (b != null) result.add(b);
                    }
                    return result;
                });
    }


    @Override
    public Task<List<Course>> searchCourses(Day day)
    {
        // Fetch all businesses
        return db.collection("businesses")
                .get()
                .onSuccessTask(bizSnap ->
                {
                    List<Task<List<Course>>> courseTasks = new ArrayList<>();

                    for (DocumentSnapshot bizDoc : bizSnap.getDocuments()) // get docs
                    {
                        String bizId = bizDoc.getId();

                        // For each business, fetch its courses
                        Task<List<Course>> t = db.collection("businesses")
                                .document(bizId)
                                .collection("courses")
                                .get()
                                .continueWith(courseSnapTask ->
                                {
                                    if (!courseSnapTask.isSuccessful())
                                    {
                                        throw Objects.requireNonNull(courseSnapTask.getException());
                                    }

                                    List<Course> matching = new ArrayList<>();
                                    for (DocumentSnapshot cs : courseSnapTask.getResult())
                                    {
                                        Course c = cs.toObject(Course.class);
                                        if (c == null || c.getSchedule() == null) continue;

                                        // Filter: only add courses with the given Day
                                        if (day.equals(c.getSchedule().getDay()))
                                        {
                                            c.setId(cs.getId());       // Firestore ID
                                            c.setBusinessId(bizId);    // parent business
                                            matching.add(c);
                                        }
                                    }
                                    return matching;
                                });

                        courseTasks.add(t);
                    }

                    // Combine all course lists into one
                    return Tasks.whenAllSuccess(courseTasks);
                })
                .continueWith(finalTask ->
                {
                    if (!finalTask.isSuccessful())
                    {
                        throw Objects.requireNonNull(finalTask.getException());
                    }

                    @SuppressWarnings("unchecked")
                    List<List<Course>> listOfLists = (List<List<Course>>) (List<?>) finalTask.getResult();
                    List<Course> all = new ArrayList<>();
                    for (List<Course> sub : listOfLists)
                    {
                        all.addAll(sub);
                    }
                    return all;
                });
    }
}
