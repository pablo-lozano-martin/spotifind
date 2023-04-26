package com.example.spotifind.notifications;

import com.example.spotifind.notifications.FcmNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FcmApiService {
    @POST("fcm/send")
    Call<Void> sendNotification(
            @Header("Authorization") String serverKey,
            @Body FcmNotification notification);
}