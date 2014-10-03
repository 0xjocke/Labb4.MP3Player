package se.bachstatter.labb4mp3player.Models;

import android.graphics.drawable.Drawable;

/**
 * Created by Jocek on 2014-09-29.
 */
public class Track {
    private String artist;
    private String fileName;
    private String album;
    private String path;
    private Drawable albumArt;
    private Track nextTrack = null;

    /**
     * Constructor that sets all the arguments it get to calss variables.
     * @param artist
     * @param fileName
     * @param album
     * @param path
     */
    public Track(String artist, String fileName, String album, String path){
        this.artist = artist;
        this.fileName = fileName;
        this.album = album;
        this.path = path;
    }

    public String getAlbum() {
        return album;
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
