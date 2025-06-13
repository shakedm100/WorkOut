package Model;

import com.google.firebase.Timestamp;

public class Enrollment extends Entity
{
    private Client clientId;
    private Course courseId;
    private Timestamp signedUpAt; // or com.google.firebase.Timestamp

    // Firestore needs a no-arg constructor
    public Enrollment()
    {
    }

    public Enrollment(Client clientId, Course courseId, Timestamp signedUpAt)
    {
        this.clientId = clientId;
        this.courseId = courseId;
        this.signedUpAt = signedUpAt;
    }

    // getters & setters
    public Client getClient()
    {
        return clientId;
    }

    public void setClient(Client clientId)
    {
        this.clientId = clientId;
    }

    public Course getCourse()
    {
        return courseId;
    }

    public void setCourse(Course courseId)
    {
        this.courseId = courseId;
    }

    public Timestamp getTime()
    {
        return signedUpAt;
    }

    public void setTime(Timestamp signedUpAt)
    {
        this.signedUpAt = signedUpAt;
    }
}

