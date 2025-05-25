package Model;

public class AgeRange
{
    private int minAge;
    private int maxAge;

    public AgeRange() {}
    public AgeRange(int minAge, int maxAge)
    {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
}
