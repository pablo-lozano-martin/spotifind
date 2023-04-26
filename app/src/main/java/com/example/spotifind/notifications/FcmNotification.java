package com.example.spotifind.notifications;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class FcmNotification {
    private String to;
    private NotificationData notification;

    private Map<String, String> data;

    public FcmNotification() {
        data = new HashMap<>();
    }

    public void addData(String key, String value) {
        data.put(key, value);
    }

    public void setTo(String toToken) {
        this.to = toToken;
    }

    public void setNotification(NotificationData notificationData) {
        this.notification = notificationData;
    }

    // Agrega constructores, getters y setters según sea necesario

    public static class NotificationData {
        private String title;
        private String body;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setBody(String body) {
            this.body = body;
        }

        // Agrega constructores, getters y setters según sea necesario
    }
}
