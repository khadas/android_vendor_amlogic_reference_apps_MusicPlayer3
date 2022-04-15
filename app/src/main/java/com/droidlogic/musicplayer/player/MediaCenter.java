package com.droidlogic.musicplayer.player;

import com.droidlogic.musicplayer.constant.Constants;
import com.droidlogic.musicplayer.entity.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MediaCenter {

    private static volatile MediaCenter mInstance;

    private final List<Song> songs = new ArrayList<>();
    private final List<Song> localSongs = new ArrayList<>();

    private Song currentSong;
    private int currentSongIndex;
    private int currentMode = 0;

    private MediaCenter() {
    }

    public static MediaCenter getInstance() {
        if (mInstance == null) {
            synchronized (MediaCenter.class) {
                if (mInstance == null) {
                    mInstance = new MediaCenter();
                }
            }
        }
        return mInstance;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public List<Song> getLocalSongs() {
        return localSongs;
    }

    public void setLocalSongs(List<Song> songs) {
        localSongs.clear();
        if (songs != null) {
            localSongs.addAll(songs);
        }
    }

    public Song getNextSong() {
        return getNextSong(false);
    }

    public Song getNextSong(boolean autoSkip) {
        if (songs.size() == 0) {
            return null;
        }
        int nextIndex = (currentSongIndex + 1) % songs.size();
        switch (getCurrentMode()) {
            case Constants.MODE_REPEAT_LIST:
                return songs.get(nextIndex);
            case Constants.MODE_REPEAT_SINGLE:
                if (autoSkip) {
                    return songs.get(currentSongIndex);
                } else {
                    //manual skip
                    return songs.get(nextIndex);
                }
            case Constants.MODE_REPEAT_RANDOM:
                return getRandomSong();
        }
        return null;
    }

    public Song getPreviousSong() {
        if (songs.size() == 0) {
            return null;
        }
        int nextIndex = currentSongIndex - 1;
        nextIndex = nextIndex < 0 ? songs.size() - 1 : nextIndex;
        switch (getCurrentMode()) {
            case Constants.MODE_REPEAT_LIST:
            case Constants.MODE_REPEAT_SINGLE:
                return songs.get(nextIndex);
            case Constants.MODE_REPEAT_RANDOM:
                return getRandomSong();
        }
        return null;
    }

    private Song getRandomSong() {
        Random random = new Random();
        if (songs.size() > 1) {
            for (int index = songs.size(); index > 0; index--) {
                int randomIndex = random.nextInt(songs.size());
                if (randomIndex != currentSongIndex) {
                    return songs.get(randomIndex);
                }
            }
            return songs.get(random.nextInt(songs.size()));
        } else if (songs.size() == 1) {
            return songs.get(0);
        } else {
            return null;
        }
    }

    public int togglePlayMode() {
        return currentMode = (currentMode + 1) % 3;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public Song setCurrentSong(Song song) {
        int indexCurrent = songs.indexOf(song);
        if (indexCurrent == -1) {
            return null;
        }
        this.currentSongIndex = indexCurrent;
        this.currentSong = song;
        return currentSong;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setSongs(List<Song> songs) {
        this.songs.clear();
        if (songs != null) {
            this.songs.addAll(songs);
        }
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

}
