package com.example.workout.espresso;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;

import static org.hamcrest.CoreMatchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.workout.Business.AddOrEditClassActivity;
import com.example.workout.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;

import static org.mockito.ArgumentMatchers.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Repository.CourseRepository;
import Model.Schedule;


@RunWith(AndroidJUnit4.class)
public class AddOrEditClassActivityTest
{

    @Rule
    public ActivityScenarioRule<AddOrEditClassActivity> activityRule =
            new ActivityScenarioRule<>(AddOrEditClassActivity.class);

    @Test
    public void addingCourseTest_success()
    {
        Schedule schedule = new Schedule(Day.Monday, Timestamp.now());
        AgeRange age = new AgeRange(18, 65);
        Course course = new Course("1234", CourseType.Group, "Yoga Basics", 60,
                age, 120, schedule, Category.Archery, "Yoga basics for beginners");

        CourseRepository fakeRepo = mock(CourseRepository.class);
        when(fakeRepo.insertCourse(
                any(Business.class),     // business
                anyString(),             // name
                any(Schedule.class),     // schedule
                anyInt(),                // capacity
                eq(CourseType.Group),    // type
                any(AgeRange.class),     // ageRange
                eq(Category.Archery),    // category
                anyString(),             // description
                anyInt()                 // duration
        )).thenReturn(Tasks.forResult(course));

        activityRule.getScenario().onActivity(act ->
        {
            act.setTestingConditions(fakeRepo, null);
        });

        onView(withId(R.id.courseNameEditText)).perform(ViewActions.typeText("Yoga Basics"),
                closeSoftKeyboard());

        onView(withId(R.id.courseTypeSpinner)).perform(click());

        // From all the data, pick the data that is equal to "Group"
        onData(allOf(is(instanceOf(String.class)), is("Group")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.categorySpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Archery")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.dayOfWeekSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Monday")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.capacityEditText)).perform(ViewActions.
                typeText("60"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.descriptionEditText)).perform(ViewActions.
                typeText("Yoga basics for beginners"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.buttonSave)).perform(ViewActions.click());

        onView(withId(R.id.addOrEditTestTextView))
                .check(matches(withText(containsString("Successfully added the course"))));
    }

    @Test
    public void updateCourseTest_success()
    {
        Schedule currentSchedule = new Schedule(Day.Monday, Timestamp.now());
        AgeRange age = new AgeRange(18, 65);
        Course currentCourse = new Course("1234", CourseType.Group, "Yoga Basics", 60,
                age, 120, currentSchedule, Category.Archery, "Yoga basics for beginners");

        CourseRepository fakeRepo = mock(CourseRepository.class);
        when(fakeRepo.updateCourse(
                any(Course.class)
                ,any(Business.class)     // business
        )).thenReturn(Tasks.forResult(true));

        activityRule.getScenario().onActivity(act ->
        {
            act.setTestingConditions(fakeRepo, currentCourse);
        });

        onView(withId(R.id.courseNameEditText)).perform(ViewActions.typeText("testUpdate"),
                closeSoftKeyboard());

        onView(withId(R.id.courseTypeSpinner)).perform(click());

        // From all the data, pick the data that is equal to "Group"
        onData(allOf(is(instanceOf(String.class)), is("Group")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.categorySpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Archery")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.dayOfWeekSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Sunday")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.capacityEditText)).perform(ViewActions.
                typeText("40"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.descriptionEditText)).perform(ViewActions.
                typeText("This is a test course insert"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.buttonSave)).perform(ViewActions.click());

        onView(withId(R.id.addOrEditTestTextView))
                .check(matches(withText(containsString("Successfully updated the course"))));
    }
/*
    private void setTimePickerButtons()
    {
        onView(withId(R.id.startTimeButton)).perform(click());

        // switch to keyboard input mode
        onView(withId(material_timepicker_mode_button)).inRoot(isDialog()).perform(click());

        // enter hour “14”
        onView(withId(com.google.android.material.R.id.material_hour_text_input))
                .perform(replaceText("14"), closeSoftKeyboard());

        // enter minute “30”
        onView(withId(com.google.android.material.R.id.material_minute_text_input))
                .perform(replaceText("30"), closeSoftKeyboard());

        // confirm
        onView(withText("OK")).inRoot(isDialog()).perform(click());

        onView(withId(R.id.endTimeButton)).perform(click());

        // switch to keyboard input mode
        onView(withId(material_timepicker_mode_button)).inRoot(isDialog()).perform(click());

        // enter hour “14”
        onView(withId(com.google.android.material.R.id.material_hour_text_input))
                .perform(replaceText("16"), closeSoftKeyboard());

        // enter minute “30”
        onView(withId(com.google.android.material.R.id.material_minute_text_input))
                .perform(replaceText("00"), closeSoftKeyboard());

        // confirm
        onView(withText("OK")).inRoot(isDialog()).perform(click());
    }*/
}
