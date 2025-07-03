package Model.Repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Client;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Enrollment;
import Model.Entity;
import Model.Schedule;
import Model.SearchStrategies.SearchStrategyInterface;

public class CourseRepository
{
    FirebaseFirestore db;
    private final String collection = "businesses";
    private final String subCollection = "courses";
    private final String enrollmentCollection = "enrollment";

    public CourseRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    public CourseRepository(FirebaseFirestore db)
    {
        this.db = db;
    }

    public Task<Course> insertCourse(Business business, String name, Schedule schedule, int capacity,
                                     CourseType type, AgeRange ageRange, Category category, String description,
                                     int duration)
    {
        Map<String, Object> course = new HashMap<>();
        course.put("name", name);
        course.put("schedule", schedule);
        course.put("participants", new ArrayList<>());
        course.put("capacity", capacity);
        course.put("type", type);
        course.put("ageRange", ageRange);
        course.put("category", category);
        course.put("description", description);
        course.put("businessId", business.getId());
        course.put("duration", duration);

        return db.collection(collection).document(business.getId()).collection(subCollection)
                .add(course).continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    DocumentReference reference = task.getResult();
                    String id = reference.getId();
                    Course add = new Course(id, type, name, new ArrayList<>(), capacity, ageRange
                            , schedule, category, description, business.getId(), duration);
                    business.addCourse(add);
                    return add;
                });
    }

    public Task<Boolean> updateCourse(Course course, Business business)
    {
        return checkIfCourseExist(business, course)
                .continueWithTask(existsTask ->
                {
                    if (!existsTask.isSuccessful())
                    {
                        return Tasks.forResult(false);
                    }
                    if (!existsTask.getResult())
                    {
                        return Tasks.forResult(false);
                    }
                    return updateHelper(course, business)
                            .continueWithTask(updateTask ->
                            {
                                if (!updateTask.isSuccessful())
                                {
                                    return Tasks.forResult(false);
                                }
                                boolean ok = business.updateCourse(course);
                                return Tasks.forResult(ok);
                            });
                });
    }

    public Task<Boolean> updateHelper(Course course, Business business)
    {
        DocumentReference current = db.collection(collection).document(business.getId())
                .collection(subCollection).document(course.getId());
        return current.set(course).continueWith(Task::isSuccessful);
    }

    public Task<Boolean> deleteCourse(Course course, Business business)
    {
        return checkIfCourseExist(business, course)
                .continueWithTask(existsTask ->
                {
                    if (!existsTask.isSuccessful())
                    {
                        return Tasks.forResult(false);
                    }
                    if (!existsTask.getResult())
                    {
                        return Tasks.forResult(false);
                    }
                    return deleteHelper(course, business);
                });
    }

    public Task<Boolean> checkIfCourseExist(Business business, Course course)
    {
        return db.collection(collection).document(business.getId()).collection(subCollection).
                document(course.getId()).get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        return false;
                    if (task.getResult() == null)
                        return false;

                    return task.getResult().exists();
                });
    }

    public Task<Boolean> deleteHelper(Course course, Business business)
    {
        if (course == null || business == null)
        {
            return Tasks.forException(new IllegalArgumentException("Course or Business is null"));
        }

        DocumentReference current = db.collection(collection).document(business.getId())
                .collection(subCollection).document(course.getId());
        return current.delete().continueWith(Task::isSuccessful);
    }

    public Task<List<Course>> getCoursesById(Course course)
    {
        return db.collection(collection).document(course.getBusinessId())
                .collection(subCollection).document(course.getId()).get()
                .continueWith(task ->
                {
                    if (!task.isSuccessful())
                    {
                        throw Objects.requireNonNull(task.getException());
                    }
                    DocumentSnapshot snap = task.getResult();
                    if (snap == null || !snap.exists())
                    {
                        return Collections.emptyList();
                    }
                    Course fetched = snap.toObject(Course.class);
                    // restore ID on the model if you don’t set it via @PropertyName
                    if (fetched != null)
                    {
                        fetched.setId(snap.getId());
                    }
                    return Collections.singletonList(fetched);
                });
    }


    public Task<List<Course>> getAllBusinessesCourses(Business business)
    {
        return db.collection(collection).document(business.getId()).collection(subCollection).get()
                .continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    QuerySnapshot snap = task.getResult();
                    List<Course> courses = new ArrayList<>();
                    if (snap != null)
                    {
                        for (DocumentSnapshot doc : snap.getDocuments())
                        {
                            Course course = doc.toObject(Course.class);
                            if (course != null)
                            {
                                course.setId(doc.getId());
                                courses.add(course);
                                business.addCourse(course);
                            }
                        }
                    }
                    return courses;
                });
    }

    /**
     * This function is responsible for all the search logic. It receives a generic
     * SearchInterface that decides how to search and an object that acts as a search filter.
     *
     * @param searchStrategy dictates how to search
     * @param data           the relative search data
     * @return a list of courses that agree with the search terms
     */
    public Task<List<Course>> searchByStrategy(SearchStrategyInterface searchStrategy, Object data)
    {
        return searchStrategy.searchCourses(data);
    }

    public Task<List<Course>> getCoursesByDayOfWeek(Business business, Day day)
    {
        return db.collection(collection).document(business.getId()).collection(subCollection)
                .whereEqualTo("schedule.day", day).get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    QuerySnapshot querySnapshot = task.getResult();
                    List<Course> courses = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot)
                    {
                        Course current = documentSnapshot.toObject(Course.class);
                        if (current != null)
                            courses.add(current);

                    }

                    return courses;
                });
    }

    public Task<Boolean> signupClientToCourse(Client client, Course course, Timestamp timestamp)
    {
        Map<String, Object> enroll = new HashMap();
        enroll.put("client", client);
        enroll.put("course", course);
        enroll.put("time", timestamp);

        if (!course.insertParticipant(client))
            throw new IllegalArgumentException("Course is full");

        return db.collection(enrollmentCollection).add(enroll).continueWith(Task::isSuccessful);
    }

    public Task<Boolean> cancelSignupClientToCourse(Enrollment enrollment)
    {
        if (!enrollment.getCourse().removeParticipant(enrollment.getClient()))
            throw new IllegalArgumentException("Client didn't signup to class");


        DocumentReference documentReference = db.collection(enrollmentCollection).document(enrollment.getId());
        return documentReference.delete().continueWith(Task::isSuccessful);
    }

    public Task<Enrollment> getEnrollmentByDate(Client client, Course course, Timestamp start, Timestamp end)
    {
        return db.collection(enrollmentCollection).whereGreaterThanOrEqualTo("time", start)
                .whereLessThanOrEqualTo("time", end).whereEqualTo("client", client.getId())
                .whereEqualTo("course", course.getId()).limit(1).get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    Enrollment enrollment;
                    if (task.getResult() != null && !task.getResult().isEmpty())
                        enrollment = task.getResult().getDocuments().get(0).toObject(Enrollment.class);
                    else
                        throw new NoSuchElementException("No enrollment found");

                    return enrollment;
                });
    }

    public Task<List<Enrollment>> getAllEnrollmentsByClient(Client client)
    {
        return db.collection(enrollmentCollection).whereEqualTo("client.id", client.getId())
                .get().continueWith(task ->
                {
                    if (!task.isSuccessful())
                        throw Objects.requireNonNull(task.getException());

                    ArrayList<Enrollment> enrollments = new ArrayList<>();
                    for (DocumentSnapshot snapshot : task.getResult())
                    {
                        Enrollment enrollment = snapshot.toObject(Enrollment.class);
                        if (enrollment != null)
                            enrollments.add(enrollment);
                    }

                    return enrollments;
                });
    }

    /**
     * This method receives a client and returns all the courses it participated in the past
     * it returns enrollments that contain a timestamp when the client participated in the course
     * @param client the client we want to get his history
     * @return Task<List<Enrollment>> the occurred in the past
     */
    public Task<List<Enrollment>> getClientHistory(Client client)
    {
        return getAllEnrollmentsByClient(client).continueWith(task ->
        {
            if(!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            List<Enrollment> enrollments = new ArrayList<>();
            for (Enrollment enrollment : task.getResult())
            {
                if(enrollment.getTime().getSeconds() < Timestamp.now().getSeconds()) // Check if it's in the past
                    enrollments.add(enrollment);
            }

            return enrollments;
        });
    }
}
