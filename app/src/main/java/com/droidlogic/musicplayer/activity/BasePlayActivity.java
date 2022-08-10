package com.droidlogic.musicplayer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.droidlogic.musicplayer.event.EventPlayControl;
import com.droidlogic.musicplayer.event.EventPlayOpt;
import com.droidlogic.musicplayer.event.EventPlaySong;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BasePlayActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPlayOpt(EventPlayOpt eventPlayOpt) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPlaySong(EventPlaySong eventPlaySong) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                sendPlayPlayControl(EventPlayControl.PlayControl.PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                sendPlayPlayControl(EventPlayControl.PlayControl.STOP);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                sendPlayPlayControl(EventPlayControl.PlayControl.PREVIOUS);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                sendPlayPlayControl(EventPlayControl.PlayControl.NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                sendPlayPlayControl(EventPlayControl.PlayControl.REWIND);
                break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                sendPlayPlayControl(EventPlayControl.PlayControl.FORWARD);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void sendPlayPlayControl(EventPlayControl.PlayControl playControl) {
        EventBus.getDefault().post(new EventPlayControl(playControl));
    }

}
