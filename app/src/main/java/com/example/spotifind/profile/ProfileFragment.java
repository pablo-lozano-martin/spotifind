package com.example.spotifind.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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
    private ImageButton spotifyButton;
    private Boolean _isPrivateProfile;

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
        setInterface(view, user, isPrivateProfile);
        return view;
    }

    private void setInterface(View view, LocalUser user, Boolean isPrivateProfile) {

        _isPrivateProfile=isPrivateProfile;
        textNickname = view.findViewById(R.id.textNickname);
        userImage = view.findViewById(R.id.imageView);
        spotifyButton= view.findViewById(R.id.buttonSpotify);
        editButton = view.findViewById(R.id.buttonEdit);
        recyclerView = view.findViewById(R.id.recyclerViewTop5Artists);
        Button buttonTopSongs = view.findViewById(R.id.buttonTopSongs);
        Button buttonTopArtists = view.findViewById(R.id.buttonTopArtists);
        Button buttonAccept = view.findViewById(R.id.aceptarButton);
        Button buttonReject = view.findViewById(R.id.rechazarButton);

        // Lista de artistas para el carrusel, reemplazar con datos reales
        List<CustomArtist> artistList = new ArrayList<>();

        textNickname.setText(user.getUsername());

        loadImageFromFirebaseStorage(user.imageUrl(), userImage);

        // Configurar el adaptador y el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        CardAdapter artistCardAdapter = new CardAdapter(user.getTop5Artists(), null, getActivity());
        CardAdapter songsCardAdapter = new CardAdapter(null, user.getTop5Songs(), getActivity());
        recyclerView.setAdapter(songsCardAdapter); // Por defecto, muestra las canciones

        spotifyButton.setOnClickListener(v -> {
            // Abrir el perfil de Spotify del usuario
            String spotifyUri = user.spotifyUri();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("spotify:user:" + spotifyUri));
            startActivity(intent);
        });

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

        if (_isPrivateProfile) {
            // Si el perfil es privado, muestra los botones de editar perfil y editar cuenta de usuario
            editButton.setVisibility(View.VISIBLE);
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonReject.setVisibility(View.INVISIBLE);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Abre la actividad para editar perfil
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            // Si el perfil es público, muestra los botones de aceptar y rechazar
            buttonAccept.setVisibility(View.VISIBLE);
            buttonReject.setVisibility(View.VISIBLE);
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Aquí se puede agregar el código para aceptar la solicitud de amistad
                }
            });
            buttonReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Aquí se puede agregar el código para rechazar la solicitud de amistad
                }
            });
        }

    }


    private void loadImageFromFirebaseStorage(String imageReference, ImageView userImage) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //Modifica la ruta de la referencia a la carpeta de perfiles
        StorageReference storageReference = storage.getReferenceFromUrl(imageReference);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(userImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                userImage.setImageResource(R.drawable.profile_icon);
            }
        });
    }


    private String getTokenFromCache(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }
}
