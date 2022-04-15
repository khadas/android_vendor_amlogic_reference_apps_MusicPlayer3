package com.droidlogic.musicplayer.event;

import com.droidlogic.musicplayer.lrcview.LrcEntry;

public class EventLyricRefresh {

    public LrcEntry lrcEntry;

    public EventLyricRefresh(LrcEntry lrcEntry) {
        this.lrcEntry = lrcEntry;
    }

}
