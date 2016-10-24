package com.rorysoft.glidemusicplayer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rorysoft.glidemusicplayer.R;
import com.rorysoft.glidemusicplayer.Song;

import java.util.ArrayList;

/**
 * Created by Alexander on 8/28/16.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songList;
    private LayoutInflater inflater;

    public SongAdapter(Context context, ArrayList<Song> songList) {
        this.songList = songList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songView = (LinearLayout) inflater.inflate(R.layout.song, parent, false);

        TextView songTitle = (TextView) songView.findViewById(R.id.songTitle);
        TextView songArtist = (TextView) songView.findViewById(R.id.songArtist);
        TextView songAlbum = (TextView) songView.findViewById(R.id.songAlbum);

        Song song = songList.get(position);

        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());
        songAlbum.setText(song.getAlbum());

        songView.setTag(position);
        return songView;
    }
}
