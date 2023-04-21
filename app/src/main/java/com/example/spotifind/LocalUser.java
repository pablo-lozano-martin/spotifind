package com.example.spotifind;

import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.example.spotifind.Spotify.SpotifyService;
import com.example.spotifind.Spotify.SpotifyUriService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;

import com.example.spotifind.Spotify.CustomArtist;
import com.example.spotifind.Spotify.CustomTrack;

public class LocalUser {
    private SpotifyService artistSpotifyService=null;
    private SpotifyService trackSpotifyService=null;
    private SpotifyService spotifyService;
    private SpotifyUriService spotifyUriService=null;
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

    private List<LocalUser> friendList;

    private Location location;

    // Constructor de la clase

    public LocalUser() {
    }

    public LocalUser(String mAccessToken, FirebaseAuth mAuth) {
        friendList = new ArrayList<>();
        this.setFirebaseCredentials(mAuth);

        SpotifyService artistSpotifyService = new SpotifyService(
                MainActivity.mAccessToken,
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
                MainActivity.mAccessToken,
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
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("LocalUser", "Error al obtener las URIs de las imágenes de los artistas", throwable);
            }
        });
        artistImageUriService.execute(new Pair<>("artists", artistIds));
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
            }
            @Override
            public void onFailure(Throwable throwable) {
                Log.e("LocalUser", "Error al obtener las URIs de las imágenes de los álbumes", throwable);
            }
        });
        albumImageUriService.execute(new Pair<>("tracks", trackIds));
    }


        public void addFriend (LocalUser friend){
            friendList.add(friend);
        }

        public void removeFriend (LocalUser friend){
            friendList.remove(friend);
        }

        public List<LocalUser> getFriendList () {
            return friendList;
        }


        public String getUsername () {
            return username;
        }

        public void setUsername (String username){
            this.username = username;
        }

        public String getUid () {
            return uid;
        }

        public void setUid (String uid){
            this.uid = uid;
        }

        public void setLocation (Location location){
            this.location = location;
            updateLocation(this.location);
        }

        public Location getLocation (Location location){
            return this.location;
        }

        public String getSpotitoken () {
            return spotitoken;
        }

        public Track getLastPlayedSong () {
            return lastPlayedSong;
        }

        public void setLastPlayedSong (Track lastPlayedSong){
            this.lastPlayedSong = lastPlayedSong;
            // Get DatabaseReference for user's object in Firebase
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.getUid());
            // Update last played song in Firebase
            databaseRef.child("lastPlayedSong").setValue(lastPlayedSong);
            Log.d("LocalUser", "Last played song updated in Firebase");
        }

        public List<CustomTrack> getTop5Songs () {
            return top5Songs;
        }

        public List<CustomArtist> getTop5Artists () {
            return top5Artists;
        }

        public void setTop5Songs (List < CustomTrack > top5Songs) {
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

        public void setTop5Artists (List < CustomArtist > top5Artists) {
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
        public void saveToFirebase (String userId){
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
            databaseRef.child(userId).setValue(this);
            Log.d("LocalUser", "User saved to Firebase Realtime Database");
            readFromFirebase();
        }

       public void setFirebaseCredentials (FirebaseAuth mAuth){
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            this.email = currentUser.getEmail();
            this.uid = currentUser.getUid();
            //save for first time
            this.saveToFirebase(this.uid);
            Log.d("LocalUser", "Firebase credentials set");
        }

        public void updateCurrentSong () {
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

        public void setSpoifyAppRemote (SpotifyAppRemote mSpotifyAppRemote){
            mspotifyAppRemote = mSpotifyAppRemote;
            updateCurrentSong();
        }

        public void readFromFirebase () {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.getUid());
            Log.d("UID:", uid);
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    LocalUser user = dataSnapshot.getValue(LocalUser.class);
                    if (user != null) {
                        Log.d("LocalUser", "Username: " + user.getUsername());

                        List<CustomArtist> top5Artists = user.getTop5Artists();
                        if (top5Artists != null) {
                            for (CustomArtist artist : top5Artists) {
                                Log.d("LocalUser", "Top 5 artists: " + artist.getName());
                            }
                        } else {
                            Log.d("LocalUser", "Top 5 artists: null");
                        }

                        List<CustomTrack> top5Songs = user.getTop5Songs();
                        if (top5Songs != null) {
                            for (CustomTrack song : top5Songs) {
                                Log.d("LocalUser", "Top 5 songs: " + song.getName());
                            }
                        } else {
                            Log.d("LocalUser", "Top 5 songs: null");
                        }

                        Track lastPlayedSong = user.getLastPlayedSong();
                        if (lastPlayedSong != null) {
                            Log.d("LocalUser", "Last played song: " + lastPlayedSong.name);
                        } else {
                            Log.d("LocalUser", "Last played song: null");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("LocalUser", "Failed to read value.", error.toException());
                }
            });
        }

        // Escuchar los cambios de ubicación del usuario y actualizar la interfaz de usuario
        public void listenForLocationChanges (ValueEventListener listener){
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            databaseRef.addValueEventListener(listener);
        }

        // Actualizar la ubicación del usuario en tiempo real en Firebase Realtime Database
        public void updateLocation (Location location){
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            //databaseRef.child("latitude").setValue(location.getLatitude());
            //databaseRef.child("longitude").setValue(location.getLongitude());
            Log.d("LocalUser", "Location " + location);
        }


    }