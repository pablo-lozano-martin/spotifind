package com.example.spotifind;

import java.util.ArrayList;

public class User {

    private int _id;
    private String _username;
    private String _password;
    private ArrayList<User> friendlist;

    public User(int id, String username, String password){
        _id = id;
        _username = username;
        _password = password;
        this.friendlist = new ArrayList<User>();
    }

    public User(int id, String username, String password, ArrayList<User> fl){
        _id = id;
        _username = username;
        _password = password;
        this.friendlist = fl;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public ArrayList<User> getFriendlist() {
        return friendlist;
    }

    public void setFriendlist(ArrayList<User> friendlist) {
        this.friendlist = friendlist;
    }
}
