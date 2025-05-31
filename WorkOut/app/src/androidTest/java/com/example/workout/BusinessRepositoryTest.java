package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Model.Address;
import Model.AgeRange;
import Model.Business;
import Model.Category;
import Model.City;
import Model.Client;
import Model.Course;
import Model.CourseType;
import Model.Day;
import Model.Gender;
import Model.Location;
import Model.Phone;
import Model.PhonePrefix;
import Model.Rating;
import Model.Repository.BusinessRepository;
import Model.Schedule;
import Model.SearchStrategies.SearchAgeStrategy;
import Model.SearchStrategies.SearchCategoryStrategy;
import Model.SearchStrategies.SearchCourseTypeStrategy;
import Model.SearchStrategies.SearchDateStrategy;
import Model.SearchStrategies.SearchRadiusStrategy;
import Model.SearchStrategies.SearchStrategyInterface;

@RunWith(AndroidJUnit4.class)
public class BusinessRepositoryTest
{
    private static BusinessRepository repository;
    private static Business testSubject;
    private static Client testHelper;

    @BeforeClass
    public static void setUp()
    {
        repository = new BusinessRepository();
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("Rosh Ha'Ayin"), "Haim Hertzog");
        testHelper = new Client("esdrg","shakedm100", "1234", phone,
                "shaked1mi@gmail.com", "Shaked","Michael", address, Gender.Male);

        try
        {
            // Just to add more robustness to testing, even if something failed it
            // still attempts to delete the previous testEverything subject
            Business testBusiness = await(repository.getBusinessByUsername("testEverything"), 10, TimeUnit.SECONDS);
            await(repository.deleteBusiness(testBusiness), 10, TimeUnit.SECONDS);
        }
        catch (Exception e) { /*User doesn't exist in db, good!*/ }
    }

    @AfterClass
    public static void setDown() throws ExecutionException, InterruptedException, TimeoutException
    {
        // All database calls need to await because if we don't it just exits the testEverything
        // without finishing executing the function
        await(repository.deleteBusiness(testSubject), 30, TimeUnit.SECONDS);
    }

    @Test
    public void queryBusinessTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        // Test insert
        testSubject = await(repository.insertBusiness("testEverything", "1234", testHelper.getPhone(),
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy"), 10, TimeUnit.SECONDS);

        assertNotNull(testSubject);
        assertEquals("testEverything", testSubject.getUsername());
        assertEquals("1234", testSubject.getPassword());
        assertEquals(testHelper.getPhone(), testSubject.getPhone());
        assertEquals("testEverything@mail", testSubject.getEmail());
        assertEquals(new Location(12345, 2145435), testSubject.getLocation());
        assertEquals("EasyBusy", testSubject.getBusinessName());
        assertEquals("Policy", testSubject.getPolicy());

        // Insert fail by username
        assertThrows(Exception.class, () -> await(repository.insertBusiness("testEverything", "1234", testHelper.getPhone(),
                "bla@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy"), 10, TimeUnit.SECONDS));

        // Insert fail by email
        assertThrows(Exception.class, () -> await(repository.insertBusiness("bla", "1234", testHelper.getPhone(),
                "testEverything@mail", "EasyBusy", new Location(12345, 2145435),
                "Policy"), 10, TimeUnit.SECONDS));

        // Test get by username
        Business check = await(repository.getBusinessByUsername(testSubject.getUsername()), 10, TimeUnit.SECONDS);
        assertNotNull(check);
        assertEquals(check.getId(), testSubject.getId());

        // Test update
        Boolean checkUpdate = await(repository.updateBusiness(testSubject), 10, TimeUnit.SECONDS);
        assertNotNull(checkUpdate);
        assertTrue(checkUpdate);
    }

    @Test
    public void genericSearchTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        SearchStrategyInterface searchStrategy;

        searchStrategy = new SearchAgeStrategy();

        AgeRange ageRange = new AgeRange(10,70);
        ArrayList<Business> businesses = (ArrayList<Business>) await(searchStrategy.searchBusinesses(ageRange)
                , 10, TimeUnit.SECONDS);
        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        searchStrategy = new SearchCategoryStrategy();

        businesses = (ArrayList<Business>) await(searchStrategy.searchBusinesses(Category.Baseball)
                , 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        searchStrategy = new SearchCourseTypeStrategy();

        businesses = (ArrayList<Business>) await(searchStrategy.searchBusinesses(CourseType.Group)
                , 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        searchStrategy = new SearchRadiusStrategy(10);

        businesses = (ArrayList<Business>) await(searchStrategy
                .searchBusinesses(new Location(32.08, 34.7)), 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());

        searchStrategy = new SearchDateStrategy();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2025);
        calendar.set(Calendar.MONTH, 3); // Months are from 0 - 11
        calendar.set(Calendar.DAY_OF_MONTH, 23); // Wednesday

        // Set min time at 12:00
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date minDate = calendar.getTime();
        Timestamp minTime = new Timestamp(minDate);

        // Set max time at 13:00
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date maxDate = calendar.getTime();
        Timestamp maxTime = new Timestamp(maxDate);

        Timestamp[] times = new Timestamp[]{minTime, maxTime};

        businesses = (ArrayList<Business>) await(searchStrategy
                .searchBusinesses(times), 10, TimeUnit.SECONDS);

        assertNotNull(businesses);
        assertFalse(businesses.isEmpty());
    }
}
