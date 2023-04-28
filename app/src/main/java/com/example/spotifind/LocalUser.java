package com.example.spotifind;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.spotifind.Spotify.CustomArtist;
import com.example.spotifind.Spotify.CustomTrack;
import com.example.spotifind.Spotify.SpotifyService;
import com.example.spotifind.Spotify.SpotifyUriService;
import com.example.spotifind.friendlist.FriendlistActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private String profileImageurl;

    public LocalUser(){
    }

    public LocalUser(DataSnapshot dataSnapshot, Context context) {
        this.context = context;

        if (dataSnapshot.hasChild("uid")) {
            setUsername(dataSnapshot.child("uid").getValue(String.class));
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
            setFcmToken(dataSnapshot.child("spotifyUri").getValue(String.class));
        }

        if (dataSnapshot.hasChild("profileImageUrl")) {
            setFcmToken(dataSnapshot.child("profileImageUrl").getValue(String.class));
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
            //saveSpotifyAccountUriToFirebase(spotitoken);
        initializeMyDataFromCache();
        initializeTopArtistsAndSongs();
    }

    public LocalUser(Context context, String uid,String spotitoken) {
        this.context = context;
        context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        this.spotitoken = spotitoken;
        if(spotitoken!=null)
            //saveSpotifyAccountUriToFirebase(spotitoken);
        if (uid != null) {
            this.uid = uid;
            initializeOtherFromCache();
        } else {
            this.uid = getDataFromCache(context, "user_id");
            initializeMyDataFromCache();
        }

        initializeTopArtistsAndSongs();
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
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
        String jsonData = new Gson().toJson(spotifyUri);
        saveDataToCache(this.context, "spotifyUri", jsonData);
    }


    public String imageUrl() {
        return profileImageurl;
    }

    public void setimageUrl(String imageUrl) {
        this.profileImageurl = imageUrl;
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
    private void initializeMyDataFromCache() {

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

        if (profileImageUrl != null) {
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
            this.friendList = cachedFriendList;
        }

        if(spotifyUri!=null){
            this.spotifyUri = spotifyUri;
        }

        if(profileImageUrl!=null){
            this.profileImageurl = profileImageUrl;
        }

        if (username == null || fcmToken == null || uid == null || spotifyUri==null || profileImageUrl==null) {
            getDataFromFirebase();
        }
    }


    private void initializeOtherFromCache() {

        List<LocalUser> cachedFriendList = getFriendListFromCache(context);
        String username = getDataFromCache(context, "username");
        String email = getDataFromCache(context, "email");
        String fcmToken = getDataFromCache(context, "fcmToken");
        String profileImageUrl = getDataFromCache(context, "profileImageUrl");
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

        if (cachedFriendList != null) {
            this.friendList = cachedFriendList;
        }

        if(spotifyUri!=null){
            this.spotifyUri = spotifyUri;
        }

        if(profileImageUrl!=null){
            this.profileImageurl = spotifyUri;
        }

        if (username == null || fcmToken == null || uid == null|| profileImageUrl==null) {
            getDataFromFirebase();
        }
    }

    private void getDataFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(getDataFromCache(context,"user_id"));
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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

                    if(profileImageurl == null && localUser.imageUrl()!= null){
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
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.uid);

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
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(this.uid);

        if (top5Artists != null) {
            // Update Top 5 Artists in Firebase
            databaseRef.child("top5Artists").setValue(top5Artists);
            Log.d("LocalUser", "Top5Artists updated in Firebase");
        } else {
            Log.w("LocalUser", "Cannot update Top5Artists in Firebase, top5Artists is null");
        }
    }

   /* private void saveAccountUriToFirebase(String userId) {
        // Obtener una referencia a la base de datos de Firebase
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Guardar el URI de la cuenta de Spotify en Firebase (puedes cambiar la estructura según tus necesidades)
        DatabaseReference userRef = database.child("users").child(userId).child("Spotyuri");
        String accountUri = userId;
        userRef.setValue(accountUri).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("LocalUser", "Spotify account URI saved in Firebase");
                    setSpotifyUri(accountUri);
                } else {
                    Log.e("LocalUser", "Error saving Spotify account URI to Firebase", task.getException());
                }
            }
        });
    }

    private void saveSpotifyAccountUriToFirebase(String accessToken) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LocalUser", "Error getting spotify account uri", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String userId = jsonObject.getString("id");
                        saveAccountUriToFirebase(userId);
                    } catch (JSONException e) {
                        // Manejar el error
                    }
                } else {
                    // Manejar el error
                }
            }
        });
    }*/
}