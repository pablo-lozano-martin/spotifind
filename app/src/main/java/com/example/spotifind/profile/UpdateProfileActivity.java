package com.example.spotifind.profile;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.spotifind.R;

public class UpdateProfileActivity extends AppCompatActivity {

    private Button updateEmailButton;
    private Button updatePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        updateEmailButton = findViewById(R.id.update_email_button);
        updatePasswordButton = findViewById(R.id.update_password_button);

        updateEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class));
            }
        });

        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdateProfileActivity.this, UpdatePasswordActivity.class));
            }
        });
    }
}
