package Model;

/**
 * Gender enumeration with a case-insensitive lookup.
 */
public enum Gender
{
    Male,
    Female;

    /**
     * Returns the Gender matching the supplied string, ignoring case.
     *
     * @param gender the input string (must not be null)
     * @return the matching Gender constant
     * @throws IllegalArgumentException if input is null or no match found
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
