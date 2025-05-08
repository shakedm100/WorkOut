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
}
