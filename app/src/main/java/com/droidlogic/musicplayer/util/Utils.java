package com.droidlogic.musicplayer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.droidlogic.musicplayer.entity.Album;
import com.droidlogic.musicplayer.entity.Artist;
import com.droidlogic.musicplayer.entity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String UN_KNOW = "<unKnow>";

    public static void toast(Context context, String toastMessage) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

    public static List<Song> getLocalMusics(Context context) {
        List<Song> songList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                String artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                Artist artist = new Artist();
                ArrayList<Artist> artists = new ArrayList<>();
                artist.setName(artistName);
                artists.add(artist);
                song.setArtists(artists);
                Album album = new Album();
                album.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                album.setCoverUrl("");
                song.setAlbum(album);
                song.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                song.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                song.setSongLyric(getLyricByFile(new File(song.getPath())));
                //String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                song.setName(getSongFileName(song.getPath()));
                songList.add(song);
                //Logger.d(song.toString());
            }
            cursor.close();
        }
        return songList;
    }

    private static String getSongFileName(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                String fileName = file.getName();
                return fileName.substring(0, fileName.lastIndexOf("."));
            }
        }
        return UN_KNOW;
    }

    public static String getAlbumThumbnail(Context context, String album_id) {
        String str = null;
        ContentResolver resolver = context.getContentResolver();
        Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String thumbnail = MediaStore.Audio.Albums.ALBUM_ART;
        String id = MediaStore.Audio.Albums._ID;
        Cursor cursor = resolver.query(albumUri, new String[]{thumbnail}, id + "=?", new String[]{album_id}, null);
        while (cursor.moveToNext()) {
            str = cursor.getString(cursor.getColumnIndex(thumbnail));
        }
        cursor.close();
        return str;
    }

    public static String getLyricByFile(File musicFile) {
        File parentFile = musicFile.getParentFile();
        if (parentFile == null) return null;
        String parentPath = parentFile.getAbsolutePath();
        File lrcFile = new File(parentPath, musicFile.getName().substring(0, musicFile.getName().indexOf(".")) + ".lrc");
        if (lrcFile.exists()) {
            return lrcFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static Bitmap getSongCover(String path) {
        Bitmap cover = null;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            byte[] pictureByte = mmr.getEmbeddedPicture();
            if (pictureByte != null) {
                cover = BitmapFactory.decodeByteArray(pictureByte, 0, pictureByte.length);
            }
            mmr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cover;
    }

    public static Song getSong(String filePath) {
        Song song = null;
        File file = new File(filePath);
        if (file.exists()) {
            song = new Song();
            song.setPath(filePath);
            String fileName = file.getName();
            song.setName(fileName.substring(0, fileName.lastIndexOf(".")));
            try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                mmr.setDataSource(filePath);
                String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                Album album = new Album();
                album.setName(TextUtils.isEmpty(albumName) ? UN_KNOW : albumName);
                song.setAlbum(album);
                song.setDuration(Integer.parseInt(duration));
                song.setArtists(new ArrayList<Artist>() {
                    {
                        Artist artist = new Artist();
                        artist.setName(TextUtils.isEmpty(artistName) ? UN_KNOW : artistName);
                        add(artist);
                    }
                });
                song.setSongLyric(Utils.getLyricByFile(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return song;
    }

    public static String formatDuration(long duration) {
        long minute = duration / 1000 / 60;
        long second = duration / 1000 % 60;
        return (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second;
    }

}
