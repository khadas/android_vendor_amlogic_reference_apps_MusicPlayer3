package com.droidlogic.musicplayer.activity;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.droidlogic.musicplayer.event.EventPlayOpt;
import com.droidlogic.musicplayer.event.EventPlaySong;
import com.droidlogic.musicplayer.service.PlayerService;

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

}
