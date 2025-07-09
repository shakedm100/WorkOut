package Model;

public enum Gender
{
    Male,
    Female;

    /**
     * Case-insensitive lookup of a Gender by name.
     * @param gender the string to convert
     * @return the matching Gender
     * @throws IllegalArgumentException if no match is found or input is null
     */
    public static Gender fromString(String gender) {
        if (gender == null) {
            throw new IllegalArgumentException("gender must not be null");
        }
        String normalized = gender.trim();
        for (Gender g : values()) {
            if (g.name().equalsIgnoreCase(normalized)) {
                return g;
            }
        }
        throw new IllegalArgumentException("No enum constant Gender." + gender);
    }
}
