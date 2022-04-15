package com.droidlogic.musicplayer.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.droidlogic.musicplayer.R;
import com.droidlogic.musicplayer.adapter.PlayListAdapter;
import com.droidlogic.musicplayer.entity.Song;
import com.droidlogic.musicplayer.event.EventLyricRefresh;
import com.droidlogic.musicplayer.event.EventPlayOpt;
import com.droidlogic.musicplayer.event.EventPlaySate;
import com.droidlogic.musicplayer.event.EventPlaySong;
import com.droidlogic.musicplayer.player.MediaCenter;
import com.droidlogic.musicplayer.util.Utils;
import com.droidlogic.musicplayer.view.PlaysStatusAnimView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class PlayListActivity extends FragmentActivity {

    private ImageView imgAlbum;
    private TextView tvLyric;
    private TextView tvSongName;
    private PlaysStatusAnimView playAnimView;
    private ListView playList;
    private PlayListAdapter playListAdapter;
    private final MediaCenter mediaCenter = MediaCenter.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);
        EventBus.getDefault().register(this);
        imgAlbum = findViewById(R.id.img_pl_album);
        tvLyric = findViewById(R.id.tv_pl_lyric);
        tvSongName = findViewById(R.id.tv_pl_play_song);
        playAnimView = findViewById(R.id.tv_pl_play_anim);
        playList = findViewById(R.id.lv_pl);
        //current play item
        Song currentSong = mediaCenter.getCurrentSong();
        //init adapter
        playListAdapter = new PlayListAdapter();
        playListAdapter.setCurrentSong(currentSong);
        playListAdapter.setSongs(mediaCenter.getSongs());
        playList.setAdapter(playListAdapter);
        playList.setOnItemClickListener((parent, view, position, id) -> {
            Song song = playListAdapter.getItem(position);
            EventBus.getDefault().post(new EventPlaySong(song));
        });
        playList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                asyncHandler.removeCallbacks(mmrTask);
                mmrTask.song = playListAdapter.getItem(position);
                asyncHandler.postDelayed(mmrTask, 300);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        playList.post(() -> {
            int currentIndex = playListAdapter.getCurrentIndex();
            if (currentIndex >= 0) {
                playList.setSelection(currentIndex);
                playList.smoothScrollToPosition(currentIndex);
            }
        });
        syncPlayInfo(currentSong);
    }

    private void syncPlayInfo(Song currentSong) {
        onEventPlayOpt(EventBus.getDefault().getStickyEvent(EventPlayOpt.class));
        onEventLyricRefresh(EventBus.getDefault().getStickyEvent(EventLyricRefresh.class));
        if (currentSong != null) {
            tvSongName.setText(currentSong.getName());
        } else {
            tvSongName.setText("");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPlaySate(EventPlaySate playSate) {
        Song playSong = playSate.song;
        tvSongName.setText(playSong.getName());
        tvLyric.setText("");
        List<Song> songList = playListAdapter.getSongList();
        for (Song song : songList) {
            if (song.equals(playSate.song)) {
                playListAdapter.setCurrentSong(song);
                playListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventLyricRefresh(EventLyricRefresh lyricRefresh) {
        if (lyricRefresh == null) return;
        if (lyricRefresh.lrcEntry != null) {
            tvLyric.setText(lyricRefresh.lrcEntry.getShowText());
        } else {
            tvLyric.setText("");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventPlayOpt(EventPlayOpt playOpt) {
        if (playOpt == null) return;
        updatePlayState(playOpt.isPlaying);
    }

    private void updatePlayState(boolean isPlaying) {
        if (isPlaying) {
            playAnimView.startAnim();
        } else {
            playAnimView.stopAnim();
        }
    }

    private final HandlerThread handlerThread = new HandlerThread("mmr_thread") {
        {
            start();
        }
    };
    private Handler asyncHandler = new Handler(handlerThread.getLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    private final MmrTask mmrTask = new MmrTask();

    private class MmrTask implements Runnable {

        public Song song;

        @Override
        public void run() {
            final Bitmap bitmap = Utils.getSongCover(song.getPath());
            runOnUiThread(() -> runOnUiThread(() -> Glide.with(PlayListActivity.this)
                    .load(bitmap)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .error(R.mipmap.ic_music_default)
                    .into(imgAlbum)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        asyncHandler.removeCallbacksAndMessages(null);
        asyncHandler = null;
        handlerThread.quitSafely();
    }

}