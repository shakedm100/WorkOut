package Model;

public class City extends Entity
{
    private String name;

    public City() {}

    public City(String id, String name)
    {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name)
    { this.name = name; }
}
