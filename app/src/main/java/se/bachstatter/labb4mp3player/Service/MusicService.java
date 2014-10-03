package se.bachstatter.labb4mp3player.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import se.bachstatter.labb4mp3player.Activity.MainActivity;
import se.bachstatter.labb4mp3player.Models.Track;
import se.bachstatter.labb4mp3player.Models.TrackListHelper;
import se.bachstatter.labb4mp3player.R;


public class MusicService extends Service{
    public MediaPlayer mediaPlayer;
    public Track currentTrack = null;
    private ArrayList<Track> trackList;
    private boolean isStarted = false;
    private final IBinder mBinder = new PlayerBinder();


    public class PlayerBinder extends Binder {
         public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        super.onCreate();
        Log.d("MusicService", "Creating service...");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MusicService", "Service started...");
        return START_STICKY;
    }

    private void onGoingNotification(Track currentTrack) {
        final int myID = 1234;

        //The intent to launch when the user clicks the expanded notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //This constructor is deprecated. Use Notification.Builder instead
        Notification notice = new Notification(R.drawable.ic_launcher, currentTrack.getFileName().split(".mp3")[0] + " - " + currentTrack.getArtist(), 3);
        //This method is deprecated. Use Notification.Builder instead.
        notice.setLatestEventInfo(this, currentTrack.getFileName(), currentTrack.getArtist(), pendIntent);

        notice.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(myID, notice);

        isStarted = true;
    }

    public void musicPlayer(Track track){
        if (track == null)
            return;
        onGoingNotification(track);


        try {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop(); // Stop current song.

            mediaPlayer.reset(); // reset resource of player
            mediaPlayer.setDataSource(this, Uri.parse(track.getPath())); // set Song to play
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION); // select audio stream
            mediaPlayer.prepare(); // prepare resource
            // on completion handler
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                // onDone
                @Override
                public void onCompletion(MediaPlayer mp) {
                    musicPlayer(currentTrack.getNextTrack());
                }
            });
            mediaPlayer.start(); // play!
            isStarted = true;
            currentTrack = track;
        } catch (Exception e) {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
            Log.d("MusicService error", e.toString());
        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MusicService.class.toString(), "Död!");
        if(mediaPlayer.isPlaying()){
            isStarted = false;
            currentTrack = null;
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}