package Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Objects;

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

    public Course()
    {
    }

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

    public Course(Course other) // Shallow copy constructor
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


    public CourseType getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public ArrayList<Client> getParticipants()
    {
        return participants;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public AgeRange getAgeRange()
    {
        return ageRange;
    }

    public Schedule getSchedule()
    {
        return schedule;
    }

    public Category getCategory()
    {
        return category;
    }

    public String getDescription()
    {
        return description;
    }

    public String getBusinessId()
    {
        return businessId;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setType(CourseType type)
    {
        this.type = type;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setParticipants(ArrayList<Client> participants)
    {
        this.participants = participants;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    public void setAgeRange(AgeRange ageRange)
    {
        this.ageRange = ageRange;
    }

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }

    public void setCategory(Category category)
    {
        this.category = category;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setBusinessId(String businessId)
    {
        this.businessId = businessId;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public boolean insertParticipant(Client client)
    {
        if(participants.size() == capacity)
            return false;

        participants.add(client);
        return true;
    }

    public boolean removeParticipant(Client client)
    {
        return participants.remove(client);
    }

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
}
