package se.bachstatter.labb4mp3player.Models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Jocek on 2014-10-01.
 */
public class TrackListHelper {
    // Const Variables
    private Context context;
    public final static String TRACK = "track";
    public final static String ACTION = "action";
    public final static int PLAY = 1;
    public final static int PAUSE = 2;
    public final static int PLAY_SONG = 3;



    public TrackListHelper(Context context){
        this.context = context;
    }

    public ArrayList<Track> getPlaylist() {
        ArrayList<Track> trackList = new ArrayList<Track>();

        Cursor musicResult = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DURATION},
                MediaStore.Audio.Media.IS_MUSIC + " > 0 ",
                null,
                null
        );

        trackList = new ArrayList<Track>();
        if(musicResult == null){
            return trackList;
        }
        if (musicResult.getCount() > 0) {
            musicResult.moveToFirst();
            Track prev = null;
            do {
                Track track = new Track(
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.DURATION))
                );

                if (prev != null) //here prev song linked to current one. To simple play them in list
                    prev.setNextTrack(track);

                getAlbumArt(track);

                prev = track;

                trackList.add(track);
            }
            while (musicResult.moveToNext());

            prev.setNextTrack(trackList.get(0)); //play in cycle;
        }
        Log.d("MainActivity PlaylistCount ", "" + musicResult.getCount());

        musicResult.close();

        return trackList;
    }

    private void getAlbumArt(Track track) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(track.getPath());
        byte[] artBytes =  mmr.getEmbeddedPicture();
        if(artBytes != null)
        {
            InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
            Bitmap bm = BitmapFactory.decodeStream(is);
            Drawable drawable = new BitmapDrawable(context.getResources(),bm);

            track.setAlbumArt(drawable);
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
