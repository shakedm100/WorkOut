package com.example.workout;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;
import static com.google.android.gms.tasks.Tasks.await;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class ClientRepositoryTest
{
    private ClientRepository repository;
    private Client testSubject;

    @Before
    public void setUp()
    {
        repository = new ClientRepository();
    }

    @Test
    public void insertClientTest() throws ExecutionException, InterruptedException, TimeoutException
    {
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("2","Rosh Ha'Ayin"), "Haim Hertzog");
        testSubject = await(repository.insertClient("test", "1234", phone, "test@gmail.com", "Shaked",
                "Michael", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername())), 10, TimeUnit.SECONDS);

        assertNotNull(testSubject);
    }
}
