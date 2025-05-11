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

    /**
     * @return the numeric prefix, e.g. "050"
     */
    public String getCode()
    {
        return code;
    }
}
