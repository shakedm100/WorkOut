package Model;

import androidx.annotation.Nullable;

public enum Category
{
    Bicycle,
    Boxing,
    MartialArts,
    Badminton,
    Baseball,
    Bridge,
    Judo,
    Golf,
    IceHockey,
    Wrestling,
    Taekwondo,
    Climbing,
    Tennis,
    Soccer,
    Handball,
    Basketball,
    Volleyball,
    BeachVolleyball,
    Fencing,
    Squash,
    Football,
    Karate,
    Archery,
    Rugby,
    Swimming;


    /**
     * Try to map a free‐form string into one of the enum constants.
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
