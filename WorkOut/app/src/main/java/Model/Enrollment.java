package Model;

import com.google.firebase.Timestamp;

import java.util.Comparator;

/**
 * Represents a client's enrollment in a course, with timestamp.
 * Extends {@link Entity} to inherit a unique ID.
 */
public class Enrollment extends Entity
{
    private Client clientId;
    private Course courseId;
    private Timestamp signedUpAt; // or com.google.firebase.Timestamp

    /** No-arg constructor for Firestore serialization. */
    public Enrollment()
    {
    }

    /**
     * Constructs an Enrollment.
     *
     * @param id         unique enrollment ID
     * @param clientId   the client enrolling
     * @param courseId   the course enrolled in
     * @param signedUpAt timestamp of signup
     */
    public Enrollment(String id, Client clientId, Course courseId, Timestamp signedUpAt)
    {
        super(id);
        this.clientId = clientId;
        this.courseId = courseId;
        this.signedUpAt = signedUpAt;
    }

    /** @return the enrolled client */
    public Client getClient() { return clientId; }

    /** @param clientId the client to set */
    public void setClient(Client clientId) { this.clientId = clientId; }

    /** @return the course enrolled */
    public Course getCourse() { return courseId; }

    /** @param courseId the course to set */
    public void setCourse(Course courseId) { this.courseId = courseId; }

    /** @return signup timestamp */
    public Timestamp getTime() { return signedUpAt; }

    /** @param signedUpAt the timestamp to set */
    public void setTime(Timestamp signedUpAt) { this.signedUpAt = signedUpAt; }

    /** Comparator to order enrollments by signup time (oldest first). */
    public static final Comparator<Enrollment> BY_TIME =
            Comparator.comparing(Enrollment::getTime);



}

