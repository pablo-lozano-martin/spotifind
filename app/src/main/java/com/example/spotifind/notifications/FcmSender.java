package com.example.spotifind.notifications;

import android.util.Log;

import com.example.spotifind.BuildConfig;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmSender {
    private static final String FCM_BASE_URL = "https://fcm.googleapis.com/";

    public static void sendFcmNotification(String toToken, String title, String body, String senderUid) {

        String fcmServerKey = "key=" + BuildConfig.SERVER_KEY;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FCM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FcmApiService fcmApiService = retrofit.create(FcmApiService.class);

        FcmNotification notification = new FcmNotification();
        notification.setTo(toToken);

        FcmNotification.NotificationData notificationData = new FcmNotification.NotificationData();
        notificationData.setTitle(title);
        notificationData.setBody(body);
        notification.setNotification(notificationData);

        // Agrega senderUid como un dato de la notificación
        notification.addData("senderUid", senderUid);

        Call<Void> call = fcmApiService.sendNotification(fcmServerKey, notification);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Notificación enviada exitosamente");
                } else {
                    Log.e("FCM", "Error al enviar notificación. Código de estado: " + response.code());
                    try {
                        Log.e("FCM", "Error al enviar notificación. Cuerpo de la respuesta: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("FCM", "Error al leer el cuerpo de la respuesta", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Error al enviar notificación", t);
            }
        });
    }
}
