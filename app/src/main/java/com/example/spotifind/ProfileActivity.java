package com.example.spotifind;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ProfileActivity extends AppCompatActivity {

    private TextView textNickname;
    private TextView artistName;
    private ImageView userImage;
    private ImageView artistImage;
    private ImageButton editButton;
    private Button spotifyButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        textNickname = findViewById(R.id.textNickname);
        artistName = findViewById(R.id.artistName);
        userImage = findViewById(R.id.userImage);
        artistImage = findViewById(R.id.artistImage);
        editButton = findViewById(R.id.buttonEdit);
        spotifyButton = findViewById(R.id.buttonSpotify);


        textNickname.setText("Nombre usuario"); // COGER INFO DEL USUARIO DE LA CLASE USER
        artistName.setText("Nombre artista"); // INFO DE FIREBASE??
        //userImage.setImageResource(R.drawable.imagen_usurio);
        //artistImage.setImageResource(R.drawable.imagen_artista);

        //listener botón editar el perfil
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NUEVA VISTA Y ACTIVIDAD PARA EDITAR LOS DATOS?
            }
        });

        // listener botón "Spotify"
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // REDIRIGIR A SPOTI
            }
        });


    }
}
