package com.example.spotifind.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FcmApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=YOUR_SERVER_KEY" // Reemplaza YOUR_SERVER_KEY con la clave del servidor de tu proyecto Firebase
    })
    @POST("fcm/send")
    Call<Void> sendNotification(@Body FcmNotification notification);
}
