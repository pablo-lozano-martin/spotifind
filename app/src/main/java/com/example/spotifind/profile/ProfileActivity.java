package com.example.spotifind.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifind.R;
import com.example.spotifind.LocalUser;

import com.example.spotifind.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView textNickname;
    private ImageView userImage;
    private ImageButton editButton;
    private Button spotifyButton;
    private RecyclerView recyclerView;
    private UserProfileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        textNickname = findViewById(R.id.textNickname);
        userImage = findViewById(R.id.imageView);
        editButton = findViewById(R.id.buttonEdit);
        spotifyButton = findViewById(R.id.buttonSpotify);
        recyclerView = findViewById(R.id.recycler_view);

        // Lista de usuarios para el carrusel, reemplazar con datos reales
        List<LocalUser> userList = new ArrayList<>();

        // Configurar el adaptador y el RecyclerView
        adapter = new UserProfileAdapter(userList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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
