package com.example.spotifind.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotifind.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {


    private static final String TAG = "SettingsActivity";
    private EditText editTextUsername;
    private ImageView profileImage;
    private ImageView editImageIcon;
    private Button saveButton;
    private Button cancelButton;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        editTextUsername = findViewById(R.id.nicknameEditText);
        profileImage = findViewById(R.id.profileImage);
        editImageIcon = findViewById(R.id.editImageIcon);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Añadir la imagen de perfil actual
        if (currentUser.getPhotoUrl() != null) {
            Picasso.get().load(currentUser.getPhotoUrl()).into(profileImage);
        }

        editTextUsername.setText(currentUser.getDisplayName());

        editImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsername();
                uploadImage();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            StorageReference ref = storageReference.child("images/" + FirebaseAuth.getInstance().getCurrentUser() + "/profile.jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Si la imagen se subió con éxito, actualiza el perfil con la nueva URL de la imagen
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build();
                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile image updated.");
                                            Toast.makeText(SettingsActivity.this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.w(TAG, "Error updating user profile image.", task.getException());
                                            Toast.makeText(SettingsActivity.this, "Error al actualizar la imagen de perfil", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error uploading image.", e);
                        Toast.makeText(SettingsActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUsername() {
        String newUsername = editTextUsername.getText().toString().trim();

        if (!newUsername.isEmpty()) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile username updated.");
                                Toast.makeText(SettingsActivity.this, "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "Error updating user profile username.", task.getException());
                                Toast.makeText(SettingsActivity.this, "Error al actualizar el nombre de usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(SettingsActivity.this, "Por favor, introduce un nombre de usuario válido", Toast.LENGTH_SHORT).show();
        }
    }


}
