package com.example.workout.espresso;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.workout.AddOrEditClassActivity;
import com.example.workout.R;
import com.example.workout.SearchActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;

import static org.mockito.ArgumentMatchers.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Repository.CourseRepository;
import Model.Schedule;
import Model.SearchStrategies.SearchStrategyInterface;

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest
{
    @Rule
    public ActivityScenarioRule<SearchActivity> activityRule =
            new ActivityScenarioRule<>(SearchActivity.class);

    @Test
    public void searchUITest()
    {
        Schedule schedule = new Schedule(Day.Monday, Timestamp.now());
        AgeRange age = new AgeRange(18, 65);
        Course course = new Course("1234", CourseType.Group, "Yoga Basics", 60,
                age, 120, schedule, Category.Archery, "Yoga basics for beginners");

        List<Course> courseList = new ArrayList<>();
        courseList.add(course);

        CourseRepository fakeRepo = mock(CourseRepository.class);
        when(fakeRepo.searchByStrategy(
                any(SearchStrategyInterface.class),
                any(Object.class)
        )).thenReturn(Tasks.forResult(courseList));

        activityRule.getScenario().onActivity(act ->
        {
            act.setTestingConditions(fakeRepo, null);
        });

        onView(withId(R.id.categorySearchSpinner)).perform(ViewActions.click());
        onData(allOf(is(instanceOf(String.class)), is("Archery")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.typeSpinner)).perform(ViewActions.click());
        onData(allOf(is(instanceOf(String.class)), is("Group")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.dayOfWeekSearchSpinner)).perform(ViewActions.click());
        onData(allOf(is(instanceOf(String.class)), is("Monday")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.toggle_list)).perform(ViewActions.click());

        onView(withId(R.id.searchButton)).perform(ViewActions.click());

        onView(withId(R.id.searchTestTextView)).check(matches(withText(containsString("Successfully searching and showing list"))));
    }
}
