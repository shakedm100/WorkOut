package Model;

public class Client extends User {
    private String firstName;
    private String lastName;
    private Address address;
    private Gender gender;

    public Client(String id, String username, String password, Phone phone, String email,
                  String firstName, String lastName, Address address, Gender gender)
    {
        super(id, username, phone, email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.gender = gender;
    }

    // getters and setters

    public String getFirstName()
    {
        return firstName;
    }


    
}
