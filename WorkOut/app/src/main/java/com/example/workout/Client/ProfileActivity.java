package com.example.workout.Client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.workout.LoginActivity;
import com.example.workout.MainActivity;
import com.example.workout.MessagesActivity;
import com.example.workout.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import Model.Client;
import ViewModel.ClientProfileViewModel;

public class ProfileActivity extends AppCompatActivity
{

    private ClientProfileViewModel clientProfileViewModel;
    private TextView textViewName, textViewEmail;
    private EditText editTextName, editTextEmail;
    private Button buttonSaveChanges;
    private ProgressBar progressBarProfile;
    private BottomNavigationView bottomNavigationView;
    Client current;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        current = getIntent().getParcelableExtra("client");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button logout = findViewById(R.id.clientLogoutButton);
        logout.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("client", current);
            startActivity(intent);
        });

        // DVIR

        Button messagesButton = findViewById(R.id.notificationsButton);
        messagesButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, MessagesActivity.class);
            intent.putExtra("client", current);
            startActivity(intent);
        });

        Button accountButton = findViewById(R.id.accountButton);
        accountButton.setOnClickListener(task ->
        {
            Intent intent = new Intent(this, UpdateClientActivity.class);
            intent.putExtra("client", current);
            startActivity(intent);
        });

        //

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        TextView userLabel = findViewById(R.id.userName);
        userLabel.setText(current.getUsername());

        setUpBottomNavigationView();
    }
    private void setUpBottomNavigationView()
    {
        bottomNavigationView.setOnItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_home)
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_profile)
            {
                // you’re already here
                return true;
            }
            else if (id == R.id.nav_search)
            {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_map)
            {
                Intent intent = new Intent(this, MapActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_Calendar)
            {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("client", current);
                startActivity(intent);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    // <- remove this when uncomment
        /*
        // Initialize Views
        textViewFirstName = findViewById(R.id.firstNameText);
        textViewLastName = findViewById(R.id.lastNameText);

        textViewEmail = findViewById(R.id.emailText);
        buttonSaveChanges = findViewById(R.id.);
        progressBarProfile = findViewById(R.id.progressBarProfile);

        // Get current client ID (e.g., from Intent)
        String currentClientId = getIntent().getStringExtra("CLIENT_ID");
        // For testing, you might hardcode it initially
        //currentClientId = "someClientId";


        // Initialize ViewModel
        ClientRepository clientRepository = new ClientRepository(); // Use DI or a proper factory
        //ViewModelFactory factory = new ViewModelFactory(getApplication(), clientRepository);
        //clientProfileViewModel = new ViewModelProvider(this, factory).get(ClientProfileViewModel.class);
        clientProfileViewModel = new ViewModelProvider(this).get(ClientProfileViewModel.class);

        setupObservers();
        setupClickListeners();

        // Fetch initial profile data
        if (currentClientId != null && !currentClientId.isEmpty()) {
            clientProfileViewModel.fetchClientProfile(currentClientId);
        }
    }
    private void setupObservers()
    {
        clientProfileViewModel.getProfileDataState().observe(this, profileDataState -> {
            switch (profileDataState.getStatus())
            {
                case IDLE:
                    progressBarProfile.setVisibility(View.GONE);
                    buttonSaveChanges.setEnabled(true);
                    break;
                case LOADING:
                    progressBarProfile.setVisibility(View.VISIBLE);
                    buttonSaveChanges.setEnabled(false);
                    break;
                case SUCCESS:
                    progressBarProfile.setVisibility(View.GONE);
                    buttonSaveChanges.setEnabled(true);
                    // Update UI with profile data
                    //Client client = profileDataState.getData();
                    Toast.makeText(ProfileActivity.this, "Update Successful!", Toast.LENGTH_LONG).show();
                    // navigate to home activity when success occurs
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class); // move to home page
                    startActivity(intent);
                    finish(); // Optional: finish ProfileActivity so user can't go back
                    break;
                case ERROR:
                    progressBarProfile.setVisibility(View.GONE);
                    buttonSaveChanges.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, profileDataState.getErrorMessage(), Toast.LENGTH_LONG).show();
                    break;

            }
        });
    }

    private void setupClickListeners()
    {
        buttonSaveChanges.setOnClickListener(v -> {
            String username = ed.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Basic validation
            if (username.isEmpty()) {
                editTextUsername.setError("Username cannot be empty");
                return;
            }
            if (password.isEmpty()) {
                editTextPassword.setError("Password cannot be empty");
                return;
            }

            // Call the ViewModel method
            clientProfileViewModel.updateClientProfile(username, password);
        });
    }

         */
}