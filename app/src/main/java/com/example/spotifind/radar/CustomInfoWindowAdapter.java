package com.example.spotifind.radar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifind.LocalUser;
import com.example.spotifind.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private final Context mContext;

    private LocalUser user;

    public CustomInfoWindowAdapter(Context context, LocalUser user) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
        this.user=user;
    }



    private void renderWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        TextView songName = view.findViewById(R.id.song_name);
        Button openSpotifyButton = view.findViewById(R.id.open_spotify_button);
        ImageView songImage = view.findViewById(R.id.song_image);
        songName.setText(user.getLastPlayedSong().name);

        String rawImageUri = user.getLastPlayedSong().imageUri.raw;
        String imageUrl = "https://i.scdn.co/image/" + rawImageUri.replace("spotify:image:", "");

        Picasso picasso = Picasso.get();
        picasso.load(imageUrl).fetch();
        picasso.load(imageUrl).priority(Picasso.Priority.HIGH).into(songImage);

        openSpotifyButton.setOnClickListener(v -> {
            // Abre la aplicación de Spotify en la canción del usuario
            String uri = "spotify:track:" + user.getLastPlayedSong().uri;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mContext.startActivity(intent);
        });
    }


    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}

