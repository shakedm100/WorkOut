package Model;

import androidx.annotation.Nullable;

/**
 * A category of sports or activities.
 */
public enum Category
{
    Archery,
    Athletics,
    Badminton,
    Baseball,
    Basketball,
    BeachVolleyball,
    Bicycle,
    Biathlon,
    Boxing,
    Bridge,
    Canoeing,
    Climbing,
    Cricket,
    Crossfit,
    Diving,
    Fencing,
    FigureSkating,
    Football,
    Golf,
    Gymnastics,
    Handball,
    IceHockey,
    IceSkating,
    Judo,
    Karate,
    Kayaking,
    martialArts,
    MotorSport,
    MountainBiking,
    Pilates,
    Powerlifting,
    Rugby,
    Running,
    Skateboarding,
    Skiing,
    Snowboarding,
    Soccer,
    Squash,
    Surfing,
    Swimming,
    TableTennis,
    Taekwondo,
    Tennis,
    Triathlon,
    Volleyball,
    WaterPolo,
    Weightlifting,
    Wrestling,
    Yoga,
    Parkour;


    /**
     * Attempts to map a free‐form string into a Category enum constant.
     * <p>
     * Trims whitespace and replaces spaces with underscores, then
     * looks up the matching enum. Returns null if no match.
     *
     * @param s the input string to convert
     * @return the matching Category, or null if none found
     */
    public static @Nullable Category fromString(String s)
    {
        if (s == null) return null;
        String key = s.trim().replace(" ", "_");
        try
        {
            return valueOf(key);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
