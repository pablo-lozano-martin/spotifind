package com.example.spotifind;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    // Declarar las variables correspondientes a los campos de texto y el botón
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginButton;

    private Button mSignUpButton;

    private FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Inicializar la interfaz de usuario
        setInterface();
    }

    private void setInterface() {
        // Obtener las referencias a los campos de texto y el botón en el layout
        setContentView(R.layout.login);
        mEmailField = findViewById(R.id.etEmail);
        mPasswordField = findViewById(R.id.etPass);
        mLoginButton = findViewById(R.id.btnLogin);
        mSignUpButton = findViewById(R.id.btnSignUpRedirect);
        // Establecer el listener para el botón de login
        mLoginButton.setOnClickListener(v -> {
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            signIn(email, password);
        });

        mSignUpButton.setOnClickListener(v -> {
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            signIn(email, password);
        });

        mSignUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

    }

    private void signIn(String email, String password) {
        // Verificar que los campos de correo electrónico y contraseña no estén vacíos
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar sesión con correo electrónico y contraseña en Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // finalizar la actividad actual
                    } else {
                        // Si el inicio de sesión falla, mostrar un mensaje al usuario
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}