package Model;

import java.util.Objects;

public enum PhonePrefix {
    PREFIX_050("050"),
    PREFIX_052("052"),
    PREFIX_053("053"),
    PREFIX_054("054"),
    PREFIX_055("055"),
    PREFIX_058("058");

    private final String code;

    PhonePrefix(String code)
    {
        this.code = code;
    }

    /**
     * Parses an exact prefix code string into the matching enum.
     *
     * @param phoneNumber the prefix string (e.g. "052")
     * @return the matching PhonePrefix, or null if none
     */
    public static PhonePrefix fromString(String phoneNumber)
    {
        if(phoneNumber == null)
            return null;

        switch (phoneNumber)
        {
            case "050":
                return PREFIX_050;
            case "052":
                return PREFIX_052;
            case "053":
                return PREFIX_053;
            case "054":
                return PREFIX_054;
            case "055":
                return PREFIX_055;
            case "058":
                return PREFIX_058;
            default:
                return null;
        }
    }

    /**
     * Infers the prefix from a full 10-digit phone number string.
     *
     * @param phoneNumber the full number (e.g. "0521234567")
     * @return the matching PhonePrefix, or null if invalid
     */
    public static PhonePrefix inferPreFix(String phoneNumber)
    {
        if(Objects.equals(phoneNumber.charAt(1), '5') && Objects.equals(phoneNumber.charAt(2), '0'))
            return PREFIX_050;
        if(Objects.equals(phoneNumber.charAt(1), '5') && Objects.equals(phoneNumber.charAt(2), '2'))
            return PREFIX_052;
        if(Objects.equals(phoneNumber.charAt(1), '5') && Objects.equals(phoneNumber.charAt(2), '3'))
            return PREFIX_053;
        if(Objects.equals(phoneNumber.charAt(1), '5') && Objects.equals(phoneNumber.charAt(2), '4'))
            return PREFIX_054;
        if(Objects.equals(phoneNumber.charAt(1), '5') && Objects.equals(phoneNumber.charAt(2), '5'))
            return PREFIX_055;
        if(Objects.equals(phoneNumber.charAt(1), '5') && Objects.equals(phoneNumber.charAt(2), '8'))
            return PREFIX_058;

        return null;
    }

    /** @return the numeric prefix code, e.g. "050" */
    public String getCode()
    {
        return code;
    }
}
