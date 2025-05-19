package Model;

import java.util.ArrayList;

public class Course extends Entity
{
    private CourseType courseType;
    private String name;
    private ArrayList<Client> participants;
    private int capacity;
    private AgeRange ageRange;
    private Schedule occurrence;
    private Category category;
    private String description;

    public Course(int id, CourseType courseType, String name, ArrayList<Client> participants, int capacity,
                  AgeRange ageRange, Schedule schedule, Category category, String description)
    {
        super(id);
        this.courseType = courseType;
        this.name = name;
        this.participants = participants;
        this.capacity = capacity;
        this.ageRange = ageRange;
        this.occurrence = schedule;
        this.category = category;
        this.description = description;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Client> getParticipants() {
        return participants;
    }

    public int getCapacity() {
        return capacity;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public Schedule getSchedule() {
        return occurrence;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
