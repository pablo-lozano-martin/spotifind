package com.example.spotifind;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.spotifind.Spotify.CustomArtist;
import com.example.spotifind.Spotify.CustomTrack;
import com.example.spotifind.Spotify.SpotifyService;
import com.example.spotifind.profile.OnUserInitializedListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.spotify.protocol.types.Track;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalUser<SpotifyApi> {

    private static final String SPOTIFY_AUTH_TOKEN_KEY = null;

    private String username;

    private String email;

    private String fcmToken;
    private String uid;
    private List<CustomArtist> top5Artists;
    private Track lastPlayedSong;
    private List<CustomTrack> top5Songs;

    private String spotifyUri;

    private String profileImageUrl;

    public Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    private String spotitoken;

    private Context context;

    private List<LocalUser> friendList;

    private FirebaseFirestore db;

    public LocalUser(){
    }

    public LocalUser(DataSnapshot dataSnapshot, Context context) {
        this.context = context;

        if (dataSnapshot.hasChild("uid")) {
            setUid(dataSnapshot.child("uid").getValue(String.class));
        }

        if (dataSnapshot.hasChild("username")) {
            setUsername(dataSnapshot.child("username").getValue(String.class));
        }

        if (dataSnapshot.hasChild("email")) {
            setEmail(dataSnapshot.child("email").getValue(String.class));
        }

        if (dataSnapshot.hasChild("fcmToken")) {
            setFcmToken(dataSnapshot.child("fcmToken").getValue(String.class));
        }

        if (dataSnapshot.hasChild("spotifyUri")) {
            setSpotifyUri(dataSnapshot.child("spotifyUri").getValue(String.class));
            String originalUri = dataSnapshot.child("spotifyUri").getValue(String.class);
            String replacedUri = originalUri.replace(".", "-");
            if (!originalUri.equals(replacedUri)) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(getUid());
                ref.child("spotifyUri").setValue(replacedUri);
                Log.d("LocalUser", "Spotify Uri actualizado en Firebase");
            }
        }

        if (dataSnapshot.hasChild("profileImageUrl")) {
            setimageUrl(dataSnapshot.child("profileImageUrl").getValue(String.class));
        }

        if (dataSnapshot.hasChild("top5Artists")) {
            top5Artists = new ArrayList<>();
            for (DataSnapshot artistSnapshot : dataSnapshot.child("top5Artists").getChildren()) {
                CustomArtist artist = artistSnapshot.getValue(CustomArtist.class);
                top5Artists.add(artist);
            }
        }

        if (dataSnapshot.hasChild("top5Songs")) {
            top5Songs = new ArrayList<>();
            for (DataSnapshot songSnapshot : dataSnapshot.child("top5Songs").getChildren()) {
                CustomTrack track = songSnapshot.getValue(CustomTrack.class);
                top5Songs.add(track);
            }
        }

        if(dataSnapshot.hasChild("friendList")){
            friendList= new ArrayList<>();
            for (DataSnapshot songSnapshot : dataSnapshot.child("friendList").getChildren()) {
                LocalUser friend = songSnapshot.getValue(LocalUser.class);
                friendList.add(friend);
            }
        }

        getSpotifyStats();
    }


    public LocalUser(Context context, String spotitoken) {
        this.context = context;
        context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        this.spotitoken=spotitoken;
        this.uid = getDataFromCache(context, "user_id");
        if(spotitoken!=null)
            saveSpotifyAccountUriToFirebase(spotitoken);
        initializeMyDataFromCacheStart();
        initializeTopArtistsAndSongs();
    }
    public LocalUser(Context context, String spotitoken,OnUserInitializedListener listener) {
        this.context = context;
        context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        this.spotitoken=spotitoken;
        this.uid = getDataFromCache(context, "user_id");
        if(spotitoken!=null)
            saveSpotifyAccountUriToFirebase(spotitoken);
        initializeMyDataFromCache(listener);
        initializeTopArtistsAndSongs();
    }

    public LocalUser(Context context, String uid, String spotitoken, OnUserInitializedListener listener) {
        this.context = context;
        context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        this.spotitoken = spotitoken;
        if (spotitoken != null) {
            saveSpotifyAccountUriToFirebase(spotitoken);
        }
        if (uid != null) {
            this.uid = uid;
            initializeOtherFromCache(listener);
        } else {
            this.uid = getDataFromCache(context, "user_id");
            initializeMyDataFromCache(listener);
        }
    }


    private void saveSpotifyAccountUriToFirebase(String spotitoken) {

    }

    public void setUid(String uid){
        this.uid=uid;
    }

    private void updateTop5Artists(List<CustomArtist> artists) {
        List<CustomArtist> artistPairs = new ArrayList<>();

        for (CustomArtist artist : artists) {
            artistPairs.add(new CustomArtist(artist.getId(), artist.getName(), artist.getImageUrl()));
        }

        setTop5Artists(artistPairs);
        Log.d("LocalUser", "Top 5 artists updated: " + artistPairs.get(0).getName());
        String jsonData = new Gson().toJson(artistPairs);
        saveDataToCache(context, "TOP_ARTISTS", jsonData);
    }

    private void updateTop5Tracks(List<CustomTrack> tracks) {
        List<CustomTrack> trackPairs = new ArrayList<>();

        for (CustomTrack track : tracks) {
            trackPairs.add(new CustomTrack(track.getId(), track.getName(), track.getUri(), track.getImageUrl()));
        }

        setTop5Songs(trackPairs);
        Log.d("LocalUser", "Top 5 tracks updated: " + trackPairs.get(0).getName());
        String jsonData = new Gson().toJson(trackPairs);
        saveDataToCache(context, "TOP_TRACKS", jsonData);
    }

    public String getUid() {
        return uid;
    }

    private void saveDataToCache(Context context, String key, String jsonData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, jsonData);
        editor.apply();
    }

    private String getDataFromCache(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }



    private void saveOtherDataToCache(Context context,String key, String jsonData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(uid + "_" + key, jsonData);
        editor.apply();
    }

    private String getOtherDataFromCache(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString(uid + "_" + key, null);
    }

    public String getSpotifyAuthToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    public void setUsername(String username) {
        this.username = username;
        String jsonData = new Gson().toJson(username);
        saveDataToCache(this.context, "username", jsonData);
    }

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
        String jsonData = new Gson().toJson(email);
        saveDataToCache(this.context, "email", jsonData);
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        String jsonData = new Gson().toJson(fcmToken);
        saveDataToCache(this.context, "fcmToken", jsonData);
    }

    public String spotifyUri() {
        String spotifyUri = this.spotifyUri;
        if (spotifyUri != null && spotifyUri.contains("-")) {
            spotifyUri = spotifyUri.replace("-", ".");
        }
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
        String jsonData = new Gson().toJson(spotifyUri);
        saveDataToCache(this.context, "spotifyUri", jsonData);
    }


    public String imageUrl() {
        return profileImageUrl;
    }

    public void setimageUrl(String imageUrl) {
        this.profileImageUrl = imageUrl;
        String jsonData = new Gson().toJson(imageUrl);
        saveDataToCache(this.context, "profileImageUrl", jsonData);
    }

    public void setFriendList(List<LocalUser> friendList) {
        this.friendList = friendList;
        String jsonData = new Gson().toJson(friendList);
        saveDataToCache(this.context, "friendList", jsonData);
    }

    private List<LocalUser> getFriendListFromCache(Context context) {
        String jsonData = getDataFromCache(context, "friendList");
        if (jsonData != null) {
            Type friendListType = new TypeToken<List<LocalUser>>(){}.getType();
            return new Gson().fromJson(jsonData, friendListType);
        }
        return null;
    }
    private void initializeMyDataFromCache(OnUserInitializedListener listener) {

        List<LocalUser> cachedFriendList = getFriendListFromCache(context);
        String uid = getDataFromCache(context, "user_id");
        String username = getDataFromCache(context, "username");
        String profileImageUrl = getDataFromCache(context, "profileImageUrl");
        String email = getDataFromCache(context, "email");
        String fcmToken = getDataFromCache(context, "fcmToken");
        String friendListJson = getDataFromCache(context, "friendList");
        String spotifyUri = getDataFromCache(context,"spotifyUri");

        if (username != null) {
            this.username = new Gson().fromJson(username, String.class);
        }

        if (email != null) {
            this.email = new Gson().fromJson(email, String.class);
        }

        if (fcmToken != null) {
            this.fcmToken = new Gson().fromJson(fcmToken, String.class);
        }

        if (uid != null) {
            this.uid = new Gson().fromJson(uid, String.class);
        }

        if (cachedFriendList != null) {
            this.friendList = new Gson().fromJson(friendListJson, List.class);
        }

        if(spotifyUri!=null){
            this.spotifyUri = new Gson().fromJson(spotifyUri, String.class);
        }

        if(profileImageUrl!=null){
            this.profileImageUrl = new Gson().fromJson(profileImageUrl, String.class);
        }

        if (username == null || fcmToken == null || uid == null || spotifyUri==null || profileImageUrl==null) {
            getDataFromFirebase(listener);
        }

    }
    private void initializeMyDataFromCacheStart() {

        List<LocalUser> cachedFriendList = getFriendListFromCache(context);
        String uid = getDataFromCache(context, "user_id");
        String username = getDataFromCache(context, "username");
        String profileImageUrl = getDataFromCache(context, "profileImageUrl");
        String email = getDataFromCache(context, "email");
        String fcmToken = getDataFromCache(context, "fcmToken");
        String friendListJson = getDataFromCache(context, "friendList");
        String spotifyUri = getDataFromCache(context,"spotifyUri");

        if (username != null) {
            this.username = new Gson().fromJson(username, String.class);
        }

        if (email != null) {
            this.email = new Gson().fromJson(email, String.class);
        }

        if (fcmToken != null) {
            this.fcmToken = new Gson().fromJson(fcmToken, String.class);
        }

        if (uid != null) {
            this.uid = new Gson().fromJson(uid, String.class);
        }

        if (cachedFriendList != null) {
            this.friendList = new Gson().fromJson(friendListJson, List.class);
        }

        if(spotifyUri!=null){
            this.spotifyUri = new Gson().fromJson(spotifyUri, String.class);
        }

        if(profileImageUrl!=null){
            this.profileImageUrl = new Gson().fromJson(profileImageUrl, String.class);
        }

        if (username == null || fcmToken == null || uid == null || spotifyUri==null || profileImageUrl==null) {
            getDataFromFirebaseStart();
        }

    }


    private void initializeOtherFromCache(OnUserInitializedListener listener) {
        String uid = getOtherDataFromCache("uid");
        String username = getOtherDataFromCache("username");
        String email = getOtherDataFromCache("email");
        String fcmToken = getOtherDataFromCache("fcmToken");
        String profileImageUrl = getOtherDataFromCache("profileImageUrl");
        String spotifyUri = getOtherDataFromCache("spotifyUri");


        if (uid != null) {
            this.uid = new Gson().fromJson(uid, String.class);
        }

        if (uid != null) {
            this.uid = new Gson().fromJson(uid, String.class);
        }

        if (username != null) {
            this.username = new Gson().fromJson(username, String.class);
        }

        if (email != null) {
            this.email = new Gson().fromJson(email, String.class);
        }

        if (fcmToken != null) {
            this.fcmToken = new Gson().fromJson(fcmToken, String.class);
        }

        if (spotifyUri != null) {
            this.spotifyUri = new Gson().fromJson(spotifyUri, String.class);
        }

        if (profileImageUrl != null) {
            this.profileImageUrl = new Gson().fromJson(profileImageUrl, String.class);
        }

        if (username == null || fcmToken == null || uid == null || profileImageUrl == null || spotifyUri == null) {
            initializeOtherUserFromFirebase(listener);
        }
    }


    private void getDataFromFirebase(OnUserInitializedListener listener) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LocalUser localUser = new LocalUser(dataSnapshot, context);

                    if(uid== null && localUser.getUid()!=null){
                        setUid(localUser.getUid());
                    }

                    if (username == null && localUser.getUsername() != null) {
                        setUsername(localUser.getUsername());
                    }

                    if (email == null && localUser.getEmail() != null) {
                        setEmail(localUser.getEmail());
                    }

                    if (fcmToken == null && localUser.getFcmToken() != null) {
                        setFcmToken(localUser.getFcmToken());
                    }

                    if(spotifyUri == null && localUser.spotifyUri() != null){
                        setSpotifyUri(localUser.spotifyUri());
                    }

                    if(profileImageUrl == null && localUser.imageUrl()!= null){
                        setimageUrl(localUser.imageUrl());
                    }

                    List<LocalUser> cachedFriendList = getFriendListFromCache(context);
                    if (friendList == null && localUser.getFriendList() != null) {
                        setFriendList(localUser.getFriendList());
                    }
                    listener.onUserInitialized();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LocalUser", "Error al obtener datos de Firebase", databaseError.toException());
            }
        };

        databaseRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void getDataFromFirebaseStart() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LocalUser localUser = new LocalUser(dataSnapshot, context);

                    if(uid== null && localUser.getUid()!=null){
                        setUid(localUser.getUid());
                    }

                    if (username == null && localUser.getUsername() != null) {
                        setUsername(localUser.getUsername());
                    }

                    if (email == null && localUser.getEmail() != null) {
                        setEmail(localUser.getEmail());
                    }

                    if (fcmToken == null && localUser.getFcmToken() != null) {
                        setFcmToken(localUser.getFcmToken());
                    }

                    if(spotifyUri == null && localUser.spotifyUri() != null){
                        setSpotifyUri(localUser.spotifyUri());
                    }

                    if(profileImageUrl == null && localUser.imageUrl()!= null){
                        setimageUrl(localUser.imageUrl());
                    }

                    List<LocalUser> cachedFriendList = getFriendListFromCache(context);
                    if (friendList == null && localUser.getFriendList() != null) {
                        setFriendList(localUser.getFriendList());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LocalUser", "Error al obtener datos de Firebase", databaseError.toException());
            }
        };

        databaseRef.addListenerForSingleValueEvent(valueEventListener);
    }


    private void initializeOtherUserFromFirebase(OnUserInitializedListener listener) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Inicializa solo los campos deseados de otherUser con los datos obtenidos de Firebase

                    if (dataSnapshot.hasChild("username")) {
                        setUsername(dataSnapshot.child("username").getValue(String.class));
                    }

                    if (dataSnapshot.hasChild("top5Artists")) {
                        top5Artists = new ArrayList<>();
                        for (DataSnapshot artistSnapshot : dataSnapshot.child("top5Artists").getChildren()) {
                            CustomArtist artist = artistSnapshot.getValue(CustomArtist.class);
                            top5Artists.add(artist);
                        }
                    }

                    if (dataSnapshot.hasChild("top5Songs")) {
                        top5Songs = new ArrayList<>();
                        for (DataSnapshot songSnapshot : dataSnapshot.child("top5Songs").getChildren()) {
                            CustomTrack track = songSnapshot.getValue(CustomTrack.class);
                            top5Songs.add(track);
                        }
                    }
                    listener.onUserInitialized();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LocalUser", "Error al obtener datos de Firebase", databaseError.toException());
            }
        };

        databaseRef.addListenerForSingleValueEvent(valueEventListener);
    }


    private void setFriendList(String topArtistsJson) {
        Type artistListType = new TypeToken<List<CustomArtist>>(){}.getType();
        top5Artists = new Gson().fromJson(topArtistsJson, artistListType);
    }

    private List<LocalUser> getFriendList() {
        return friendList;
    }

    public String getUsername() {
        return this.username;
    }




    ////////////////////////////
    //////////Spotify Statistics
    ////////////////////////////

    private void initializeTopArtistsAndSongs() {
        String topArtistsJson = getDataFromCache(context, "TOP_ARTISTS");
        String topTracksJson = getDataFromCache(context, "TOP_TRACKS");

        if (topArtistsJson != null && topTracksJson != null) {
            Type artistListType = new TypeToken<List<CustomArtist>>(){}.getType();
            top5Artists = new Gson().fromJson(topArtistsJson, artistListType);
            Type trackListType = new TypeToken<List<CustomTrack>>(){}.getType();
            top5Songs = new Gson().fromJson(topTracksJson, trackListType);
        } else {
            getSpotifyStats();
        }
    }

    private void getSpotifyStats(){
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

  /*  public void getSpotifyUid(String token) {
        new SpotifyService(token, new SpotifyService.SpotifyCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String uid = result;

                // Reemplazar caracteres no permitidos por Firebase Realtime Database
                if (uid.contains(".") || uid.contains("$") || uid.contains("[") || uid.contains("]")) {
                    uid = uid.replaceAll("[.$\\[\\]]", "-");

                    // Agregar indicador de modificación
                    uid += "_modified";
                }

                setSpotifyUri(uid);

                // Guardar el ID de usuario en Realtime Database
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(getUid());
                ref.child("spotifyUri").setValue(uid);
            }

            @Override
            public void onFailure(Throwable throwable) {
                // Manejar el error aquí
            }
        }).execute();
    }*/



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
}