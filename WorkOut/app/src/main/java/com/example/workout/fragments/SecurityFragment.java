package com.example.workout.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workout.R;

public class SecurityFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security, container, false);

        EditText usernameEdit = view.findViewById(R.id.editUsername);
        EditText passwordEdit = view.findViewById(R.id.editPassword);
        EditText confirmPasswordEdit = view.findViewById(R.id.editConfirmPassword);
        Button saveButton = view.findViewById(R.id.saveSecurityButton);

        saveButton.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString();
            String confirmPassword = confirmPasswordEdit.getText().toString();

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Update username and password in your data source (e.g. Firebase, Room)
            Toast.makeText(getContext(), "Details updated", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}

