package Model;

public class Address
{
    private City city;
    private String name;

    public Address(){}
    public Address(City city, String name)
    {
        this.city = city;
        this.name = name;
    }

    public City getCity()
    {
        return city;
    }

    public String getName()
    {
        return name;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setName(String name) {
        this.name = name;
    }
}
