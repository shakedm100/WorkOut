package Model;

import android.annotation.SuppressLint;

import com.google.firebase.Timestamp;

import java.util.Comparator;

/**
 * Represents a client's enrollment in a course, with timestamp.
 * Extends {@link Entity} to inherit a unique ID.
 */
@SuppressLint("ParcelCreator")
public class Enrollment extends Entity
{
    private Client client;
    private Course course;
    private Timestamp signedUpAt; // or com.google.firebase.Timestamp

    /** No-arg constructor for Firestore serialization. */
    public Enrollment()
    {
    }

    /**
     * Constructs an Enrollment.
     *
     * @param id         unique enrollment ID
     * @param client   the client enrolling
     * @param course   the course enrolled in
     * @param signedUpAt timestamp of signup
     */
    public Enrollment(String id, Client client, Course course, Timestamp signedUpAt)
    {
        super(id);
        this.client = client;
        this.course = course;
        this.signedUpAt = signedUpAt;
    }

    /** @return the enrolled client */
    public Client getClient() { return client; }

    /** @param clientId the client to set */
    public void setClient(Client clientId) { this.client = clientId; }

    /** @return the course enrolled */
    public Course getCourse() { return course; }

    /** @param courseId the course to set */
    public void setCourse(Course courseId) { this.course = courseId; }

    /** @return signup timestamp */
    public Timestamp getTime() { return signedUpAt; }

    /** @param signedUpAt the timestamp to set */
    public void setTime(Timestamp signedUpAt) { this.signedUpAt = signedUpAt; }

    /** Comparator to order enrollments by signup time (oldest first). */
    public static final Comparator<Enrollment> BY_TIME =
            Comparator.comparing(Enrollment::getTime);



}

