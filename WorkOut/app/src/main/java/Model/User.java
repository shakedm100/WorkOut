package Model;

public class User extends Entity {
    private String username;
    private String password;
    private Phone phone;
    private String email;

    public User() {}
    public User(String id, String username, String password, Phone phone, String email)
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }
}
