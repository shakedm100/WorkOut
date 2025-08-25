package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.Course;
import Model.CourseType;
import Model.Location;
import Model.SearchStrategies.SearchAgeStrategy;
import Model.SearchStrategies.SearchCategoryStrategy;
import Model.SearchStrategies.SearchCourseTypeStrategy;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

public class SearchStrategyTest
{
    SearchStrategyInterface searchStrategy;


    @Test
    public void searchAgeRangeTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchAgeStrategy();

        AgeRange ageRange = new AgeRange(10, 70);
        @SuppressWarnings("unchecked")
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.searchBusinesses(ageRange)
                , 10, TimeUnit.SECONDS);
        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        @SuppressWarnings("unchecked")
        ArrayList<Course> courses = (ArrayList<Course>) await(searchStrategy.searchCourses(ageRange)
                , 10, TimeUnit.SECONDS);

        assertNotNull(courses);
        assertFalse(courses.isEmpty());
    }

    @Test
    public void searchCategoryTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchCategoryStrategy();

        @SuppressWarnings("unchecked")
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.searchBusinesses(Category.Baseball)
                , 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        @SuppressWarnings("unchecked")
        ArrayList<Course> courses = (ArrayList<Course>) await(searchStrategy.searchCourses(Category.Baseball)
                , 10, TimeUnit.SECONDS);

        assertNotNull(courses);
        assertFalse(courses.isEmpty());
    }

    @Test
    public void searchCourseTypeTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchCourseTypeStrategy();

        @SuppressWarnings("unchecked")
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.searchBusinesses(CourseType.Group)
                , 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        @SuppressWarnings("unchecked")
        ArrayList<Course> courses = (ArrayList<Course>) await(searchStrategy.searchCourses(CourseType.Group)
                , 10, TimeUnit.SECONDS);

        assertNotNull(courses);
        assertFalse(courses.isEmpty());
    }

    @Test
    public void searchRadiusTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchRadiusStrategy(10);

        @SuppressWarnings("unchecked")
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy
                .searchBusinesses(new Location(32.08, 34.7)), 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        @SuppressWarnings("unchecked")
        ArrayList<Course> courses = (ArrayList<Course>) await(searchStrategy.searchCourses(new Location(32.08, 34.7))
                , 10, TimeUnit.SECONDS);

        assertNotNull(courses);
        assertFalse(courses.isEmpty());
    }

    @Test
    public void searchDayOfWeekTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        // TODO: Implement day of week test
    }
}