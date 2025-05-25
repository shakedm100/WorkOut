package Model;

import java.util.ArrayList;

public class Course extends Entity
{
    private CourseType courseType;
    private String name;
    private ArrayList<Client> participants;
    private int capacity;
    private AgeRange ageRange;
    private Schedule schedule;
    private Category category;
    private String description;

    public Course() {}
    public Course(String id, CourseType courseType, String name, ArrayList<Client> participants, int capacity,
                  AgeRange ageRange, Schedule schedule, Category category, String description)
    {
        super(id);
        this.courseType = courseType;
        this.name = name;
        this.participants = participants;
        this.capacity = capacity;
        this.ageRange = ageRange;
        this.schedule = schedule;
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
        return schedule;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParticipants(ArrayList<Client> participants) {
        this.participants = participants;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
