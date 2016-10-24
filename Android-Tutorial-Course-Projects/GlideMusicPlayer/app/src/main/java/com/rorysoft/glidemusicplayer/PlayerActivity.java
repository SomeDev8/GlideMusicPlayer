package com.rorysoft.glidemusicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.rorysoft.glidemusicplayer.Adapter.ArtistAdapter;
import com.rorysoft.glidemusicplayer.Adapter.SongAdapter;
import com.rorysoft.glidemusicplayer.Service.MusicService;
import com.rorysoft.glidemusicplayer.Service.MusicService.MusicBinder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlayerActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private ListView songListView;
    private ArrayList<Song> songList;
    private MusicService musicService;
    private Intent intent;
    private boolean isBound = false;

    private Button play;
    private Button next;
    private Button prev;
    private Button artistButton;
    private Button albumButton;
    private Button songButton;

    private Button shuffle;
    private boolean activityPaused = false;
    private boolean playbackPaused = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        songListView = (ListView) findViewById(R.id.songList);
        songList = new ArrayList<Song>();
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingList);
        artistButton = (Button) findViewById(R.id.artistButton);
        albumButton = (Button) findViewById(R.id.albumButton);
        songButton = (Button) findViewById(R.id.songButton);

        play = (Button) findViewById(R.id.play);
        next = (Button) findViewById(R.id.next);
        prev = (Button) findViewById(R.id.prev);
        shuffle = (Button) findViewById(R.id.shuffle);


        final int PERMISSIONS_CODE = 123;

        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(PlayerActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_CODE);
                return;
            }
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (intent == null) {
            intent = new Intent(this, MusicService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityPaused) {
            //   initController();
            activityPaused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
    }

    @Override
    protected void onStop() {
        //    musicController.hide();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        initSongList();
                        initPanel();
                        initAdapter();
                        initShuffle();
                        initControls();
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                        alertDialog.setTitle("NEED ACCESS");
                        alertDialog.setMessage("This application needs access to your device's storage, so it can play your music");
                        alertDialog.create();
                        alertDialog.show();
                       // alertDialog.setPositiveButton()
                    }
                }
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            Log.d("SERVICE CONNECTION", "HERE");

            musicService = binder.getService();

            musicService.setSongList(songList);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void initControls() {
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playbackPaused) {
                    musicService.start();
                    playbackPaused = false;
                } else {
                    musicService.pausePlayer();
                    playbackPaused = true;
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playNext();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playPrev();
            }
        });
    }

    // Initialize the panel to slide up and reveal list view & other views

    private void initPanel() {
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
    }

    private void initSongList() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);

            do {
                long songId = cursor.getLong(idColumn);
                String songTitle = cursor.getString(titleColumn);
                String songArtist = cursor.getString(artistColumn);
                String songAlbum = cursor.getString(albumColumn);
                songList.add(new Song(songId, songTitle, songArtist, songAlbum));
            } while (cursor.moveToNext());
        }
        Log.d("SONGS", "GOT SONGS");
    }

    private void initShuffle() {
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.toggleShuffle();
            }
        });
    }

    private void sortSong() {
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    private void sortArtist() {
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getArtist().compareTo(b.getArtist());
            }
        });
    }

    public void songSelected(View view) {

        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playMusic();

        if (playbackPaused) {
            //   initController();
            playbackPaused = false;
        }
        //  musicController.show(0);
    }

    public void initAdapter() {

        songButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortSong();
                SongAdapter songAdapter = new SongAdapter(PlayerActivity.this, songList);
                songListView.setAdapter(songAdapter);
            }
        });

        artistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortArtist();
                ArtistAdapter artistAdapter = new ArtistAdapter(PlayerActivity.this, songList);
                songListView.setAdapter(artistAdapter);
            }
        });

    }


//    @Override
//    public int getDuration() {
//        if (musicService != null && isBound && musicService.isPlaying()) {
//            return musicService.getSongDur();
//        } else {
//            return 0;
//        }
//    }
//
//    @Override
//    public int getCurrentPosition() {
//        if (musicService != null && isBound && musicService.isPlaying()) {
//            return musicService.getSongPos();
//        } else {
//            return 0;
//        }
//    }
//
//    @Override
//    public void seekTo(int pos) {
//        musicService.seek(pos);
//    }
//
//    @Override
//    public boolean isPlaying() {
//        if (musicService != null && isBound) {
//            return musicService.isPlaying();
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public int getBufferPercentage() {
//        return 0;
//    }
//
//    @Override
//    public boolean canPause() {
//        return true;
//    }
//
//    @Override
//    public boolean canSeekBackward() {
//        return true;
//    }
//
//    @Override
//    public boolean canSeekForward() {
//        return true;
//    }
//
//    @Override
//    public int getAudioSessionId() {
//        return 0;
//    }
}
