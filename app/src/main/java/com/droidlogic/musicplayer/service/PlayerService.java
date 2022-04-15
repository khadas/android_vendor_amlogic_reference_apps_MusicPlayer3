package com.droidlogic.musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.droidlogic.musicplayer.constant.Constants;
import com.droidlogic.musicplayer.entity.Song;
import com.droidlogic.musicplayer.player.MediaCenter;
import com.droidlogic.musicplayer.util.Logger;

import java.io.IOException;

public class PlayerService extends Service {

    public static class PlayerBackground {

        private int position;
        private boolean playing;
        private Song song;

        public PlayerBackground(int position, boolean playing, Song song) {
            this.position = position;
            this.playing = playing;
            this.song = song;
        }

        public int getPosition() {
            return position;
        }

        public boolean isPlaying() {
            return playing;
        }

        public Song getSong() {
            return song;
        }

        @Override
        public String toString() {
            return "PlayerBackground{" +
                    "position=" + position +
                    ", playing=" + playing +
                    ", song=" + song +
                    '}';
        }
    }

    private MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerBinder();
    }

    public class PlayerBinder extends Binder {

        private boolean autoPlay = true;
        private int seekToMsec;
        private Handler handler;
        private final MediaCenter mediaCenter = MediaCenter.getInstance();

        public void setHandler(Handler handler) {
            this.handler = handler;
        }

        public MediaPlayer getMediaPlayer() {
            return player;
        }

        public void playSong(Song song) {
            playSong(song, true, 0);
        }

        public void playSong(Song song, boolean autoPlay) {
            playSong(song, autoPlay, 0);
        }

        public void playSong(Song song, boolean autoPlay, int seekTo) {
            this.autoPlay = autoPlay;
            this.seekToMsec = seekTo;
            Song playSong = mediaCenter.setCurrentSong(song);
            if (playSong != null) {
                playMusic(playSong.getPath());
            }
        }

        public void togglePlayMode() {
            mediaCenter.togglePlayMode();
            handler.sendEmptyMessage(Constants.MSG_TOGGLE_MODE);
        }

        public void play() {
            if (player != null && !player.isPlaying()) {
                player.start();
                handler.sendEmptyMessage(Constants.MSG_MUSIC_START);
            }
        }

        public void pause() {
            if (player != null && player.isPlaying()) {
                player.pause();
                handler.sendEmptyMessage(Constants.MSG_MUSIC_PAUSE);
            }
        }

        public void togglePlay() {
            if (player == null) return;
            if (!player.isPlaying()) {
                player.start();
                handler.sendEmptyMessage(Constants.MSG_MUSIC_START);
            } else {
                player.pause();
                handler.sendEmptyMessage(Constants.MSG_MUSIC_PAUSE);
            }
        }

        public boolean isPlaying() {
            if (player != null) {
                return player.isPlaying();
            }
            return false;
        }

        public int getDuration() {
            if (player != null) {
                return player.getDuration();
            }
            return 0;
        }

        public int getCurrentPosition() {
            if (player != null) {
                return player.getCurrentPosition();
            }
            return 0;
        }

        public void seekTo(int msec) {
            if (player != null) {
                player.seekTo(msec);
            }
        }

        public PlayerBackground playerBackground() {
            if (player == null) return null;
            PlayerBackground playerBackground = new PlayerBackground(getCurrentPosition(), isPlaying(), mediaCenter.getCurrentSong());
            pause();
            releasePlayer();
            return playerBackground;
        }

        public void playMusic(String url) {
            try {
                preparePlayer();
                player.setDataSource(url);
                player.prepareAsync();
                player.setOnPreparedListener(mp -> {
                    handler.sendEmptyMessage(Constants.MSG_MUSIC_INIT);
                    if (seekToMsec > 0) {
                        mp.seekTo(seekToMsec);
                    }
                    if (autoPlay) {
                        play();
                    }
                    autoPlay = true;
                    seekToMsec = 0;
                });
                player.setOnCompletionListener(mp -> {
                    handler.sendEmptyMessage(Constants.MSG_PLAY_COMPLETE);
                    final Song song = mediaCenter.getNextSong(true);
                    //Logger.d("Play", "playMusic--song" + song);
                    if (song != null) {
                        playSong(song);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                //Logger.d("Play", "playMusic--" + e);
            }
        }
    }

    private void preparePlayer() {
        if (player == null) {
            player = new MediaPlayer();
        } else {
            player.reset();
        }
    }

    private void releasePlayer() {
        try {
            if (player != null) {
                player.reset();
                player.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            player = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
