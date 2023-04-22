package com.example.spotifind.Spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthSpotify {

    private static final String TAG = "AuthSpotify";
    private static final String AUTH_URL = "https://accounts.spotify.com/api/token";

    private Context context;
    private String clientId;
    private String clientSecret;
    private SharedPreferences sharedPref;

    public AuthSpotify(Context context, String clientId, String clientSecret) {
        this.context = context;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        sharedPref = context.getSharedPreferences("spotify", Context.MODE_PRIVATE);
    }

    public interface AuthCallback {
        void onAuthResponse(String token);
        void onAuthError(String error);
    }

    public void authenticate(AuthCallback callback) {
        StringRequest request = new StringRequest(Request.Method.POST, AUTH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String token = jsonObject.getString("access_token");
                            callback.onAuthResponse(token);
                            Log.d(TAG, "Authentication successful");
                            saveToken(token);
                        } catch (JSONException e) {
                            callback.onAuthError("Error parsing response");
                            Log.e(TAG, "Error parsing response", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onAuthError(error.getMessage());
                        Log.e(TAG, "Authentication failed", error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String credentials = clientId + ":" + clientSecret;
                String encodedCredentials = new String(android.util.Base64.encode(credentials.getBytes(), android.util.Base64.NO_WRAP));
                headers.put("Authorization", "Basic " + encodedCredentials);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "client_credentials");
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", token);
        editor.apply();
        Log.d(TAG, "Token saved");
    }

    public String getToken() {
        return sharedPref.getString("token", null);
    }

}
