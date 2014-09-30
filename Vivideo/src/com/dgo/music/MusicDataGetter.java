package com.dgo.music;

import android.app.ListActivity;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

public class MusicDataGetter {
    public ArrayList<String> getListofNames(ArrayList<Music> songs){
        ArrayList<String> songnames=new ArrayList<String>();
        for (int i=0; i<songs.size(); i++){
            songnames.add(songs.get(i).display_name);
        }
        return songnames;
    }
    public ArrayList<Music> getMusics(ListActivity lm){
        ArrayList<Music> songs=new ArrayList<Music>();


        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = lm.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);


        while(cursor.moveToNext()){
            Music m=new Music();
            m.id = cursor.getString(0);
            m.artist = cursor.getString(1);
            m.title = cursor.getString(2);
            m.data = cursor.getString(3);
            m.display_name = cursor.getString(4);
            m.duration = cursor.getString(5);
            m.album_id = cursor.getString(6);
            songs.add(m);
        }
        return songs;
    }

    public Music getMusicByName(String item, ArrayList<Music> mp3s) {
        ArrayList<Music> selected = new ArrayList<Music>();

        for (Music m : mp3s) {
            if (m.display_name.contentEquals(item))
            {
                selected.add(m);
            }
        }
        if (!selected.isEmpty())
            return selected.get(0);
        else
            return null;
    }

}
