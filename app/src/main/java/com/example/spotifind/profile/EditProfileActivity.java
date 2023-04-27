package com.example.spotifind.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotifind.R;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private EditText nicknameEditText;
    private EditText emailEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        profileImage = findViewById(R.id.profileImage);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);

      // MODIFICA LA CLASE USER ??

    }


}

