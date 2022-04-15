package com.droidlogic.musicplayer.event;

import com.droidlogic.musicplayer.entity.Song;

/**
 * player prepared
 */
public class EventPlaySate {

    public Song song;

    public EventPlaySate(Song song) {
        this.song = song;
    }

}
