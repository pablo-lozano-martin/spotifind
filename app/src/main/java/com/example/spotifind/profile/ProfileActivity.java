package com.example.spotifind.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private String uid;
    private LocalUser user;
    private BottomNavigationView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        uid = getIntent().getStringExtra("user_id");
        saveDataToCache(this, "profile_uid",uid);
        LocalUser user= new LocalUser(this);
        setInterface(user);
    }

    private void setInterface(LocalUser user){
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
        textNickname.setText(user.getUsername());


        // Configurar el adaptador y el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        CardAdapter ArtistCardAdapter = new CardAdapter( user.getTop5Artists(),null, this);
        CardAdapter SongsCardAdapter = new CardAdapter(null,user.getTop5Songs(),this);
            recyclerView.setAdapter(SongsCardAdapter);

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


    private void saveDataToCache(Context context, String key, String jsonData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, jsonData);
        editor.apply();
    }
}
