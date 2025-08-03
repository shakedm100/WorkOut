package Model;

import androidx.annotation.Nullable;

/**
 * Defines how a course is delivered: solo, duo, or group.
 */
public enum CourseType
{
    Solo,
    Duo,
    Group;

    /**
     * Attempts to parse a free-form string into a CourseType.
     *
     * @param s input string
     * @return matching CourseType or null if none found
     */
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
