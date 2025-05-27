package com.example.workout;

import static com.google.android.gms.tasks.Tasks.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalTime;
import java.util.ArrayList;
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
        Address address = new Address(new City("2", "Rosh Ha'Ayin"), "Haim Hertzog");
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
        ArrayList<Client> participants = new ArrayList<>();
        participants.add(testHelper);
        ArrayList<Client> followers = new ArrayList<>();
        Rating rating = new Rating("1234", 4.9F,"Terribly amazing", testHelper);
        ArrayList<Rating> ratings = new ArrayList<>();
        ratings.add(rating);
        followers.add(testHelper);
        Schedule schedule = new Schedule("bla", Day.Sunday, Timestamp.now());
        Course course = new Course("bla", CourseType.Dou, "TRX", participants, 50, new AgeRange(23,50),
                schedule, Category.Archery, "Shoot to kill");
        ArrayList<Course> courses = new ArrayList<>();
        courses.add(course);

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
        assertEquals("EasyBusy", testSubject.getName());
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
}
