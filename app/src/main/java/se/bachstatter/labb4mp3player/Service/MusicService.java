package se.bachstatter.labb4mp3player.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import se.bachstatter.labb4mp3player.Activity.MainActivity;
import se.bachstatter.labb4mp3player.Models.Track;
import se.bachstatter.labb4mp3player.R;


public class MusicService extends Service{
    /**
     * Constants.
     */
    private static final String ERROR_MESSAGE = "Cant play this song";
    private static final String DASH_DIVIDER = " - " ;
    /**
     * Class variable
      */
    public MediaPlayer mediaPlayer;
    public Track currentTrack = null;
    /**
     * Class variable for insansiate inner class
     */
    private final IBinder mBinder = new PlayerBinder();

    /**
     * Class used for the client Binder.
     */
    public class PlayerBinder extends Binder {
         public MusicService getService() {
            // Return this instance of MusicService so clients can call public methods
            return MusicService.this;
        }
    }

    /**
     * OnCreate instatiate a new MediaPlayer
     */
    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        super.onCreate();
    }

    /**
     * On bind return out mBinder with PLayerBinder class
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Returning start sticky will make it possible to stop the service.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MusicService", "Service started...");
        return START_STICKY;
    }

    /**
     *
     * Create The intent to launch when the user clicks the expanded notification
     * On click the MainActivity will be runned.
     *
     * Set flags so the user cant "throw" away the notifaication and so it dont disaper when user
     * clicks on clear.
     * Finally start it as a Foreground notification.
     *
     * @param currentTrack
     */
    private void onGoingNotification(Track currentTrack) {
        final int myID = 1234;

        //The intent to launch when the user clicks the expanded notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //This constructor is deprecated. Use Notification.Builder instead
        Notification notice = new Notification(R.drawable.ic_launcher, currentTrack.getFileName() + DASH_DIVIDER + currentTrack.getArtist(), 3);
        //This method is deprecated. Use Notification.Builder instead.
        notice.setLatestEventInfo(this, currentTrack.getFileName(), currentTrack.getArtist(), pendIntent);

        notice.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(myID, notice);
    }

    /**
     * Our music player method will create a mediaplayer.
     * if track i null return.
     * Always show a notification with the playing song.
     * If media player isPLaying stop.
     * Reset the player, set data source to our path,
     * setAudioStreamType to music, prepare the player.
     * Set and onCompletionListener to play the next song on completion.
     * Start the player.
     * Set currentTrack to the track playing
     *
     * On error catch it and send error message to user with toast and to logcat.
     * @param track the track we want to play.
     */
    public void musicPlayer(Track track){
        if (track == null)
            return;
        onGoingNotification(track);
        try {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, Uri.parse(track.getPath()));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp) {
                    musicPlayer(currentTrack.getNextTrack());
                }
            });
            mediaPlayer.start();
            currentTrack = track;
        } catch (Exception e) {
            Toast.makeText(this, ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
            Log.d(this.toString(), e.toString());
        }
    }


    /**
     * On destroy is mediaplayer is playing
     * set currentTrack to null
     * stop the player.
     * And finally release resources associated with this MediaPlayer object.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()){
            currentTrack = null;
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}