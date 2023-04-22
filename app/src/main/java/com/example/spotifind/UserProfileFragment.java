package com.example.spotifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifind.CardAdapter;
import com.example.spotifind.R;
import com.example.spotifind.Spotify.CustomArtist;
import com.example.spotifind.Spotify.CustomTrack;
import com.spotify.protocol.types.Item;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;
    private List<Item> items;

    //@Nullable
    //@Override
    /*public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.user_profile, container, false);

        // Inicializa el RecyclerView y el adaptador
        //recyclerView = view.findViewById(R.id.recyclerView);
        items = new ArrayList<>();
        //cardAdapter = new CardAdapter(items, getContext());

        // Configura el RecyclerView
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(cardAdapter);

        // Carga los datos en la lista de items y actualiza el adaptador
        loadData();

        //return view;
    }

    private void loadData() {
        List<CustomArtist> topArtists = spotifyService.getTopArtists();
        List<CustomTrack> topTracks = spotifyService.getTopTracks();

        // Añade los elementos a la lista de 'items' y actualiza el adaptador
        items.addAll(topArtists);
        items.addAll(topTracks);
        cardAdapter.notifyDataSetChanged();
    }*/
}