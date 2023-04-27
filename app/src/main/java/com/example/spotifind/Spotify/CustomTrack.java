package com.example.spotifind.Spotify;

public class CustomTrack {
    private String id;
    private String name;
    private String uri;
    private String imageUrl;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    private String artist;


    public CustomTrack(){

    }
    public CustomTrack(String id, String name, String artist,String imageUrl) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.imageUrl=imageUrl;

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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setAlbumImageUrl(String s) {
        this.imageUrl=s;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }
}
