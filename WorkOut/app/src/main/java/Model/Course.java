package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a course offering, including its schedule, participants, capacity, and related metadata.
 * Extends {@link Entity} to inherit a unique ID and implements {@link Parcelable} for Android IPC.
 */
public class Course extends Entity implements Parcelable
{
    private CourseType type;
    private String businessId;
    private String name;
    private ArrayList<Client> participants;
    private int capacity;
    private AgeRange ageRange;
    private Schedule schedule;
    private Category category;
    private String description;
    private int duration;

    /**
     * Default no-argument constructor required by Firestore/Parcelable.
     */
    public Course()
    {
    }

    /**
     * Full constructor.
     *
     * @param id           unique course ID
     * @param type         format of the course (solo/duo/group)
     * @param name         display name of the course
     * @param participants list of enrolled clients
     * @param capacity     maximum number of participants
     * @param ageRange     allowed age range for participants
     * @param schedule     timing details of the course
     * @param category     activity category (e.g. Yoga, Football)
     * @param description  descriptive summary
     * @param businessId   ID of the business offering this course
     * @param duration     duration in minutes
     */
    public Course(String id, CourseType type, String name, ArrayList<Client> participants, int capacity,
                  AgeRange ageRange, Schedule schedule, Category category, String description,
                  String businessId, int duration)
    {
        super(id);
        this.type = type;
        this.name = name;
        this.participants = participants;
        this.capacity = capacity;
        this.ageRange = ageRange;
        this.schedule = schedule;
        this.category = category;
        this.description = description;
        this.businessId = businessId;
        this.duration = duration;
        this.businessId = "";
    }

    /**
     * Simplified constructor that initializes an empty participant list and omits businessId.
     *
     * @param id          unique course ID
     * @param type        format of the course
     * @param name        display name
     * @param capacity    maximum participants
     * @param ageRange    allowed age range
     * @param duration    duration in minutes
     * @param schedule    timing details
     * @param category    activity category
     * @param description descriptive summary
     */
    public Course(String id, CourseType type, String name, int capacity, AgeRange ageRange,
                  int duration, Schedule schedule, Category category, String description)
    {
        super(id);
        this.type = type;
        this.name = name;
        this.participants = new ArrayList<>();
        this.capacity = capacity;
        this.ageRange = ageRange;
        this.schedule = schedule;
        this.category = category;
        this.description = description;
        this.duration = duration;
        this.businessId = "";
    }

    /**
     * Shallow copy constructor.
     *
     * @param other the Course to copy
     */
    public Course(Course other)
    {
        super(other.getId());
        this.type = other.type;
        this.businessId = other.businessId;
        this.name = other.name;
        this.participants = other.participants;
        this.capacity = other.capacity;
        this.ageRange = other.ageRange;
        this.schedule = other.schedule;
        this.category = other.category;
        this.description = other.description;
        this.duration = other.duration;
    }

    /**
     * @return the course type (Solo, Duo, Group)
     */
    public CourseType getType()
    {
        return type;
    }

    /**
     * @return the course name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return list of enrolled clients
     */
    public ArrayList<Client> getParticipants()
    {
        return participants;
    }

    /**
     * @return maximum participant capacity
     */
    public int getCapacity()
    {
        return capacity;
    }

    /**
     * @return allowed age range
     */
    public AgeRange getAgeRange()
    {
        return ageRange;
    }

    /**
     * @return timing schedule
     */
    public Schedule getSchedule()
    {
        return schedule;
    }

    /**
     * @return activity category
     */
    public Category getCategory()
    {
        return category;
    }

    /**
     * @return course description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return ID of the business offering this course
     */
    public String getBusinessId()
    {
        return businessId;
    }

    /**
     * @return duration in minutes
     */
    public int getDuration()
    {
        return duration;
    }

    /**
     * @param type the course type to set
     */
    public void setType(CourseType type)
    {
        this.type = type;
    }

    /**
     * @param name the course name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param participants the participant list to set
     */
    public void setParticipants(ArrayList<Client> participants)
    {
        this.participants = participants;
    }

    /**
     * @param capacity the capacity to set
     */
    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @param ageRange the age range to set
     */
    public void setAgeRange(AgeRange ageRange)
    {
        this.ageRange = ageRange;
    }

    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(Category category)
    {
        this.category = category;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param businessId the business ID to set
     */
    public void setBusinessId(String businessId)
    {
        this.businessId = businessId;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    /**
     * Attempts to add a participant if capacity allows.
     *
     * @param client the client to enroll
     * @return true if added, false if full
     */
    public boolean insertParticipant(Client client)
    {
        if (participants.size() == capacity)
            return false;

        participants.add(client);
        return true;
    }

    /**
     * Removes a participant.
     *
     * @param client the client to remove
     * @return true if removed, false otherwise
     */
    public boolean removeParticipant(Client client)
    {
        return participants.remove(client);
    }

    /**
     * Deserializes a Course from a Parcel.
     *
     * @param in the Parcel to read from
     */
    protected Course(Parcel in)
    {
        super(in);
        name = in.readString();
        participants = in.createTypedArrayList(Client.CREATOR);
        capacity = in.readInt();
        schedule = in.readParcelable(Schedule.class.getClassLoader());
        description = in.readString();
        ageRange = in.readParcelable(AgeRange.class.getClassLoader());
        category = Category.valueOf(in.readString());
        type = CourseType.valueOf(in.readString());
        businessId = in.readString();
        duration = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeTypedList(participants);
        dest.writeInt(capacity);
        dest.writeParcelable(schedule, flags);
        dest.writeString(description);
        dest.writeParcelable(ageRange, flags);
        dest.writeString(String.valueOf(category));
        dest.writeString(String.valueOf(type));
        dest.writeString(businessId);
        dest.writeInt(duration);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    /** Parcelable factory for Course. */
    public static final Creator<Course> CREATOR = new Creator<Course>()
    {
        @Override
        public Course createFromParcel(Parcel in)
        {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size)
        {
            return new Course[size];
        }
    };

    /**
     * Equality based on all core fields.
     * @param o other object
     * @return true if equal
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return capacity == course.capacity && duration == course.duration && type == course.type && Objects.equals(businessId, course.businessId) && Objects.equals(name, course.name) && Objects.equals(participants, course.participants) && Objects.equals(ageRange, course.ageRange) && Objects.equals(schedule, course.schedule) && category == course.category && Objects.equals(description, course.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, businessId, name, participants, capacity, ageRange, schedule, category, description, duration);
    }

    /** Oldest-first comparator by schedule occurrence. */
    public static final Comparator<Course> BY_OCCURRENCE =
            Comparator.comparing(course -> course.getSchedule().getOccurrence());

    /** Newest-first comparator by schedule occurrence. */
    public static final Comparator<Course> BY_OCCURRENCE_REVERSED =
            BY_OCCURRENCE.reversed();
}
