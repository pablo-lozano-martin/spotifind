package com.example.spotifind.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifind.CardAdapter;
import com.example.spotifind.LocalUser;
import com.example.spotifind.R;
import com.example.spotifind.Spotify.CustomArtist;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String ARG_IS_PRIVATE_PROFILE = "isPrivateProfile";
    private static final String ARG_USER_ID = "USER_ID";

    private boolean isPrivateProfile;

    private TextView textNickname;
    private ImageView userImage;
    private ImageButton editButton;
    private RecyclerView recyclerView;

    private LocalUser user;
    private String uid;
    private BottomNavigationView navBar;
    private ImageButton spotifyButton;


    public ProfileFragment() {
        // Constructor vacío requerido
    }

    public static ProfileFragment newInstance(boolean isPrivateProfile, String uid) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_PRIVATE_PROFILE, isPrivateProfile);
        args.putString(ARG_USER_ID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPrivateProfile = getArguments().getBoolean(ARG_IS_PRIVATE_PROFILE);
            uid= getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.publicprofile, container, false);
        String token = getTokenFromCache(getContext());
        if(isPrivateProfile)
            user = new LocalUser(getContext(),token);
        else{
            user= new LocalUser(getContext(),uid,token);
        }
        setInterface(view, user);
        return view;
    }

    private void setInterface(View view, LocalUser user) {

        textNickname = view.findViewById(R.id.textNickname);
        userImage = view.findViewById(R.id.imageView);
        editButton = view.findViewById(R.id.buttonEdit);
        spotifyButton = view.findViewById(R.id.buttonSpotify);
        recyclerView = view.findViewById(R.id.recyclerViewTop5Artists);
        Button buttonTopSongs = view.findViewById(R.id.buttonTopSongs);
        Button buttonTopArtists = view.findViewById(R.id.buttonTopArtists);


        // Lista de artistas para el carrusel, reemplazar con datos reales
        List<CustomArtist> artistList = new ArrayList<>();

        textNickname.setText(user.getUsername());

        // Configurar el adaptador y el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        CardAdapter artistCardAdapter = new CardAdapter(user.getTop5Artists(), null, getActivity());
        CardAdapter songsCardAdapter = new CardAdapter(null, user.getTop5Songs(), getActivity());
        recyclerView.setAdapter(songsCardAdapter); // Por defecto, muestra las canciones

        buttonTopSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(songsCardAdapter);
            }
        });

        buttonTopArtists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(artistCardAdapter);
            }
        });


        // Listener botón Spotify
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // REDIRIGIR A SPOTIFY
            }
        });

        if (isPrivateProfile) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Abre la actividad para editar perfil
                    Intent intent = new Intent(getActivity(), com.example.spotifind.settings.SettingsActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            editButton.setVisibility(View.GONE);
        }

    }

    private String getTokenFromCache(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }
}
