package com.example.spotifind.radar;

import com.example.spotifind.MainActivity;
import com.example.spotifind.firebase.FirebaseService;
import com.example.spotifind.notifications.*;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.spotifind.LocalUser;
import com.example.spotifind.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomInfoWindowAdapter extends DialogFragment {
    private LocalUser user;

    public CustomInfoWindowAdapter(LocalUser user) {
        this.user = user;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_info_window, null);

        String title = user.getLastPlayedSong().name;
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
            openSpotifySong(user.getLastPlayedSong().uri);
        });

        Button sendFriendRequestButton = view.findViewById(R.id.send_friend_request_button);
        sendFriendRequestButton.setOnClickListener(v -> {
            sendFriendRequestNotification(user.getUid());
        });


        builder.setView(view);
        return builder.create();
    }

    private void sendFriendRequestNotification(String localUserId) {
        // Obtén el token FCM del usuario local desde la base de datos de Firebase Realtime
        MainActivity.mFirebaseService.getFcmToken(localUserId, receiverFcmToken -> {
            // Configura la notificación con el título, el mensaje y la información adicional necesaria.
            String senderUid = "EMISOR_UID";
            String title = "Solicitud de amistad";
            String body = "¡Tienes una nueva solicitud de amistad!";

            // Llama al método sendFcmNotification() de la clase FcmSender
            FcmSender.sendFcmNotification(receiverFcmToken, title, body,senderUid);
        });
    }



    // Envía la notificación utilizando un servicio web, como Retrofit o Volley.
        // Consulta la documentación de FCM para obtener información sobre cómo hacer esto:
        // https://firebase.google.com/docs/cloud-messaging/http-server-ref



    private void openSpotifySong(String song) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(song));
        intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + getActivity().getPackageName()));
        startActivity(intent);
    }
}
