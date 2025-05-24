package Model;

public class Phone {
    private PhonePrefix prefix;
    private String number;

    /**
     * No-arg constructor required by Firestore’s POJO mapper
     */
    public Phone() {
        // Firestore will use setters to populate fields
    }

    public Phone(PhonePrefix phonePrefix, String number)
    {
        this.prefix = phonePrefix;
        this.number = number;
    }

    // Getter and setter for prefix
    public PhonePrefix getPrefix() {
        return prefix;
    }
    public void setPrefix(PhonePrefix prefix) {
        this.prefix = prefix;
    }

    // Getter and setter for number
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
}
