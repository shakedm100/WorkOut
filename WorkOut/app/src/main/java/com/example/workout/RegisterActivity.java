package com.example.workout;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import Model.Address;
import Model.City;
import Model.Client;
import Model.Gender;
import Model.Phone;
import Model.PhonePrefix;
import Model.Repository.ClientRepository;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);

        //Button registerButton = findViewById(R.id.re); TODO: We forgot to add a register button!!
    }

    public void saveUserProfileToFirestore(String userId, String username, String email, String profilePictureUrl) {

        ClientRepository clientRepository = new ClientRepository();
        Client sessionClient = null;
        Phone phone = new Phone(PhonePrefix.PREFIX_052, "5265777");
        Address address = new Address(new City("1","Oranit"), "Hayarkon");
        clientRepository.insertClient("Alice", "1234", phone, email, "Dvir",
                "Bento", address, Gender.Male).addOnSuccessListener(client ->
                System.out.println("Hello" + client.getUsername()));


    }
}
