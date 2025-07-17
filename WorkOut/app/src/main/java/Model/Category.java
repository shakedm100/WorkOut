package Model;

import androidx.annotation.Nullable;

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
