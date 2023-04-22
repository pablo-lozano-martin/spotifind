package com.example.spotifind.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.spotifind.LocalUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseService {

    private DatabaseReference mDatabase;
    private LocalUser user;


    public FirebaseService() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void startRealtimeUpdates(String userId) {
        mDatabase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Actualizar la información del usuario y de su música en tiempo real
                user = snapshot.getValue(LocalUser.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Error al obtener los datos del usuario", error.toException());
            }
        });
    }
}