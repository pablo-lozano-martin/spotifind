package com.example.spotifind;

import java.util.ArrayList;

public class LocalUser {

    private int _id;
    private String _username;
    private String _password;
    private ArrayList<LocalUser> friendlist;

    public LocalUser(int id, String username, String password){
        _id = id;
        _username = username;
        _password = password;
        this.friendlist = new ArrayList<LocalUser>();
    }

    public LocalUser(int id, String username, String password, ArrayList<LocalUser> fl){
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

    public ArrayList<LocalUser> getFriendlist() {
        return friendlist;
    }

    public void setFriendlist(ArrayList<LocalUser> friendlist) {
        this.friendlist = friendlist;
    }
}
