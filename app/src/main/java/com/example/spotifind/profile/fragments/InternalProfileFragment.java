package com.example.spotifind.profile.fragments;

import android.content.Intent;
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
import com.example.spotifind.profile.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class PrivateProfileFragment extends Fragment {

    private TextView textNickname;
    private ImageView userImage;
    private ImageButton editButton, settingsButton;
    private RecyclerView recyclerView;
    private LocalUser user;
    private String uid;

    public PrivateProfileFragment(String uid) {
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
        View view = inflater.inflate(R.layout.fragment_profile_private, container, false);
        setInterface(view, user);
        return view;
    }

    private void setInterface(View view, LocalUser user) {
        textNickname = view.findViewById(R.id.textNickname);
        userImage = view.findViewById(R.id.imageView);
        editButton = view.findViewById(R.id.buttonEdit);
        settingsButton = view.findViewById(R.id.buttonSettings);
        recyclerView = view.findViewById(R.id.recyclerViewTop5Artists);

        // Configurar la barra de navegación inferior
        BottomNavigationView navBar = view.findViewById(R.id.navbar);
        navBar.setSelectedItemId(R.id.friendlist);
        NavigationBarListener navigationBarListener = new NavigationBarListener(this, uid);
        navBar.setOnNavigationItemSelectedListener(navigationBarListener);


        CardAdapter artistCardAdapter = new CardAdapter(user.getTop5Artists(), null, getActivity());
        CardAdapter songsCardAdapter = new CardAdapter(null, user.getTop5Songs(), getActivity());
        recyclerView.setAdapter(songsCardAdapter);

        textNickname.setText(user.getUsername());

        // Listener botón editar perfil
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre la actividad para editar perfil
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
