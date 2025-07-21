package com.example.workout;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;

import android.content.Context;
import android.location.Geocoder;

import androidx.test.core.app.ApplicationProvider;

import Model.Address;
import Model.City;
import Model.Location;
import Model.Repository.GeneralRepository;

@RunWith(JUnit4.class)
public class GeneralRepositoryTest
{
    private static Context  context;
    private static GeneralRepository generalRepository;
    @BeforeClass
    public static void setUp()
    {
        context = ApplicationProvider.getApplicationContext();
        generalRepository = new GeneralRepository();
    }

    @Test
    public void convertAddressToLocationTest()
    {
        City city = new City("ראש העין");
        Address address = new Address(city, "חיים הרצוג 24");
        Geocoder geocoder = new Geocoder(context, new Locale("he", "IL"));
        Location location = generalRepository.convertAddressToLocation(geocoder, address);

        assertEquals(32.082, location.getLatitude(), 0.01);
        assertEquals(34.97, location.getLongitude(), 0.01);
    }
}
