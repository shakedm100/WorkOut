package Model;

import androidx.annotation.Nullable;

public enum CourseType
{
    Solo,
    Dou,
    Group;

    /** Try to map a free‐form string into one of the enum constants. */
    public static @Nullable CourseType fromString(String s) {
        if (s == null)
            return null;
        String key = s.trim().replace(" ", "_");
        try {
            return valueOf(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
