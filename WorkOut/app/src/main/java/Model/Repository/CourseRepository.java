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

/**
 * Repository for managing {@link Course} entities and enrollments under a given {@link Business}.
 * <p>
 * Uses Firestore subcollections for courses and a top‐level collection for enrollments.
 * </p>
 */
public class CourseRepository
{
    FirebaseFirestore db;
    private final String collection = "businesses";
    private final String subCollection = "courses";
    private final String enrollmentCollection = "enrollment";

    /**
     * Default constructor initializes with Firestore singleton.
     */
    public CourseRepository()
    {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Injection constructor for testing.
     *
     * @param db Firestore instance to use
     */
    public CourseRepository(FirebaseFirestore db)
    {
        this.db = db;
    }

    /**
     * Creates a new {@link Course} under the given {@link Business} in Firestore and
     * adds it to the business's in‐memory course list.
     *
     * @param business    owning business
     * @param name        course name
     * @param schedule    meeting schedule
     * @param capacity    maximum number of participants
     * @param type        course type
     * @param ageRange    allowed age range
     * @param category    course category
     * @param description textual description
     * @param duration    duration in minutes
     * @return a Task completing with the created Course (with assigned ID),
     * or failing with any Firestore exception.
     */
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

    /**
     * Updates an existing {@link Course} document if it exists under the business,
     * and updates the in‐memory list on success.
     *
     * @param course   the course to update (must have valid ID and businessId)
     * @param business the owning business
     * @return a Task completing with {@code true} if update succeeded, {@code false} otherwise.
     */
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

    /**
     * Helper that overwrites the Firestore document for a given course.
     *
     * @param course   the course to write
     * @param business the owning business
     * @return a Task completing with {@code true} if the set succeeded, {@code false} otherwise.
     */
    public Task<Boolean> updateHelper(Course course, Business business)
    {
        DocumentReference current = db.collection(collection).document(business.getId())
                .collection(subCollection).document(course.getId());
        return current.set(course).continueWith(Task::isSuccessful);
    }

    /**
     * Deletes a {@link Course} document under the business if it exists.
     *
     * @param course   the course to delete
     * @param business the owning business
     * @return a Task completing with {@code true} if deletion succeeded, {@code false} otherwise.
     */
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

    /**
     * Checks whether a given {@link Course} exists under the specified {@link Business}.
     *
     * @param business the owning business
     * @param course   the course to check
     * @return a Task completing with {@code true} if the document exists, {@code false} otherwise.
     */
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

    /**
     * Deletes a Firestore document for the specified course under the business.
     *
     * @param course   the course to delete
     * @param business the owning business
     * @return a Task completing with {@code true} if deletion succeeded.
     * @throws IllegalArgumentException if {@code course} or {@code business} is {@code null}.
     */
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

    /**
     * Fetches a single {@link Course} by its ID.
     *
     * @param course a Course instance containing businessId and courseId
     * @return a Task completing with a singleton list containing the Course if found,
     * or an empty list if not found; fails on Firestore errors.
     */
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

    /**
     * Retrieves all {@link Course}s for a given {@link Business} and populates
     * the business's in‐memory course list.
     *
     * @param business the business whose courses to fetch
     * @return a Task completing with the list of all courses; fails on Firestore errors.
     */
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
     * Executes a custom search strategy for {@link Course}s.
     *
     * @param searchStrategy strategy implementing {@link SearchStrategyInterface}
     * @param data           filter data passed to the strategy
     * @return a Task completing with matching courses.
     */
    public Task<List<Course>> searchByStrategy(SearchStrategyInterface searchStrategy, Object data)
    {
        return searchStrategy.searchCourses(data);
    }

    /**
     * Queries for all {@link Course}s under a business that occur on a given {@link Day}.
     *
     * @param business owning business
     * @param day      day of week to match
     * @return a Task completing with the list of matching courses.
     */
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

    /**
     * Signs up a {@link Client} to a {@link Course} by creating an enrollment record.
     *
     * @param client    client to enroll
     * @param course    course to enroll in
     * @param timestamp enrollment timestamp
     * @return a Task completing with {@code true} if enrollment write succeeded.
     * @throws IllegalArgumentException if the course is already full.
     */
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

    /**
     * Cancels an existing {@link Enrollment}, removing the client from the course
     * and deleting the enrollment document.
     *
     * @param enrollment enrollment to cancel (must have valid id)
     * @return a Task completing with {@code true} if deletion succeeded.
     * @throws IllegalArgumentException if the client was not enrolled.
     */
    public Task<Boolean> cancelSignupClientToCourse(Enrollment enrollment)
    {
        if (!enrollment.getCourse().removeParticipant(enrollment.getClient()))
            throw new IllegalArgumentException("Client didn't signup to class");


        DocumentReference documentReference = db.collection(enrollmentCollection).document(enrollment.getId());
        return documentReference.delete().continueWith(Task::isSuccessful);
    }

    /**
     * Retrieves a single {@link Enrollment} for a client in a course within a time window.
     *
     * @param client client who enrolled
     * @param course course enrolled in
     * @param start  lower bound timestamp (inclusive)
     * @param end    upper bound timestamp (inclusive)
     * @return a Task completing with the found Enrollment
     * @throws NoSuchElementException if no matching enrollment is found.
     */
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

    /**
     * Fetches all {@link Enrollment} records for a given {@link Client}.
     *
     * @param client client whose enrollments to fetch
     * @return a Task completing with the list of enrollments; fails on Firestore errors.
     */
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
     * Returns the history of past {@link Enrollment}s for a client—i.e., those with
     * timestamps earlier than now.
     *
     * @param client client whose past enrollments to retrieve
     * @return a Task completing with the list of past enrollments.
     */
    public Task<List<Enrollment>> getClientHistory(Client client)
    {
        return getAllEnrollmentsByClient(client).continueWith(task ->
        {
            if (!task.isSuccessful())
                throw Objects.requireNonNull(task.getException());

            List<Enrollment> enrollments = new ArrayList<>();
            for (Enrollment enrollment : task.getResult())
            {
                if (enrollment.getTime().getSeconds() < Timestamp.now().getSeconds()) // Check if it's in the past
                    enrollments.add(enrollment);
            }

            return enrollments;
        });
    }
}
