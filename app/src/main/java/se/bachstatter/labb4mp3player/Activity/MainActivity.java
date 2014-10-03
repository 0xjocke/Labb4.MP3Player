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
    private static final int PLAY_BTN_POS = 1;
    ArrayList<Track> trackList;
    private boolean isPlaying = false;
    private int currentTrack = 0;
    private Menu menu;
    // TODO  change name
    private SeekBar seekbar;

    //Chronometer variables
    private Chronometer chronometer;
    private Boolean chronoIsCounting = false;
    private long timeWhenStopped = 0;

    MusicService mService;
    boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(this);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(this);
        startAndBindIntent();
        TrackListHelper trackListHelper = new TrackListHelper(getApplicationContext());
        if(trackListHelper.isExternalStorageWritable()){
            Log.d("MainActivity onCreate ", "Sd card available " );
            trackList = trackListHelper.getPlaylist();
            MusicAdapter musicAdapter = new MusicAdapter(this, trackList);
            setListAdapter(musicAdapter);
        }

    }

    private void startAndBindIntent(){
           Intent intentService = new Intent(this, MusicService.class);
           startService(intentService);
           bindService(intentService, mConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            mService.stopSelf();
            unbindService(mConnection);
            mBound = false;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        currentTrack = position;
        mService.musicPlayer(trackList.get(currentTrack));
        menu.getItem(PLAY_BTN_POS).setIcon(R.drawable.ic_action_pause);
        stopChronometer();
        startChronometerAndSeekBar();

        isPlaying = true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
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


    private void stopBtnAction(){
        mService.mediaPlayer.stop();
        mService.currentTrack = null;
        stopChronometer();
        seekbar.setProgress(0);
        menu.getItem(PLAY_BTN_POS).setIcon(R.drawable.ic_action_play);
        isPlaying = false;

    }
    private void prevBtnAction(){
        if(!isPlaying || currentTrack == 0)
            return;
        mService.musicPlayer(trackList.get(--currentTrack));
        stopChronometer();
        startChronometerAndSeekBar();
    }
    /**
     * NextBtnAction
     * If music not playing retrun without doing anything.
     *
     */
    private void nextBtnAction() {
        if(!isPlaying || currentTrack == trackList.size()-1)
            return;
        //TODO constat for -1
        mService.musicPlayer(trackList.get(++currentTrack));
        stopChronometer();
        startChronometerAndSeekBar();
    }

    private void playBtnAction(MenuItem item){
        if (isPlaying) {
            mService.mediaPlayer.pause();
            pauseChromometer();
            item.setIcon(R.drawable.ic_action_play);
            isPlaying = false;
        } else {
            if(mService.currentTrack == null){
                mService.musicPlayer(trackList.get(0));
                startChronometerAndSeekBar();
                item.setIcon(R.drawable.ic_action_pause);
                isPlaying = true;
            }else{
                mService.mediaPlayer.start();
                startChronometerAndSeekBar();
                item.setIcon(R.drawable.ic_action_pause);
                isPlaying = true;
            }

        }
    }

    private void startChronometerAndSeekBar(){
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronoIsCounting = true;
        seekbar.setMax(mService.mediaPlayer.getDuration()/1000);
        chronometer.start();
    }
    private void stopChronometer(){
        //reset the timewhenstop variable
        // set mChronometer is counting to false
        //stop the mChronometer
        // and reset the time
        timeWhenStopped = 0;
        chronoIsCounting = false;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    private void pauseChromometer(){
        if (chronoIsCounting){
            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
            chronoIsCounting = false;
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        int elapsedSeconds = (int) (SystemClock.elapsedRealtime() - chronometer.getBase())/1000;
        if (elapsedSeconds >= 1){
            seekbar.setProgress(elapsedSeconds);
            if(elapsedSeconds >= mService.mediaPlayer.getDuration()/1000){
                stopChronometer();
                seekbar.setProgress(0);
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

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (isPlaying){
            stopChronometer();
            timeWhenStopped = -seekbar.getProgress()*1000;
            startChronometerAndSeekBar();
            mService.mediaPlayer.seekTo(seekBar.getProgress()*1000);
        }
    }
}
