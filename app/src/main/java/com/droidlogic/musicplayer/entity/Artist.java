package com.droidlogic.musicplayer.entity;

import java.io.Serializable;

public class Artist implements Serializable {

    private long id;
    private String name;
    private String albumUrl;
    private int albumSize;
    private int musicSize;

    public Artist() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public int getAlbumSize() {
        return albumSize;
    }

    public void setAlbumSize(int albumSize) {
        this.albumSize = albumSize;
    }

    public int getMusicSize() {
        return musicSize;
    }

    public void setMusicSize(int musicSize) {
        this.musicSize = musicSize;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", albumUrl='" + albumUrl + '\'' +
                ", albumSize=" + albumSize +
                ", musicSize=" + musicSize +
                '}';
    }
}
