package com.example.spotifind.Spotify;

import android.os.AsyncTask;
import android.util.Log;

import com.example.spotifind.MainActivity;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.spotify.protocol.types.Artist;
import com.spotify.protocol.types.Track;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyService extends AsyncTask<Void, Void, List<CustomTrack>> {

    private static final String BASE_URL = "https://api.spotify.com/v1/";

    private final String queryType;
    private final SpotifyCallback<List<CustomTrack>> callback;
    private final SpotifyCallback<List<CustomArtist>> artistCallback;

    private final String token;

    private Gson gson;
    public interface SpotifyCallback<T> {
        void onSuccess(T result);
        void onFailure(Throwable throwable);
    }

    public SpotifyService(String accessToken, String queryType, SpotifyCallback<List<CustomTrack>> callback, SpotifyCallback<List<CustomArtist>> artistCallback) {
        this.token=accessToken;
        this.queryType = queryType;
        this.callback = callback;
        this.artistCallback = artistCallback;
        gson = new Gson();
    }


    @Override
    protected List<CustomTrack> doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL + "me/top/" + queryType + "?limit=5")
                .addHeader("Authorization", "Bearer " + MainActivity.getToken())
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();
        Log.d("request:", request.toString());
        try {

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                List<CustomTrack> customTracks = new ArrayList<>();
                String responseString = response.body().string();
                Gson gson = new Gson();
                if (queryType.equals("tracks")) {
                    TrackResponse trackResponse = gson.fromJson(responseString, TrackResponse.class);
                    for (Track track : trackResponse.getItems()) {
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
                        CustomTrack customTrack = new CustomTrack(trackUri, trackName, artistNames);
                        customTracks.add(customTrack);
                    }
                } else if (queryType.equals("artists")) {
                    ArtistResponse artistResponse = gson.fromJson(responseString, ArtistResponse.class);
                    AbstractList<CustomArtist> customArtists = new ArrayList<>();
                    for (Artist artist : artistResponse.getItems()) {
                        String artistName = artist.name;
                        String artistUri = artist.uri;
                        CustomArtist customArtist = new CustomArtist(artistUri, artistName);
                        customArtists.add(customArtist);
                    }
                    artistCallback.onSuccess(customArtists);
                }
                return customTracks;
            } else {
                throw new IOException("Unexpected HTTP response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<CustomTrack> customTracks) {
        if (customTracks != null) {
            if (callback != null) {
                callback.onSuccess(customTracks);
            }
        } else {
            if (artistCallback != null) {
                artistCallback.onFailure(new Exception("Error al obtener los Top " + queryType));
            }
            Log.d("SpotifyService", "Requesting Top " + MainActivity.getToken());
        }
    }

    public static class TrackResponse {
        @SerializedName("items")
        private List<Track> items;

        private TrackResponse(List<Track> items) {
            this.items = items;
        }

        public List<Track> getItems() {
            return items;
        }
    }

    public static class ArtistResponse {
        @SerializedName("items")
        private List<Artist> items;

        private ArtistResponse(List<Artist> items) {
            this.items = items;
        }

        public List<Artist> getItems() {
            return items;
        }
    }
}