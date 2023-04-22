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

    private List<LocalUser> friendList;

    private List<LocalUser> nearUsers;

    private Location currentLocation;

    Context context;

    // Constructor de la clase

    public LocalUser() {
    }

    public LocalUser(Context context, String mAccessToken, FirebaseAuth mAuth) {
        friendList = new ArrayList<>();
        nearUsers = new ArrayList<>();
        this.context = context;
        this.setFirebaseCredentials(mAuth);
        // Cargar los datos del usuario desde Firebase
        loadUserDataFromFirebase(uid);

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLocation(Location location) {
        this.currentLocation = location;
        updateLocation(this.currentLocation);
    }

    public Location getLocation(Location location) {
        return this.currentLocation;
    }

    public String getSpotitoken() {
        return spotitoken;
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
        readFromFirebase();
    }

    public void setFirebaseCredentials(FirebaseAuth mAuth) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
                    }

                });
    }

    public void setSpoifyAppRemote(SpotifyAppRemote mSpotifyAppRemote) {
        mspotifyAppRemote = mSpotifyAppRemote;
        updateCurrentSong();
    }

    public void readFromFirebase() {
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
    public void listenForLocationChanges(ValueEventListener listener) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        databaseRef.addValueEventListener(listener);
    }

    // Actualizar la ubicación del usuario en tiempo real en Firebase Realtime Database
    public void updateLocation(Location location) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        //databaseRef.child("latitude").setValue(location.getLatitude());
        //databaseRef.child("longitude").setValue(location.getLongitude());
        Log.d("LocalUser", "Location " + location);
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

    public void getNearUsers(double radiusInMeters) {
        DatabaseReference userLocationsRef = FirebaseDatabase.getInstance().getReference("userLocations");
        GeoFire geoFire = new GeoFire(userLocationsRef);

        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()),
                radiusInMeters / 1000 // Convertir metros a kilómetros
        );

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // key es el uid del usuario cercano
                // Obtén la información adicional del usuario desde el nodo "users" y añádelo a nearUsers
                getUserInfoAndAddToNearUsers(key);
                //updateNearUsersOnMap();
            }

            @Override
            public void onKeyExited(String key) {
                // Un usuario salió del área de búsqueda
                removeUserFromNearUsers(key);
                //updateNearUsersOnMap();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // La ubicación de un usuario cercano se actualizó
                //updateUserLocationInNearUsers(key, location);
                //updateNearUsersOnMap();
            }

            @Override
            public void onGeoQueryReady() {
                // La consulta geoespacial ha finalizado, y todos los eventos iniciales han sido activados
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("LocalUser", "Error en la consulta geoespacial", error.toException());
            }
        });

    }

    private void getUserInfoAndAddToNearUsers(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LocalUser user = dataSnapshot.getValue(LocalUser.class);
                if (user != null) {
                    // Agrega el usuario a la lista de usuarios cercanos
                    nearUsers.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LocalUser", "Error al obtener información de usuario", databaseError.toException());
            }
        });
    }


//    public void updateNearUsersOnMap(GoogleMap map) {
//        // Limpiar el mapa
//        map.clear();
//
//        // Iterar sobre la lista de usuarios cercanos
//        for (LocalUser user : nearUsers) {
//            // Obtener la ubicación del usuario
//            Location userLocation = user.getCurrentLocation();
//
//            // Crear un marcador en el mapa para el usuario
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .position(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
//                    .title(user.getUsername())
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon)); // Personalizar el icono del marcador según tus preferencias
//
//            // Agregar el marcador al mapa
//            map.addMarker(markerOptions);
//        }

//        // Actualizar el mapa
//        map.invalidate();
//    }

    private void removeUserFromNearUsers(String key) {
        for (LocalUser user : nearUsers) {
            if (user.getUid().equals(key)) {
                nearUsers.remove(user);
                //updateNearUsersOnMap();
                return;
            }
        }
    }

    private void updateUserLocationInNearUsers(String key, Location location) {
        for (LocalUser user : nearUsers) {
            if (user.getUid().equals(key)) {
                user.setLocation(location);
                //updateNearUsersOnMap();
                return;
            }
        }
    }

    public void loadUserDataFromFirebase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Actualizar los datos del usuario con los valores obtenidos de Firebase
                username = snapshot.child("username").getValue(String.class);
                uid = snapshot.child("uid").getValue(String.class);
                spotitoken = snapshot.child("spotitoken").getValue(String.class);
                // Actualizar los datos de la música del usuario
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
                currentLocation = new Location("");
                currentLocation.setLatitude(snapshot.child("latitude").getValue(Double.class));
                currentLocation.setLongitude(snapshot.child("longitude").getValue(Double.class));
                // Actualizar la lista de amigos
                GenericTypeIndicator<List<String>> friendListType = new GenericTypeIndicator<List<String>>() {
                };
                List<String> friendUids = snapshot.child("friendList").getValue(friendListType);
                friendList.clear();
                for (String friendUid : friendUids) {
                    // Cargar los datos de cada amigo desde Firebase y agregarlos a la lista de amigos
                    loadUserDataFromFirebase(friendUid);
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