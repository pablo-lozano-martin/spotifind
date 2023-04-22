package com.example.spotifind.Spotify;

import android.os.AsyncTask;
import android.util.Pair;

import com.example.spotifind.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyUriService extends AsyncTask<Pair<String, List<String>>, Void, List<String>> {

    private final SpotifyService.SpotifyCallback<List<String>> callback;

    public SpotifyUriService(SpotifyService.SpotifyCallback<List<String>> callback) {
        this.callback = callback;
    }

    @Override
    protected List<String> doInBackground(Pair<String, List<String>>... params) {
        String searchType = params[0].first;
        List<String> ids = params[0].second;
        List<String> uris = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        StringBuilder idsBuilder = new StringBuilder();
        for (String id : ids) {
            if (idsBuilder.length() > 0) {
                idsBuilder.append(",");
            }
            idsBuilder.append(id);
        }

        String url;
        if (searchType.equals("tracks")) {
            url = "https://api.spotify.com/v1/tracks?ids=" + idsBuilder;
        } else if (searchType.equals("artists")) {
            url = "https://api.spotify.com/v1/artists?ids=" + idsBuilder;
        } else {
            throw new IllegalArgumentException("Invalid search type: " + searchType);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + MainActivity.mAccessToken)
                .addHeader("Accept", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseString = response.body().string();
                JSONObject jsonObject = new JSONObject(responseString);
                JSONArray jsonArray = jsonObject.getJSONArray(searchType);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject itemObject = jsonArray.getJSONObject(i);
                    String uri = itemObject.getString("uri");
                    if (searchType.equals("artists")) {
                        String imageUrl = itemObject.getJSONArray("images").getJSONObject(0).getString("url");
                        uris.add(imageUrl);
                    } else {
                        uris.add(uri);
                        JSONObject albumObject = itemObject.getJSONObject("album");
                        JSONArray imageArray = albumObject.getJSONArray("images");
                        if (imageArray.length() > 0) {
                            String imageUrl = imageArray.getJSONObject(0).getString("url");
                            uris.add(imageUrl);
                        }
                    }
                }
            } else {
                throw new IOException("Unexpected HTTP response: " + response);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return uris;
    }

    @Override
    protected void onPostExecute(List<String> uris) {
        if (uris != null) {
            callback.onSuccess(uris);
        } else {
            callback.onFailure(new Exception("Error al obtener las URIs."));
        }
    }
}
