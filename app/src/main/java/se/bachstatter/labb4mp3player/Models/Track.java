package se.bachstatter.labb4mp3player.Models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Jocek on 2014-09-29.
 */
public class Track {
    private String artist;
    private String fileName;
    private String album;
    private String path;
    private int duration;
    private Drawable albumArt;
    private Track nextTrack = null;


    public Track(String artist, String fileName, String album, String path, String duration){
        this.artist = artist;
        this.fileName = fileName;
        this.album = album;
        this.path = path;
        this.duration = Integer.parseInt(duration);
    }

    public String getAlbum() {
        return album;
    }

    public int getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public String getArtist() {
        return artist;
    }

    public String getFileName() {
        return fileName.split(".mp3")[0];
    }

    public Drawable getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Drawable albumArt) {
        this.albumArt = albumArt;
    }

    public Track getNextTrack() {
        return nextTrack;
    }

    public void setNextTrack(Track nextTrack) {
        this.nextTrack = nextTrack;
    }
}
