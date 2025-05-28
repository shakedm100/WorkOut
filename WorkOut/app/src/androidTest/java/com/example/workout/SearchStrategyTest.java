package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Model.AgeRange;
import Model.Business;
import Model.Category;
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

        AgeRange ageRange = new AgeRange(10,70);
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.search(ageRange)
                , 10, TimeUnit.SECONDS);
        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());
    }

    @Test
    public void searchCategoryTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchCategoryStrategy();

        @SuppressWarnings("unchecked")
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.search(Category.Baseball)
                , 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());
    }

    @Test
    public void searchCourseTypeTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchCourseTypeStrategy();

        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.search(CourseType.Group)
                , 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());
    }

    @Test
    public void searchRadiusTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        searchStrategy = new SearchRadiusStrategy(10);

        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy
                .search(new Location(32.08, 34.7)), 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());
    }
}