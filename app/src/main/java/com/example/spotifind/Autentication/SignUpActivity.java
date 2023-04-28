package com.example.spotifind.Autentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int RC_PICK_IMAGE = 1;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Views
    private EditText muserNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private Button mSignUpButton;
    private ImageView mProfileImageView;
    private Button mSelectImageButton;

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private Uri mProfileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Inicializar Firebase Authentication, Realtime Database y Storage
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

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
        mProfileImageView = findViewById(R.id.profile_image);
        mSelectImageButton = findViewById(R.id.select_image_button);

        // Establecer el listener para el botón de registro
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        // Establecer el listener para el botón de selección de imagen de perfil
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }private void signUp() {
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

        // Verificar que el nombre de usuario no está en uso
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

                            // Subir la imagen de perfil si existe
                            if (mProfileImageUri != null) {
                                uploadProfileImage(userId, mProfileImageUri, username, email);
                            } else {
                                // Si no hay imagen de perfil, crear el usuario en la base de datos sin imagen
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
                            onBackPressed();
                        } else {
                            // Si el registro falla, mostrar un mensaje al usuario
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "El registro falló.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void selectImage() {
        // Veríficar si el usuario ha otorgado permiso para acceder a la galería de imágenes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            // Si el usuario ya otorgó permiso, abrir la galería
            Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
            pickImageIntent.setType("image/*");
            startActivityForResult(pickImageIntent, RC_PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                // Si no se seleccionó una imagen, mostrar un mensaje al usuario
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener la URI de la imagen seleccionada
            mProfileImageUri = data.getData();

            // Mostrar la imagen seleccionada en el ImageView de la interfaz de usuario
            Picasso.get().load(mProfileImageUri).into(mProfileImageView);
        }
    }

    private void uploadProfileImage(final String userId, Uri uri, final String username, final String email) {
        // Crear una referencia a la carpeta de imágenes de perfil del usuario
        final StorageReference userImageRef = mStorageRef.child("profile_images").child(userId);// Subir la imagen de perfil a Firebase Storage
        userImageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Obtener la URL de descarga de la imagen de perfil
                        userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                // Crear el usuario en la base de datos en tiempo real de Firebase con la imagen de perfil
                                createUserInDatabase(userId, username, email, downloadUrl);

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Si la carga falla, mostrar un mensaje al usuario
                        Toast.makeText(SignUpActivity.this, "Error al cargar la imagen de perfil.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al cargar la imagen de perfil.", e);
                    }
                });
        }
        private void createUserInDatabase(String userId, String username, String email, String profileImageUrl) {
        // Crear un objeto Map para guardar la información del usuario
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", userId);
            userMap.put("username", username);
            userMap.put("email", email);
            if (profileImageUrl != null) {
                userMap.put("profileImageUrl", profileImageUrl);
            }// Guardar la información del usuario en la base de datos en tiempo real de Firebase
            mDatabase.child("users").child(userId).setValue(userMap);
        }
}
