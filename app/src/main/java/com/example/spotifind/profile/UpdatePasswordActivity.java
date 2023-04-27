package com.example.spotifind.profile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotifind.R;
import com.google.firebase.auth.FirebaseAuth;

public class UpdatePasswordActivity extends AppCompatActivity {

    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button updatePasswordSubmitButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        mAuth = FirebaseAuth.getInstance();
        currentPasswordEditText = findViewById(R.id.current_password_edittext);
        newPasswordEditText = findViewById(R.id.new_password_edittext);
        confirmNewPasswordEditText = findViewById(R.id.confirm_new_password_edittext);
        updatePasswordSubmitButton = findViewById(R.id.update_password_submit_button);

        updatePasswordSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = currentPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

                if (!currentPassword.isEmpty() && !newPassword.isEmpty() && !confirmNewPassword.isEmpty()) {
                    if (newPassword.equals(confirmNewPassword)) {
                        mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), currentPassword)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        mAuth.getCurrentUser().updatePassword(newPassword)
                                                .addOnCompleteListener(updateTask -> {
                                                    if (updateTask.isSuccessful()) {
                                                        Toast.makeText(UpdatePasswordActivity.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(UpdatePasswordActivity.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(UpdatePasswordActivity.this, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(UpdatePasswordActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpdatePasswordActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

