package com.example.spotifind;

public class LocalUser {

    private int _id;
    private String _username;
    private String _password;

    public LocalUser(int id, String username, String password){
        _id = id;
        _username = username;
        _password = password;
    }

    public int getId() {
        return _id;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }
}
