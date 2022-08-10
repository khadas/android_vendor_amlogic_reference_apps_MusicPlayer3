package com.droidlogic.musicplayer.event;

public class EventPlayControl {

    public enum PlayControl {
        PAUSE, STOP, PREVIOUS, NEXT, REWIND, FORWARD
    }

    public PlayControl playControl;

    public EventPlayControl(PlayControl playControl) {
        this.playControl = playControl;
    }
}
