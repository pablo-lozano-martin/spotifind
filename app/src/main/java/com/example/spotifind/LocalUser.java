package com.example.spotifind;

import android.location.Location;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Artist;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.util.Pair;

public class LocalUser {
    private String username;
    private String uid;
    private String spotitoken;

    private List<Pair<String, String>> top5Artists;
    private Track lastPlayedSong;
    private List<Track> top5Songs;

    private FirebaseUser currentUser;

    private SpotifyAppRemote mspotifyAppRemote;

    private String email;

    private Subscription<PlayerState> subscription;

    private List<LocalUser> friendList;

    private Location location;

    // Constructor de la clase

    public LocalUser() {
        friendList = new ArrayList<>();

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLocation(Location location){
        this.location=location;
        updateLocation(this.location);
    }

    public Location getLocation(Location location){
        return this.location;
    }

    public String getSpotitoken() {
        return spotitoken;
    }

    public Track getLastPlayedSong() {
        return lastPlayedSong;
    }
    public void setSpotitoken(String token) {
        this.spotitoken=token;
    }

    public void setLastPlayedSong(Track lastPlayedSong) {
        this.lastPlayedSong = lastPlayedSong;
        // Get DatabaseReference for user's object in Firebase
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.getUid());

        // Update last played song in Firebase
        databaseRef.child("lastPlayedSong").setValue(lastPlayedSong);
        Log.d("LocalUser", "Last played song updated in Firebase");
    }

    public List<Track> getTop5Songs() {
        return top5Songs;
    }

    public void setTop5Songs(List<Track> top5Songs) {
        this.top5Songs = top5Songs;
    }

    public List<Pair<String, String>> getTop5Artists() {
        return top5Artists;
    }

    public void setTop5Artists(List<Pair<String, String>> top5Artist) {
        this.top5Artists = top5Artist;
        saveToFirebase(this.uid);

    }

    // Save user data to Firebase Realtime Database
    public void saveToFirebase(String userId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
        databaseRef.child(userId).setValue(this);
        Log.d("LocalUser", "User saved to Firebase Realtime Database");
        readFromFirebase();
    }

    public void setFirebaseCredentials(FirebaseAuth mAuth) {
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        this.email = currentUser.getEmail();
        this.uid = currentUser.getUid();
        //save for first time
        this.saveToFirebase(this.uid);
        Log.d("LocalUser", "Firebase credentials set");
    }

    public void updateCurrentSong() {
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
                        // Update user's top 5 songs
                        updateTop5Songs(track);
                    }
                });
    }


    private <TrackSimplified> void updateTop5Songs(Track track) {
        // Get current top 5 songs
        List<Track> currentTop5Songs = getTop5Songs();
        // If user has no top 5 songs yet, initialize the list
        if (currentTop5Songs == null) {
            currentTop5Songs = new ArrayList<>();
        }
        // Add the newly played song to the list
        currentTop5Songs.add(track);
        // Keep only the top 5 songs
        currentTop5Songs = currentTop5Songs.subList(0, Math.min(currentTop5Songs.size(), 5));
        // Set the updated top 5 songs list
        setTop5Songs(currentTop5Songs);
    }

    public void updateTop5Artists() {
        // Use OkHttp library to make HTTP request to Spotify API
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=short_term&limit=5")
                .addHeader("Authorization", "Bearer " + getSpotitoken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse response body to get top 5 artists
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray items = json.getJSONArray("items");
                        List<Pair<String, String>> topArtists = new ArrayList<>();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject artistObj = items.getJSONObject(i);
                            String name = artistObj.getString("name");
                            String imageUrl = artistObj.getJSONArray("images").getJSONObject(2).getString("url");
                            topArtists.add(new Pair<>(name, imageUrl));
                        }
                        // Set updated top 5 artists list
                        setTop5Artists(topArtists);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setSpoifyAppRemote(SpotifyAppRemote mSpotifyAppRemote) {
        mspotifyAppRemote=mSpotifyAppRemote;
        updateCurrentSong();
    }

    public void readFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LocalUser user = dataSnapshot.getValue(LocalUser.class);
                if (user != null) {
                    Log.d("LocalUser", "Username: " + user.getUsername());
                    Log.d("LocalUser", "Top 5 artists: " + user.getTop5Artists());
                    Log.d("LocalUser", "Top 5 songs: " + user.getTop5Songs());
                    Log.d("LocalUser", "Last played song: " + user.getLastPlayedSong());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("LocalUser", "Failed to read value.", error.toException());
            }
        });
    }

    // Escuchar los cambios de ubicación del usuario y actualizar la interfaz de usuario
    public void listenForLocationChanges(ValueEventListener listener) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        databaseRef.addValueEventListener(listener);
    }

    // Actualizar la ubicación del usuario en tiempo real en Firebase Realtime Database
    public void updateLocation(Location location) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        //databaseRef.child("latitude").setValue(location.getLatitude());
        //databaseRef.child("longitude").setValue(location.getLongitude());
        Log.d("LocalUser", "Location " +location);
    }
}