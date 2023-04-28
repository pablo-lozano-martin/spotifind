package com.example.spotifind.Spotify;

import android.os.AsyncTask;
import android.util.Log;

import com.example.spotifind.MainActivity;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyService extends AsyncTask<Void, Void, Object> {

    private static final String BASE_URL = "https://api.spotify.com/v1/";

    private final String queryType;
    private SpotifyCallback<String> userIdCallback = null;
    private final SpotifyCallback<List<CustomTrack>> callback;
    private final SpotifyCallback<List<CustomArtist>> artistCallback;

    private final String token;

    private Gson gson;

    public interface SpotifyCallback<T> {
        void onSuccess(T result);

        void onFailure(Throwable throwable);
    }

    public SpotifyService(String token, String queryType, SpotifyCallback<List<CustomTrack>> callback, SpotifyCallback<List<CustomArtist>> artistCallback) {
        this.token = token;
        this.queryType = queryType;
        this.callback = callback;
        this.artistCallback = artistCallback;
        gson = new Gson();
    }

    // Nuevo constructor para obtener el ID de usuario
    public SpotifyService(String token, SpotifyCallback<String> userIdCallback) {
        this.token = token;
        this.userIdCallback = userIdCallback;
        this.queryType = "user_id";
        this.callback = null;
        this.artistCallback = null;
        gson = new Gson();
    }

    @Override
    protected Object doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();

        if (queryType.equals("user_id")) {
            Request request = new Request.Builder()
                    .url(BASE_URL + "me")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    UserResponse userResponse = gson.fromJson(responseString, UserResponse.class);
                    return userResponse.getId();
                } else {
                    throw new IOException("Unexpected HTTP response: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Request request = new Request.Builder()
                    .url(BASE_URL + "me/top/" + queryType + "?limit=5")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();
            Log.d("request:", request.toString());
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    Gson gson = new Gson();

                    if (queryType.equals("tracks")) {
                        TrackResponse trackResponse = gson.fromJson(responseString, TrackResponse.class);
                        List<CustomTrack> customTracks = new ArrayList<>();

                        for (Track track : trackResponse.getItems()) {
                            String trackId = track.id;
                            String trackName = track.name;
                            String trackUri = track.uri;
                            String artistNames = "";
                            List<Artist> artists = track.artists;
                            for (int i = 0; i < artists.size(); i++) {
                                artistNames += artists.get(i).name;
                                if (i < artists.size() - 1) {
                                    artistNames += ", ";
                                }
                            }
                            String imageUrl = track.album.images.get(0).url;
                            CustomTrack customTrack = new CustomTrack(trackId, trackName, artistNames, imageUrl);
                            customTracks.add(customTrack);
                        }
                        return customTracks;
                    } else if (queryType.equals("artists")) {
                        ArtistResponse artistResponse = gson.fromJson(responseString, ArtistResponse.class);
                        List<CustomArtist> customArtists = new ArrayList<>();

                        for (Artist artist : artistResponse.getItems()) {
                            String artistId = artist.id;
                            String artistName = artist.name;
                            String artistUri = artist.uri;
                            String imageUrl = artist.images.get(0).url;
                            CustomArtist customArtist = new CustomArtist(artistId, artistName, imageUrl);
                            customArtist.setUri(artistUri);
                            customArtists.add(customArtist);
                        }
                        return customArtists;
                    }
                } else {
                    throw new IOException(" Unexpected HTTP response: " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result != null) {
            if (queryType.equals("tracks") && callback != null) {
                callback.onSuccess((List<CustomTrack>) result);
            } else if (queryType.equals("artists") && artistCallback != null) {
                artistCallback.onSuccess((List<CustomArtist>) result);
            } else if (queryType.equals("user_id") && userIdCallback != null) {
                userIdCallback.onSuccess((String) result);
            }
        } else {
            if (queryType.equals("tracks") && callback != null) {
                callback.onFailure(new Exception("Error al obtener los Top " + queryType));
            } else if (queryType.equals("artists") && artistCallback != null) {
                artistCallback.onFailure(new Exception("Error al obtener los Top " + queryType));
            } else if (queryType.equals("user_id") && userIdCallback != null) {
                userIdCallback.onFailure(new Exception("Error al obtener el ID de usuario"));
            }
            Log.d("SpotifyService", "Requesting Top " + token);
        }
    }



    public static class TrackResponse {
        @SerializedName("items")
        private List<Track> items;

        public List<Track> getItems() {
            return items;
        }
    }

    public static class ArtistResponse {
        @SerializedName("items")
        private List<Artist> items;

        public List<Artist> getItems() {
            return items;
        }
    }

    // Nueva clase UserResponse
    public static class UserResponse {
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }
    }

    public static class Track {
        String id;
        String name;
        String uri;
        Album album;
        List<Artist> artists;
    }

    public static class SpotifyUser {
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }
    }

    public static class Artist {
        String id;
        String name;
        String uri;
        List<Image> images;
    }

    public static class Album {
        List<Image> images;
    }

    public static class Image {
        String url;
    }
}