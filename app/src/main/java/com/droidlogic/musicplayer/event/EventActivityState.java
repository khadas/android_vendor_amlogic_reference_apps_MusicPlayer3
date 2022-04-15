package com.droidlogic.musicplayer.event;

public class EventActivityState {

    public enum State {
        BACKGROUND, FOREGROUND
    }

    public State state;

    public EventActivityState(State state) {
        this.state = state;
    }

}
