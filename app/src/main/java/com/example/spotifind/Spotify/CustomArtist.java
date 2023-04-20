package com.example.spotifind.Spotify;

import com.spotify.protocol.types.Artist;

public class CustomArtist{
    private String id;
    private String name;

    public CustomArtist(String id, String name) {

        this.id = id;
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}