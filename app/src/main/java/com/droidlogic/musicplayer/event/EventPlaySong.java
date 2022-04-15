package com.droidlogic.musicplayer.event;

import com.droidlogic.musicplayer.entity.Song;

/**
 * new play
 */
public class EventPlaySong {

    public Song song;

    public EventPlaySong(Song song) {
        this.song = song;
    }

}
