package com.example.spotifind.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifind.CardAdapter;
import com.example.spotifind.LocalUser;
import com.example.spotifind.NavigationBarListener;
import com.example.spotifind.R;
import com.example.spotifind.Spotify.CustomArtist;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView textNickname;
    private ImageView userImage;
    private ImageButton editButton;
    private Button spotifyButton;
    private RecyclerView recyclerView;
    private BottomNavigationView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        textNickname = findViewById(R.id.textNickname);
        userImage = findViewById(R.id.imageView);
        editButton = findViewById(R.id.buttonEdit);
        spotifyButton = findViewById(R.id.buttonSpotify);
        recyclerView = findViewById(R.id.recyclerViewTop5Artists);

        navBar = findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.friendlist);
        NavigationBarListener navigationBarListener = new NavigationBarListener(this,userId);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);

        // Lista de usuarios para el carrusel, reemplazar con datos reales
        List<CustomArtist> artistList = new ArrayList<>();

        // Configurar el adaptador y el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        CardAdapter cardAdapter = new CardAdapter(artistList, null, this);
        recyclerView.setAdapter(cardAdapter);

        FirebaseUser currentUser = LocalUser.getCurrentUser();
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra("username", currentUser.getDisplayName());
        profileIntent.putExtra("userImage", currentUser.getPhotoUrl());
        startActivity(profileIntent);

        // Listener botón editar el perfil
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NUEVA VISTA Y ACTIVIDAD PARA EDITAR LOS DATOS?
            }
        });

        // Listener botón Spotify
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // REDIRIGIR A SPOTIFY
            }
        });
    }
}
