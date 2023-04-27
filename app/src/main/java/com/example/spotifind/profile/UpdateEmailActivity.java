package com.example.spotifind.profile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotifind.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UpdateEmailActivity extends AppCompatActivity {

    private EditText newEmailEditText;
    private Button updateEmailSubmitButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        mAuth = FirebaseAuth.getInstance();
        newEmailEditText = findViewById(R.id.new_email_edittext);
        updateEmailSubmitButton = findViewById(R.id.update_email_submit_button);

        updateEmailSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = newEmailEditText.getText().toString().trim();
                if (!newEmail.isEmpty()) {
                    mAuth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateEmailActivity.this, "Correo electrónico actualizado", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UpdateEmailActivity.this, "Error al actualizar el correo electrónico", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(UpdateEmailActivity.this, "Por favor, introduce un correo electrónico válido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
