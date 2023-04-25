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
import com.google.common.reflect.TypeToken;
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

import java.lang.reflect.Type;
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

    private String fcmToken;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSpotitoken() {
        return spotitoken;
    }

    public void setSpotitoken(String spotitoken) {
        this.spotitoken = spotitoken;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser = currentUser;
    }

    public SpotifyAppRemote getMspotifyAppRemote() {
        return mspotifyAppRemote;
    }

    public void setMspotifyAppRemote(SpotifyAppRemote mspotifyAppRemote) {
        this.mspotifyAppRemote = mspotifyAppRemote;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Subscription<PlayerState> getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription<PlayerState> subscription) {
        this.subscription = subscription;
    }

    public void setFriendList(List<LocalUser> friendList) {
        this.friendList = friendList;
    }

    public List<LocalUser> getNearUsers() {
        return nearUsers;
    }

    public void setNearUsers(List<LocalUser> nearUsers) {
        this.nearUsers = nearUsers;
    }

    public List<String> getNotification() {
        return notification;
    }

    public void setNotification(List<String> notification) {
        this.notification = notification;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LocalUser(Context context, String uid, String spotitoken) {

        friendList = new ArrayList<>();
        nearUsers = new ArrayList<>();
        this.context = context;
        this.spotitoken=spotitoken;
        setUid(uid);
        // Cargar los datos del usuario desde Firebase


        String topArtistsJson = getDataFromCache(context, "TOP_ARTISTS");
        String topTracksJson = getDataFromCache(context, "TOP_TRACKS");

        if (topArtistsJson != null && topTracksJson != null) {
            Type artistListType = new TypeToken<List<CustomArtist>>(){}.getType();
            top5Artists = new Gson().fromJson(topArtistsJson, artistListType);
            Type trackListType = new TypeToken<List<CustomTrack>>(){}.getType();
            top5Songs = new Gson().fromJson(topTracksJson, trackListType);
        }

        else {
            SpotifyService artistSpotifyService = new SpotifyService(
                    spotitoken,
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
                    spotitoken,
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

    public void addFriend(String userId){
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
        Log.d("LocalUser", "User saved to Firebase Realtime Database");
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


}