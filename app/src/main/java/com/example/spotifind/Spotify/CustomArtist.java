package com.example.spotifind.Spotify;

import com.spotify.protocol.types.Artist;

public class CustomArtist{
    private String id;
    private String name;
    private String imageUrl;

    private String uri; // Asegúrate de tener este campo en la clase CustomArtist

    public CustomArtist() {
    }
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


    public void setImageUrl(String s) {
        this.imageUrl=s;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}