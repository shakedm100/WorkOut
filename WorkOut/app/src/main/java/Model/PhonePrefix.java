package Model;

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
     * @return the numeric prefix, e.g. "050"
     */
    public String getCode()
    {
        return code;
    }
}
