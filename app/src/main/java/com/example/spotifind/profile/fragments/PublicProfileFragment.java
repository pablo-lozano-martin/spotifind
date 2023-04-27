package com.example.spotifind.profile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifind.CardAdapter;
import com.example.spotifind.LocalUser;
import com.example.spotifind.NavigationBarListener;
import com.example.spotifind.R;
import com.example.spotifind.Spotify.CustomArtist;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class PublicProfileFragment extends Fragment {

    private TextView textNickname;
    private ImageView userImage;
    private ImageButton editButton;
    private RecyclerView recyclerView;
    private LocalUser user;
    private String uid;
    private BottomNavigationView navBar;
    private ImageButton spotifyButton;

    public PublicProfileFragment(String uid) {
        this.uid = uid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = new LocalUser(getContext(), uid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);
        setInterface(view, user);
        return view;
    }

    private void setInterface(View view, LocalUser user) {
        textNickname = view.findViewById(R.id.textNickname);
        userImage = view.findViewById(R.id.imageView);
        editButton = view.findViewById(R.id.buttonEdit);
        spotifyButton = view.findViewById(R.id.buttonSpotify);
        recyclerView = view.findViewById(R.id.recyclerViewTop5Artists);

        navBar = view.findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.friendlist);
        NavigationBarListener navigationBarListener = new NavigationBarListener(getActivity(), uid);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);

        // Lista de artistas para el carrusel, reemplazar con datos reales
        List<CustomArtist> artistList = new ArrayList<>();

        textNickname.setText(user.getUsername());

        // Configurar el adaptador y el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        CardAdapter artistCardAdapter = new CardAdapter(user.getTop5Artists(), null, getActivity());
        CardAdapter songsCardAdapter = new CardAdapter(null, user.getTop5Songs(), getActivity());
        recyclerView.setAdapter(songsCardAdapter);

        // Listener botón editar el perfil
        editButton.setVisibility(View.GONE);

        // Listener botón Spotify
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // REDIRIGIR A SPOTIFY
            }
        });
    }
}
