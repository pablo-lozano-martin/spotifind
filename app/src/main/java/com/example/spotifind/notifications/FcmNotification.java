package com.example.spotifind.notifications;

public class FcmNotification {
    private String to;
    private NotificationData notification;

    public void setTo(String toToken) {
    }

    public void setNotification(NotificationData notificationData) {
    }

    // Agrega constructores, getters y setters según sea necesario

    public static class NotificationData {
        private String title;
        private String body;

        public void setTitle(String title) {
        }

        public void setBody(String body) {
        }

        // Agrega constructores, getters y setters según sea necesario
    }
}
