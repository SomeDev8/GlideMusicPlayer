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
 * Created by Alexander on 9/12/16.
 */
public class ArtistAdapter extends BaseAdapter {

    private ArrayList<Song> songList;
    private LayoutInflater inflater;

    public ArtistAdapter(Context context, ArrayList<Song> songList) {
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
        LinearLayout artistView = (LinearLayout) inflater.inflate(R.layout.artist, parent, false);

        TextView songArtist = (TextView) artistView.findViewById(R.id.songArtist);
        Song song = songList.get(position);
        songArtist.setText(song.getArtist());

        artistView.setTag(position);

        return artistView;

    }
}
