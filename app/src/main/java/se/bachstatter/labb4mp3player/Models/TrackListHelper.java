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

import se.bachstatter.labb4mp3player.Activity.MainActivity;

/**
 * Created by Jocek on 2014-10-01.
 */
public class TrackListHelper {
    // Const Variables
    private Context context;
    public final static String TRACK = "track";
    public final static String ACTION = "action";
    public final static int NO_TRACK= -1;
    public final static int PLAY = 1;
    public final static int PAUSE = 2;
    public final static int PLAY_SONG = 3;


    /**
     * Constructor that takes context and sets it to class variable
     * @param context
     */
    public TrackListHelper(Context context){
        this.context = context;
    }

    /**
     * Get a cursor with tracks. each track contains id, displayname,album,artist,data.
     * if the cursor is null return null. If we have tracks:
     * set the values to a Track
     * if previous track not is null save this track to previous next track variable.
     * run getAlbumArt(), set prevousTrack to this track and add this track to the tracklist.
     *
     * The last track in the list will get the first track in its nexTrack variable.
     * Finally close the cursor and return our list.
     *
     * @return
     */
    public ArrayList<Track> getPlaylist() {
        ArrayList<Track> trackList;

        Cursor musicResult = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.IS_MUSIC + " > 0 ",
                null,
                null
        );

        trackList = new ArrayList<Track>();
        if(musicResult == null){
            return null;
        }
        if (musicResult.getCount() > 0) {
            musicResult.moveToFirst();
            Track previousTrack = null;
            do {
                Track track = new Track(
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        musicResult.getString(musicResult.getColumnIndex(MediaStore.Audio.Media.DATA))
                );

                if (previousTrack != null) //here prev song linked to current one. To simple play them in list
                    previousTrack.setNextTrack(track);

                getAlbumArt(track);

                previousTrack = track;

                trackList.add(track);
            }
            while (musicResult.moveToNext());

            previousTrack.setNextTrack(trackList.get(MainActivity.FIRST_TRACK)); //play in cycle;
        }
        musicResult.close();
        return trackList;
    }

    /**
     * Create a MediametaDataretriever and set its Datasource to the tracks path.
     * Get the embedded picture and save it to and byte array. If the byte array not is empty:
     * Create a ByteArrayInputStream with it and use BitmapFactory to create a bitmap.
     * Use BitmapDrawable constructor to convert it to a Drawable.
     * Finally save the drawable to the tracks AlbumArt.
     * @param track
     */
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

    /**
     * Check if Storage is available before starting using it.
     * @return boolean
     */
    public boolean checkIfStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
