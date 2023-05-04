package com.example.spotifind.Autentication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.spotifind.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Views
    private EditText muserNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private Button mSignUpButton;

    private DatabaseReference mDatabase;


    private static final int RC_PICK_IMAGE = 1;
    private Uri mProfileImageUri;
    private ImageView mProfileImageView;
    private Button mSelectImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Inicializar Firebase Authentication y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        // Configurar la interfaz de usuario
        setInterface();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
            } else {
                // El usuario ha denegado el permiso de almacenamiento, debes mostrar un mensaje al usuario y no acceder a la galería
            }
        }
    }


    private void setInterface() {
        // Obtener las referencias a los campos de texto y el botón en el layout
        muserNameField = findViewById(R.id.etName);
        mEmailField = findViewById(R.id.etEmail);
        mPasswordField = findViewById(R.id.etPass);
        mPasswordConfirmField = findViewById(R.id.etPass2);
        mSignUpButton = findViewById(R.id.btnSignup);
        mSelectImageButton = findViewById(R.id.select_image_button);
        mProfileImageView= findViewById(R.id.profile_image);

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

        mSelectImageButton.setOnClickListener(view -> selectImage());

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


    private void createUserWithEmailAndPassword(final String username, final String email, final String password) {
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

                            //profileImage
                            if (mProfileImageUri != null) {
                                uploadProfileImage(userId, mProfileImageUri, username, email);
                            } else {
                                createUserInDatabase(userId, username, email, null);
                            }

                            // Crear un objeto Map para guardar la información del usuario
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", userId);
                            userMap.put("username", username);
                            userMap.put("email",email);

                            // Guardar la información del usuario en la base de datos en tiempo real de Firebase
                            mDatabase.child("users").child(userId).setValue(userMap);

                            Toast.makeText(SignUpActivity.this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                            finish(); // volver a la actividad de inicio de sesión
                        } else {
                            // Si el registro falla, mostrar un mensaje al usuario
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "El registro falló.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), RC_PICK_IMAGE);
    }

    private void uploadProfileImage(final String userId, Uri profileImageUri, final String username, final String email) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference profileImageRef = storageReference.child("profile_images/" + userId + ".jpg");

        profileImageRef.putFile(profileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String profileImageUrl = uri.toString();
                            createUserInDatabase(userId, username, email, profileImageUrl);
                        }
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "Error al subir la imagen de perfil.", Toast.LENGTH_SHORT).show();
                    createUserInDatabase(userId, username, email, null);
                }
            }
        });
    }

    private void createUserInDatabase(String userId, String username, String email, @Nullable String profileImageUrl) {
        // Guardar el nombre de usuario en la base de datos en tiempo real de Firebase
        mDatabase.child("usernames").child(username).setValue(userId);

        // Crear un objeto Map para guardar la información del usuario
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", userId);
        userMap.put("username", username);
        userMap.put("email", email);

        if (profileImageUrl != null) {
            userMap.put("profileImageUrl", profileImageUrl);
        }

        // Guardar la información del usuario en la base de datos en tiempo real de Firebase
        mDatabase.child("users").child(userId).setValue(userMap);

        Toast.makeText(SignUpActivity.this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
        finish(); // volver a la actividad de inicio de sesión
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mProfileImageUri = data.getData();
            mProfileImageView.setImageURI(mProfileImageUri);
        }
    }


}
