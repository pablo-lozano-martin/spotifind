package com.example.spotifind.Autentication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotifind.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Views
    private EditText muserNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private Button mSignUpButton;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Inicializar Firebase Authentication y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configurar la interfaz de usuario
        setInterface();
    }


    private void setInterface() {
        // Obtener las referencias a los campos de texto y el botón en el layout
        muserNameField = findViewById(R.id.etName);
        mEmailField = findViewById(R.id.etEmail);
        mPasswordField = findViewById(R.id.etPass);
        mPasswordConfirmField = findViewById(R.id.etPass2);
        mSignUpButton = findViewById(R.id.btnSignup);

        // Establecer el listener para el botón de registro
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        String username = muserNameField.getText().toString();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        String passwordconfirm = mPasswordConfirmField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(passwordconfirm))
        {
            Toast.makeText(this, "Please enter password confirm", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!TextUtils.equals(password,passwordconfirm))
        {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child("usernames").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(SignUpActivity.this, "El nombre de usuario ya está en uso. Por favor, elige otro.", Toast.LENGTH_SHORT).show();
                } else {
                    createUserWithEmailAndPassword(username,email,password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SignUpActivity.this, "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });
/*
        // Crear una cuenta con correo electrónico y contraseña en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            FirebaseApp.getInstance().
                            finish(); // volver a la actividad de inicio de sesión
                        } else {
                            // Si el registro falla, mostrar un mensaje al usuario
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                            //recreate();?
                        }
                    }
                });*/
    }


    private void createUserWithEmailAndPassword(final String username,final String email,final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            Log.d(TAG, "createUserWithEmail:success");
                            String userId = mAuth.getCurrentUser().getUid();

                            // Guardar el nombre de usuario en la base de datos en tiempo real de Firebase
                            mDatabase.child("usernames").child(username).setValue(userId);
                            mDatabase.child("users").child(userId).child("username").setValue(username);

                            Toast.makeText(SignUpActivity.this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                            finish(); // volver a la actividad de inicio de sesión
                        } else {
                            // Si el registro falla, mostrar un mensaje al usuario
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "El registro falló.", Toast.LENGTH_SHORT).show();
                            //recreate();?
                        }
                    }
                });
    }
}
