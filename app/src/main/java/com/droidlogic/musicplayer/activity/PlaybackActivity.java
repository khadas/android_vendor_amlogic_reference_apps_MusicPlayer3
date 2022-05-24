package com.droidlogic.musicplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.droidlogic.musicplayer.R;

import com.droidlogic.musicplayer.constant.Constants;
import com.droidlogic.musicplayer.entity.Song;
import com.droidlogic.musicplayer.event.EventActivityState;
import com.droidlogic.musicplayer.event.EventLyricRefresh;
import com.droidlogic.musicplayer.event.EventPlayOpt;
import com.droidlogic.musicplayer.event.EventPlaySate;
import com.droidlogic.musicplayer.event.EventPlaySong;
import com.droidlogic.musicplayer.lrcview.LrcView;
import com.droidlogic.musicplayer.player.MediaCenter;
import com.droidlogic.musicplayer.service.PlayerService;
import com.droidlogic.musicplayer.util.Utils;
import com.droidlogic.musicplayer.view.RoundAnimImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.droidlogic.musicplayer.event.EventActivityState.State.BACKGROUND;
import static com.droidlogic.musicplayer.event.EventActivityState.State.FOREGROUND;

public class PlaybackActivity extends BasePlayActivity implements View.OnClickListener, ServiceConnection, View.OnFocusChangeListener {

    private ImageView btnPre;
    private ImageView btnNext;
    private ImageView btnPause;
    private ImageView btnListRepeatMode;
    private ImageView btnSongList;
    private TextView tvSongName;
    private TextView tvSongAlbum;
    private TextView tvSongArtists;
    private TextView tvSongPlayTime;
    private TextView tvSongTotalTime;
    private RoundAnimImageView imgAlbum;
    private SeekBar songSeekBar;
    private LrcView lrcView;

    private boolean sourceFromOther;
    private PlayerService.PlayerBinder binder;
    private Song song;
    private final DateFormat durationFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private final MediaCenter mediaCenter = MediaCenter.getInstance();

