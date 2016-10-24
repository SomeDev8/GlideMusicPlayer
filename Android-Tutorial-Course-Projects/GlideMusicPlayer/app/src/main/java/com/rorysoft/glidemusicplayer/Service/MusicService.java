package com.rorysoft.glidemusicplayer.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rorysoft.glidemusicplayer.PlayerActivity;
import com.rorysoft.glidemusicplayer.R;
import com.rorysoft.glidemusicplayer.Song;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Alexander on 8/26/16.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String TAG = MusicService.class.getName();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songList;
    private int songIndex = 0;
    private final IBinder musicBind = new MusicBinder();

    private boolean shuffle = false;
    private Random random;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
        random = new Random();
    }

    public void initMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void playMusic() {
        mediaPlayer.reset();

        Song playSong = songList.get(songIndex);
        long currSong = playSong.getId();

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (Exception e) {
            Log.e(TAG, "Error setting the data source", e);
        }

        mediaPlayer.prepareAsync();

        // ****** ISSUE? *******

        songTitle = playSong.getTitle();
    }

    public void setSong(int songIndex) {
        this.songIndex = songIndex;
    }

    public int getSongPos() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getSongDur() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mediaPlayer.pause();
    }

    public void seek(int pos) {
        mediaPlayer.seekTo(pos);
    }

    public void start() {
        mediaPlayer.start();
    }

    // **** We an add shuffle function here as well

    public void playPrev() {
        songIndex--;
        if (songIndex < 0) {
            songIndex = songList.size() - 1;
            playMusic();
        }
    }

    // **** Better way to do this for shuffle function?

    public void playNext() {
        if (shuffle) {
            int newSong = songIndex;
            while (newSong == songIndex) {
                newSong = random.nextInt(songList.size());
            }
            songIndex = newSong;
        } else {
            songIndex++;
            if (songIndex >= songList.size()) {
                songIndex = 0;
            }
        }
        playMusic();
    }

    public void toggleShuffle() {
        if (shuffle) {
            shuffle = false;
        } else
            shuffle = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mediaPlayer.getCurrentPosition() < 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.robot)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        Notification notification = builder.build();

        startForeground(NOTIFY_ID, notification);
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
