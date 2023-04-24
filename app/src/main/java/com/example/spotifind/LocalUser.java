package com.example.spotifind;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.spotifind.Spotify.CustomArtist;
import com.example.spotifind.Spotify.CustomTrack;
import com.example.spotifind.Spotify.SpotifyService;
import com.example.spotifind.Spotify.SpotifyUriService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;

public class LocalUser {

    private String username;
    private String uid;

    private String spotitoken;
    private List<CustomArtist> top5Artists;
    private Track lastPlayedSong;
    private List<CustomTrack> top5Songs;
    private FirebaseUser currentUser;

    private SpotifyAppRemote mspotifyAppRemote;

    private String email;

    private Subscription<PlayerState> subscription;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private List<LocalUser> friendList;

    private List<LocalUser> nearUsers;

    private List<String> notification;

    private Location currentLocation;

    Context context;
    // Constructor de la clase

    public LocalUser() {
    }
    public LocalUser(Context context,String uid) {

        friendList = new ArrayList<>();
        nearUsers = new ArrayList<>();
        this.context = context;
        // Cargar los datos del usuario desde Firebase
        loadUserDataFromFirebase(uid);

        SpotifyService artistSpotifyService = new SpotifyService(
                MainActivity.getToken(),
                "artists",
                null,
                new SpotifyService.SpotifyCallback<List<CustomArtist>>() {
                    @Override
                    public void onSuccess(List<CustomArtist> result) {
                        updateTop5Artists(result);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("LocalUser", "Error al obtener los Top Artistas", throwable);
                    }
                }
        );

        SpotifyService trackSpotifyService = new SpotifyService(
                MainActivity.getToken(),
                "tracks",
                new SpotifyService.SpotifyCallback<List<CustomTrack>>() {
                    @Override
                    public void onSuccess(List<CustomTrack> result) {
                        updateTop5Tracks(result);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("LocalUser", "Error al obtener los Top Tracks", throwable);
                    }
                },
                null
        );

        artistSpotifyService.execute();
        trackSpotifyService.execute();
    }


