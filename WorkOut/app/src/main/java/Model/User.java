package Model;

public class User extends Entity {
    private String username;
    private String password;
    private Phone phone;
    private String email;

    public User(int id, String username, String password, Phone phone, String email)
    {
        super(id);
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Phone getPhone() {
        return phone;
    }
}
