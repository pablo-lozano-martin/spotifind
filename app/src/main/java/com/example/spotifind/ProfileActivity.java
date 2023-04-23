package com.example.spotifind;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*import com.example.spotifind.OnItemClickListener;
import com.example.spotifind.ArtistAdapter.OnItemClickListener;*/

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements OnItemClickListener {

    private TextView textNickname;
    private ImageView userImage;
    private ImageButton editButton;
    private Button spotifyButton;

    private RecyclerView recyclerViewTop5Artists;
    /*private ArtistAdapter artistAdapter;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        textNickname = findViewById(R.id.textNickname);
        userImage = findViewById(R.id.userImage);
        editButton = findViewById(R.id.buttonEdit);
        spotifyButton = findViewById(R.id.buttonSpotify);
        recyclerViewTop5Artists = findViewById(R.id.recyclerViewTop5Artists);

        // Cargar los artistas desde la API de Spotify y agregarlos a la lista
        List<Artist> artistList = new ArrayList<>();
        // código para obtener los artistas de la API y agregarlos a la lista

       /* artistAdapter = new ArtistAdapter(artistList, new ArtistAdapter.OnItemClickListener() {
            @Override
            public void onArtistClick(Artist artist) {
                // Abrir la página del artista en la aplicación de Spotify
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(artist.getExternalUrls().getSpotify()));
                startActivity(intent);
            }
        });*/

        // Configurar RecyclerView
        recyclerViewTop5Artists.setLayoutManager(new LinearLayoutManager(this));
        /*recyclerViewTop5Artists.setAdapter(artistAdapter);*/

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

    @Override
    public void onItemClick(int position) {
        /*Artist artist = artistList.get(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(artist.getExternalUrls().getSpotify()));
        startActivity(intent);*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // acciones específicas en vista apaisada
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // acciones específicas en vista vertical
        }
    }
}

