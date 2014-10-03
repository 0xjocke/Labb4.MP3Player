package se.bachstatter.labb4mp3player.Activity;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.ArrayList;


import se.bachstatter.labb4mp3player.Adapters.MusicAdapter;
import se.bachstatter.labb4mp3player.Models.Track;
import se.bachstatter.labb4mp3player.Models.TrackListHelper;
import se.bachstatter.labb4mp3player.R;
import se.bachstatter.labb4mp3player.Service.MusicService;



public class MainActivity extends ListActivity implements Chronometer.OnChronometerTickListener, SeekBar.OnSeekBarChangeListener{
    /**
     * Constants
     */
    private static final int PLAY_BTN_POS = 1;
    private static final int START_STATE = 0 ;
    public static final int FIRST_TRACK = 0;
    /**
     * Class variables
     */
    ArrayList<Track> trackList;
    private int currentTrack = 0;

    private Menu menu;

    private SeekBar seekbar;

    private Chronometer chronometer;
    private Boolean chronoIsCounting = false;
    private long timeWhenStopped = 0;

    MusicService mService;
    boolean boundState = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * On Service connected set the mService variable to our service.
         * Set boundState
         * @param className
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to MusicService, cast the IBinder and get MusicService instance
            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            mService = binder.getService();
            boundState = true;
        }

        /**
         * onServiceDisconnected set boundState to false.
         * @param arg0
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundState = false;
        }
    };

    /**
     *  setContentView to out layout.
     * run initializeVarAndListeners
     * run startAndBindIntent
     * Create a new instance of trackListHelper
     * if externalStorage is avaliable:
     * getTrackList put it into our musicAdapter and setListAdatper to this class.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVarsAndListeners();
        startAndBindIntent();
        TrackListHelper trackListHelper = new TrackListHelper(getApplicationContext());
        if(trackListHelper.checkIfStorageAvailable()){
            trackList = trackListHelper.getPlaylist();
            MusicAdapter musicAdapter = new MusicAdapter(this, trackList);
            setListAdapter(musicAdapter);
        }
    }

    /**
     * Initialize seekbar, chronometer and set listeners
     */
    private void initializeVarsAndListeners(){
        seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(this);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(this);
    }

    /**
     * Create and intent
     * Both start and bind it.
     * We start it so it can live on its own.
     * BIND_ABOVE_CLIENT means that the service has higher priority than the activity.
     */
    private void startAndBindIntent(){
        Intent intentService = new Intent(this, MusicService.class);
        startService(intentService);
        bindService(intentService, mConnection, BIND_ABOVE_CLIENT);
    }

    /**
     * OnStop unbind. The service will still be running cause we also run startService.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (boundState) {
            unbindService(mConnection);
            boundState = false;
        }
    }

    /**
     * Save the menu to classvariable
     * Inflate the menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * onListItemClick set currentTrack to position.
     * With the help of mService run musicPlayer method and send it chosen track.
     * Change pause btn to play btn
     * run stopChronometer() and runChronometer() to make sure it starts from zero.
     * set isPlaying to true.
     *
     * @param listView
     * @param view
     * @param position
     * @param id
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        currentTrack = position;
        mService.musicPlayer(trackList.get(currentTrack));
        menu.getItem(PLAY_BTN_POS).setIcon(R.drawable.ic_action_pause);
        stopChronometer();
        startChronometerAndSeekBar();
    }

    /**
     * Check what btn is clicked and run associated function
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.playBtn:
                playBtnAction(item);
                break;
            case R.id.nextBtn:
                nextBtnAction();
                break;
            case R.id.prevBtn:
                prevBtnAction();
                break;
            case R.id.stopBtn:
                stopBtnAction();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * StopBtnAction runs stop on mediaplayer.
     * sets currentTrack in the service to null
     * sets currentTrack in activity to No_TRACK.
     * run stopChronometer()
     * set seekbar to start state
     * change playBtn to pause
     *
     */
    private void stopBtnAction(){
        mService.mediaPlayer.stop();
        mService.currentTrack = null;
        currentTrack = TrackListHelper.NO_TRACK;
        stopChronometer();
        seekbar.setProgress(START_STATE);
        menu.getItem(PLAY_BTN_POS).setIcon(R.drawable.ic_action_play);
    }

    /**
     * If musicplayer not isplaying return without doing anything.
     * is currentTrack not is in start state set currentTrack to previous track
     * run musicplayer with currentTrack, stopChronometer() and startChronometer()
     */
    private void prevBtnAction(){
        if(!mService.mediaPlayer.isPlaying())
            return;
        if(currentTrack != START_STATE){
            --currentTrack;
        }
        mService.musicPlayer(trackList.get(currentTrack));
        stopChronometer();
        startChronometerAndSeekBar();
    }
    /**
     * NextBtnAction
     *
     * If music not playing return without doing anything.
     * save lastTrack position by taking traklist.size()-1
     * Is currentTrack is lastTrack set currentTrack START_STATE.
     * else setCurrentTrack to next track
     * run musicplayer with currentTrack, stopChronometer() and startChronometer()
     *
     */
    private void nextBtnAction() {
        if(!mService.mediaPlayer.isPlaying())
            return;
        int lastTrackPosition = trackList.size()-1;
        if(currentTrack == lastTrackPosition ){
            currentTrack = START_STATE;
        }else{
            ++currentTrack;
        }
        mService.musicPlayer(trackList.get(currentTrack));
        stopChronometer();
        startChronometerAndSeekBar();
    }

    /**
     * IF mediaplayer is playing pause it, pause chornomter and set icon to pause.
     *
     * else
     * if musicService currentTrack is null, play firstTrack, startChornometerAndSeekBar
     * set playbtn to pause and put mainactivitys classVariable currentTrack to start state.
     *
     * else continue where we paused by calling play on mediaplayer.
     * start chronometer and seekbar and set icon to pause.
     * @param item
     */
    private void playBtnAction(MenuItem item){
        if (mService.mediaPlayer.isPlaying()) {
            mService.mediaPlayer.pause();
            pauseChromometer();
            item.setIcon(R.drawable.ic_action_play);
        } else {
            if(mService.currentTrack == null){
                mService.musicPlayer(trackList.get(FIRST_TRACK));
                startChronometerAndSeekBar();
                item.setIcon(R.drawable.ic_action_pause);
                currentTrack = START_STATE;
            }else{
                mService.mediaPlayer.start();
                startChronometerAndSeekBar();
                item.setIcon(R.drawable.ic_action_pause);
            }

        }
    }

    /**
     * Setbase for chronometer to  elapsed milliseconds since boot + timeWhenstop (which will be zero at first)
     * set chronoIsCounting to true.
     * set seekbar max to the duration of the track playing. Divide it by 1000 to convert from milisec to sec.
     * and start chronometer.
     *
     */
    private void startChronometerAndSeekBar(){
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronoIsCounting = true;
        seekbar.setMax(mService.mediaPlayer.getDuration()/1000);
        chronometer.start();
    }

    /**
     * set TimeWhenStopped to start state.
     * set chornoiscounting to false.
     * stop chronometer and set its base to elapsed milliseconds since boot.
     */
    private void stopChronometer(){
        timeWhenStopped = START_STATE;
        chronoIsCounting = false;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    /**
     * if chronometer is counting
     * set timewhenstoppen to chronomters base minus elapsed milliseconds since boot
     * stop chornometer
     * and set chornoiscounting to false.
     */
    private void pauseChromometer(){
        if (chronoIsCounting){
            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
            chronoIsCounting = false;
        }
    }

    /**
     *   Take chronomters base minus elapsed milliseconds since boot and divide it by 1000.
     *   When its bigger than 0 one sec will have passed. (when casting to int 0.9 == 0)
     *   on every tick that is bigger than 0
     *
     * @param chronometer
     */
    @Override
    public void onChronometerTick(Chronometer chronometer) {
        int elapsedSeconds = (int) (SystemClock.elapsedRealtime() - chronometer.getBase())/1000;
        if (elapsedSeconds > START_STATE){
            seekbar.setProgress(elapsedSeconds);
            if(elapsedSeconds >= mService.mediaPlayer.getDuration()/1000){
                stopChronometer();
                seekbar.setProgress(START_STATE);
                startChronometerAndSeekBar();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * When user changes seekbar and let it go:
     * if mediaplayer is playing:
     * stopChronometer(), set timeWhenStop to minus seekbars progress *1000 (we want it in milisec)
     * startChronometer()
     * use mediaplayers seekTo() and send it seekbars progress *1000
     *
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mService.mediaPlayer.isPlaying()){
            stopChronometer();
            timeWhenStopped = -seekbar.getProgress()*1000;
            startChronometerAndSeekBar();
            mService.mediaPlayer.seekTo(seekBar.getProgress()*1000);
        }
    }
}