    private void updateTop5Artists(List<CustomArtist> artists) {
        List<CustomArtist> artistPairs = new ArrayList<>();
        List<String> artistIds = new ArrayList<>();
        for (CustomArtist artist : artists) {
            artistPairs.add(new CustomArtist(artist.getId(), artist.getName()));
            artistIds.add(artist.getId());
        }

        SpotifyUriService artistImageUriService = new SpotifyUriService(new SpotifyService.SpotifyCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                for (int i = 0; i < result.size() && i < artistPairs.size(); i++) {
                    artistPairs.get(i).setImageUrl(result.get(i));
                }
                setTop5Artists(artistPairs);
                Log.d("LocalUser", "Top 5 artists updated: " + artistPairs.get(0).getName());
                String jsonData = new Gson().toJson(artistPairs);
                saveDataToCache(context, "TOP_ARTISTS", jsonData);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("LocalUser", "Error al obtener las URIs de las imágenes de los artistas", throwable);
            }
        });
        artistImageUriService.execute(new Pair<>("artists", artistIds));
    }

    public void setUid(String uid){
        this.uid=uid;
        saveToFirebase(uid);
    }

    private void updateTop5Tracks(List<CustomTrack> tracks) {
        List<CustomTrack> trackPairs = new ArrayList<>();
        List<String> trackIds = new ArrayList<>();
        for (CustomTrack track : tracks) {
            trackPairs.add(new CustomTrack(track.getId(), track.getName(), track.getUri()));
            trackIds.add(track.getId());
        }

        SpotifyUriService albumImageUriService = new SpotifyUriService(new SpotifyService.SpotifyCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                for (int i = 0; i < result.size() && i < trackPairs.size(); i++) {
                    trackPairs.get(i).setAlbumImageUrl(result.get(i));
                }
                setTop5Songs(trackPairs);
                Log.d("LocalUser", "Top 5 tracks updated: " + trackPairs.get(0).getName());
                String jsonData = new Gson().toJson(trackPairs);
                saveDataToCache(context, "TOP_TRACKS", jsonData);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("LocalUser", "Error al obtener las URIs de las imágenes de los álbumes", throwable);
            }
        });
        albumImageUriService.execute(new Pair<>("tracks", trackIds));
    }


    public void addFriend(LocalUser friend) {
        friendList.add(friend);
    }

    public void removeFriend(LocalUser friend) {
        friendList.remove(friend);
    }

    public List<LocalUser> getFriendList() {
        return friendList;
    }


    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }

    public Location getLocation() {
        return this.currentLocation;
    }

    public Track getLastPlayedSong() {
        return lastPlayedSong;
    }

    public void setLastPlayedSong(Track lastPlayedSong) {
        this.lastPlayedSong = lastPlayedSong;
        // Get DatabaseReference for user's object in Firebase
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.getUid());
        // Update last played song in Firebase
        databaseRef.child("lastPlayedSong").setValue(lastPlayedSong);
        Log.d("LocalUser", "Last played song updated in Firebase");
    }

    public List<CustomTrack> getTop5Songs() {
        return top5Songs;
    }

    public List<CustomArtist> getTop5Artists() {
        return top5Artists;
    }

    public void setTop5Songs(List<CustomTrack> top5Songs) {
        this.top5Songs = top5Songs;
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.getUid());

        if (top5Songs != null) {
            // Update Top 5 Songs in Firebase
            databaseRef.child("top5Songs").setValue(top5Songs);
            Log.d("LocalUser", "Top5Songs updated in Firebase");
        } else {
            Log.w("LocalUser", "Cannot update Top5Songs in Firebase, top5Songs is null");
        }
    }

    public void setTop5Artists(List<CustomArtist> top5Artists) {
        this.top5Artists = top5Artists;
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.getUid());

        if (top5Artists != null) {
            // Update Top 5 Artists in Firebase
            databaseRef.child("top5Artists").setValue(top5Artists);
            Log.d("LocalUser", "Top5Artists updated in Firebase");
        } else {
            Log.w("LocalUser", "Cannot update Top5Artists in Firebase, top5Artists is null");
        }
    }


    // Save user data to Firebase Realtime Database
    public void saveToFirebase(String userId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
        databaseRef.child(userId).setValue(this);
        Log.d("LocalUser", "User saved to Firebase Realtime Database");
    }

    public void addFriend(String userId){
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");

        Log.d("LocalUser", "User saved to Firebase Realtime Database");
    }

    public void setUid() {
        this.uid = currentUser.getUid();
        //save for first time

        Log.d("LocalUser", "Firebase credentials set");
    }

    public void updateCurrentSong(SpotifyAppRemote mspotifyAppRemote) {
        // Subscribe to PlayerState
        this.subscription = mspotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    Track track = playerState.track;
                    if (track != null) {
                        Log.d("LocalUser", "Now playing: " + track.name + " by " + track
                                .artist.name);
                        // Set last played song
                        setLastPlayedSong(track);
                    }

                });
    }

    public void setSpoifyAppRemote(SpotifyAppRemote mSpotifyAppRemote) {
        updateCurrentSong(mSpotifyAppRemote);
    }

    private void saveDataToCache(Context context, String key, String jsonData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SPOTIFIND_CACHE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, jsonData);
        editor.apply();
    }

    private String getDataFromCache(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SPOTIFIND_CACHE", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public void loadUserDataFromFirebase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Actualizar los datos del usuario con los valores obtenidos de Firebase
                username = snapshot.child("username").getValue(String.class);
                if(snapshot.child("uid").getValue(String.class) != null) {
                    uid = snapshot.child("uid").getValue(String.class);
                }
                GenericTypeIndicator<List<CustomArtist>> artistListType = new GenericTypeIndicator<List<CustomArtist>>() {
                };
                GenericTypeIndicator<List<CustomTrack>> trackListType = new GenericTypeIndicator<List<CustomTrack>>() {
                };
                top5Artists = snapshot.child("top5Artists").getValue(artistListType);
                top5Songs = snapshot.child("top5Songs").getValue(trackListType);

                // Actualizar la última canción reproducida
                if (snapshot.child("lastPlayedSong").exists()) {
                    Track lastTrack = snapshot.child("lastPlayedSong").getValue(Track.class);
                    if (lastTrack != null) {
                        lastPlayedSong = lastTrack;
                    }
                }

                // Actualizar la ubicación del usuario
                //currentLocation = new Location("");
                //currentLocation.setLatitude(snapshot.child("latitude").getValue(Double.class));
                //currentLocation.setLongitude(snapshot.child("longitude").getValue(Double.class));
                // Actualizar la lista de amigos
                GenericTypeIndicator<List<String>> friendListType = new GenericTypeIndicator<List<String>>() {
                };
                List<String> friendUids = snapshot.child("friendList").getValue(friendListType);
                friendList.clear();
                if (friendUids != null) {
                    for (String friendUid : friendUids) {
                        // Cargar los datos de cada amigo desde Firebase y agregarlos a la lista de amigos
                        loadUserDataFromFirebase(friendUid);
                    }
                }
                // Actualizar la imagen de perfil del usuario
                String base64Image = snapshot.child("profileImage").getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    //profileImage = Base64.decode(base64Image, Base64.DEFAULT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LocalUser", "Error al cargar los datos del usuario desde Firebase", error.toException());
            }
        });
    }

}