    private PlayerService.PlayerBackground playerBackground;
    private boolean isBindService = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_MUSIC_INIT:
                    updateView(mediaCenter.getCurrentSong());
                    syncOptState(false);
                    EventBus.getDefault().postSticky(new EventLyricRefresh(null));
                    break;
                case Constants.MSG_MUSIC_START:
                    btnPause.setImageResource(R.drawable.ic_pause_selector);
                    handler.post(runnable);
                    imgAlbum.startAnim();
                    syncOptState(true);
                    break;
                case Constants.MSG_MUSIC_PAUSE:
                    btnPause.setImageResource(R.drawable.ic_play_selector);
                    handler.removeCallbacks(runnable);
                    imgAlbum.pauseAnim();
                    syncOptState(false);
                    break;
                case Constants.MSG_TOGGLE_MODE:
                    updatePlayMode(mediaCenter.getCurrentMode());
                    break;
                case Constants.MSG_PLAY_COMPLETE:
                    imgAlbum.stopAnim();
                    break;
                default:
                    break;
            }
        }
    };

    private void syncOptState(boolean isPlaying) {
        EventBus.getDefault().postSticky(new EventPlayOpt(isPlaying));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (PlayerService.PlayerBinder) service;
        binder.setHandler(handler);
        if (song != null) {
            //binder.playSong(song, sourceFromOther);
            binder.playSong(song, true);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBindService = false;
        binder = null;
    }

    @Override
    public void onEventPlaySong(EventPlaySong eventPlaySong) {
        super.onEventPlaySong(eventPlaySong);
        if (binder != null) {
            binder.playSong(eventPlaySong.song);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initView();
        checkPermission();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        } else {
            loadData();
        }
    }

    private void loadData() {
        List<Song> localSongs = Utils.getLocalMusics(getApplicationContext());
        mediaCenter.setLocalSongs(localSongs);
        Song preparePlaySong = null;
        //from 3rd source
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null && uri.toString().length() > 0) {
            Song song = null;
            try {
                song = parseIntent(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (song != null) {
                for (Song localSong : localSongs) {
                    if (localSong.equals(song)) {
                        //included in localList
                        preparePlaySong = localSong;
                        break;
                    }
                }
                //don't included , insert list
                if (preparePlaySong == null) {
                    localSongs.add(song);
                    preparePlaySong = song;
                }
                sourceFromOther = true;
                playerBackground = null;
            }
        }
        if (preparePlaySong == null) {
            if (localSongs.size() > 0) {
                preparePlaySong = localSongs.get(0);
            }
        }

        prepareMediaList(localSongs, preparePlaySong);

        if (!isBindService) {
            Intent serviceIntent = new Intent(this, PlayerService.class);
            isBindService = bindService(serviceIntent, this, BIND_AUTO_CREATE);
        } else {
            if (song != null) {
                //binder.playSong(song, sourceFromOther);
                binder.playSong(song, true);
            }
        }

    }

    private Song parseIntent(Uri uri) {

        String filePath = null;

        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            filePath = uri.getPath();
        } else {
            final String column = "_data";
            final String[] projection = {column};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                filePath = cursor.getString(column_index);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        if (!TextUtils.isEmpty(filePath)) {
            return Utils.getSong(filePath);
        }

        setIntent(new Intent());

        return null;
    }

    private void prepareMediaList(List<Song> songs, Song preparePlaySong) {
        mediaCenter.setSongs(songs);
        song = preparePlaySong;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadData();
            } else {
                Utils.toast(this, getString(R.string.permission_denied));
                finish();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadData();
    }

    private void initView() {
        btnPre = findViewById(R.id.image_button_pre);
        btnNext = findViewById(R.id.image_button_next);
        btnPause = findViewById(R.id.image_button_pause);
        btnListRepeatMode = findViewById(R.id.image_button_list_repeat_mode);
        btnSongList = findViewById(R.id.image_button_song_list);

        songSeekBar = findViewById(R.id.song_seek_bar);
        tvSongName = findViewById(R.id.tv_song_name);
        tvSongAlbum = findViewById(R.id.tv_song_album);
        tvSongArtists = findViewById(R.id.tv_song_artists);
        tvSongPlayTime = findViewById(R.id.tv_song_play_time);
        tvSongTotalTime = findViewById(R.id.tv_song_total_time);

        lrcView = findViewById(R.id.lrc_view);
        imgAlbum = findViewById(R.id.img_song_album);

        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnPause.setFocusedByDefault(true);
        btnListRepeatMode.setOnClickListener(this);
        btnSongList.setOnClickListener(this);

        btnPre.setOnFocusChangeListener(this);
        btnNext.setOnFocusChangeListener(this);
        btnPause.setOnFocusChangeListener(this);
        btnListRepeatMode.setOnFocusChangeListener(this);
        btnSongList.setOnFocusChangeListener(this);

        lrcView.setOnLyricRefreshListener((view, lrcEntry) -> EventBus.getDefault().postSticky(new EventLyricRefresh(lrcEntry)));

        final Rect bounds = songSeekBar.getProgressDrawable().getBounds();
        songSeekBar.setOnFocusChangeListener((v, hasFocus) -> {
            Drawable progressDrawable;
            if (hasFocus) {
                progressDrawable = ContextCompat.getDrawable(v.getContext(), R.drawable.play_seek_bar_focus);
            } else {
                progressDrawable = ContextCompat.getDrawable(v.getContext(), R.drawable.play_seek_bar);
            }
            songSeekBar.setProgressDrawable(progressDrawable);
            if (progressDrawable != null) {
                progressDrawable.setBounds(bounds);
            }
        });

    }

    public void updateView(Song song) {
        if (song == null) return;
        if (!TextUtils.equals(song.getName(), tvSongName.getText())) {
            tvSongName.setText(song.getName());
        }
        String albumName = getString(R.string.unknown);
        if (song.getAlbum() != null) {
            albumName = song.getAlbum().getName();
        }
        tvSongAlbum.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.song_album), albumName));
        tvSongArtists.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.song_artists), song.getArtistsName()));
        int duration = binder.getDuration();
        tvSongTotalTime.setText(durationFormat.format(duration));
        songSeekBar.setMax(duration);
        songSeekBar.setProgress(0);
        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    binder.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //load lyric
        String lyric = song.getSongLyric();
        if (!TextUtils.isEmpty(lyric)) {
            File lyricFile = new File(lyric);
            if (lyricFile.exists()) {
                lrcView.loadLrc(lyricFile);
            }
        } else {
            lrcView.loadLrc("");
        }
        //broadcast
        EventBus.getDefault().post(new EventPlaySate(song));
    }

    private final DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventPlaySate(EventPlaySate playSate) {
        Song song = playSate.song;
        final Bitmap bitmap = Utils.getSongCover(song.getPath());
        runOnUiThread(() -> Glide.with(PlaybackActivity.this)
                .load(bitmap)
                .error(R.mipmap.ic_music_default)
                .transition(DrawableTransitionOptions.withCrossFade(1500))
                .circleCrop()
                .into(imgAlbum));
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventActivityState(EventActivityState activityState) {
        if (isBindService && binder != null) {
            if (BACKGROUND == activityState.state) {
                playerBackground = binder.playerBackground();
            } else if (FOREGROUND == activityState.state) {
                if (playerBackground != null && playerBackground.isPlaying()) {
                    binder.playSong(playerBackground.getSong(), true, playerBackground.getPosition());
                }
                playerBackground = null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_button_pre:
                binder.playSong(mediaCenter.getPreviousSong());
                break;
            case R.id.image_button_next:
                binder.playSong(mediaCenter.getNextSong());
                break;
            case R.id.image_button_pause:
                binder.togglePlay();
                break;
            case R.id.image_button_list_repeat_mode:
                binder.togglePlayMode();
                break;
            case R.id.image_button_song_list:
                startActivity(new Intent(this, PlayListActivity.class));
                break;
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (binder.isPlaying()) {
                long time = binder.getCurrentPosition();
                lrcView.updateTime(time);
                songSeekBar.setProgress((int) time);
                tvSongPlayTime.setText(durationFormat.format(time));
            }
            handler.postDelayed(this, 300);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler = null;
        if (isBindService) {
            unbindService(this);
            isBindService = false;
        }
    }

    private void updatePlayMode(int playMode) {
        int toastStrId = -1;
        switch (playMode) {
            case Constants.MODE_REPEAT_LIST:
                btnListRepeatMode.setImageResource(R.drawable.ic_repeat_list_selector);
                toastStrId = R.string.play_mode_repeat_list;
                break;
            case Constants.MODE_REPEAT_SINGLE:
                btnListRepeatMode.setImageResource(R.drawable.ic_repeat_single_selector);
                toastStrId = R.string.play_mode_repeat_single;
                break;
            case Constants.MODE_REPEAT_RANDOM:
                btnListRepeatMode.setImageResource(R.drawable.ic_repeat_random_selector);
                toastStrId = R.string.play_mode_random;
                break;
        }
        if (toastStrId != -1) {
            Toast.makeText(this, toastStrId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_notice)
                    .setMessage(R.string.dialog_exit_msg)
                    .setPositiveButton(R.string.dialog_btn_confirm, (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .setNegativeButton(R.string.dialog_btn_cancel, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            songSeekBar.setNextFocusDownId(v.getId());
        }
    }

}
