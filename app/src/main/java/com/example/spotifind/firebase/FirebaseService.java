package com.example.spotifind.firebase;

import static android.content.ContentValues.TAG;

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

    public void getFcmToken(String userId, final FcmTokenCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users/" + userId + "/fcmToken");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fcmToken = dataSnapshot.getValue(String.class);
                callback.onTokenReceived(fcmToken);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Error al obtener el token FCM", databaseError.toException());
            }
        });
    }

    public interface FcmTokenCallback {
        void onTokenReceived(String fcmToken);
    }

}