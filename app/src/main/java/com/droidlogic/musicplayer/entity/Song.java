package com.droidlogic.musicplayer.entity;

import com.droidlogic.musicplayer.util.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Song implements Serializable {

    private long id;
    private String name;
    private List<Artist> artists;
    private Album album;
    private long size;
    private String url;
    private String songLyric;
    private int duration;
    private String path;

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

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtistsName() {
        String artistsName = " ";
        if (artists != null) {
            for (Artist artist : artists) {
                artistsName += artist.getName() + " ";
            }
        }
        return artistsName;
    }

    public String getSongLyric() {
        return songLyric;
    }

    public void setSongLyric(String songLyric) {
        this.songLyric = songLyric;
    }

    public int getDuration() {
        return duration;
    }

    public String getFormatDuration() {
        return Utils.formatDuration(duration);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return hashCode() == song.hashCode();
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artists=" + artists +
                ", album=" + album +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", songLyric='" + songLyric + '\'' +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                '}';
    }
}
