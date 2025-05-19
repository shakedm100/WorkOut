package Model;

public class Phone {
    private PhonePrefix prefix;
    private String number;

    public Phone(PhonePrefix phonePrefix, String number)
    {
        this.prefix = phonePrefix;
        this.number = number;
    }
}
