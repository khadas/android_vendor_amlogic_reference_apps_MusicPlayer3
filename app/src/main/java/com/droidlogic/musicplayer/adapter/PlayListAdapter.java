package com.droidlogic.musicplayer.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidlogic.musicplayer.R;
import com.droidlogic.musicplayer.entity.Song;
import com.droidlogic.musicplayer.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class PlayListAdapter extends BaseAdapter {

    private final List<Song> songList = new ArrayList<>();
    private Song currentSong;
    private int currentIndex;

    public void setSongs(List<Song> songs) {
        this.songList.clear();
        if (songs != null) {
            this.songList.addAll(songs);
        }
        updateCurrentIndex();
        notifyDataSetChanged();
    }

    public List<Song> getSongList() {
        return songList;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    private void updateCurrentIndex() {
        if (currentSong != null) {
            currentIndex = songList.indexOf(currentSong);
        }
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
        updateCurrentIndex();
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Song getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
            Holder holder = new Holder(convertView);
            convertView.setTag(holder);
        }
        Holder holder = (Holder) convertView.getTag();
        Song song = getItem(position);
        boolean isCurrent = song.equals(currentSong);
        if (isCurrent) {
            currentIndex = position;
        }
        holder.bindData(song, isCurrent);
        return convertView;
    }

    private static class Holder {

        private final ImageView imgPlayStatus;
        private final TextView tvSongName;
        private final TextView tvSongArtists;
        private final TextView tvSongDuration;

        public Holder(View convertView) {
            imgPlayStatus = convertView.findViewById(R.id.img_play_item_status);
            tvSongName = convertView.findViewById(R.id.tv_play_item_song_name);
            tvSongArtists = convertView.findViewById(R.id.tv_play_item_song_artists);
            tvSongDuration = convertView.findViewById(R.id.tv_play_item_song_duration);
        }

        public void bindData(Song song, boolean isPlaying) {
            if (song == null) {
                song = new Song();
            }
            imgPlayStatus.setVisibility(isPlaying ? View.VISIBLE : View.INVISIBLE);
            tvSongName.setText(song.getName());
            tvSongArtists.setText(song.getArtistsName());
            tvSongDuration.setText(song.getFormatDuration());
        }

    }

}
