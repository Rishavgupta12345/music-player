package com.example.apnamusicplayer;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.example.apnamusicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.example.apnamusicplayer.MainActivity.PATH_TO_FRAG;
import static com.example.apnamusicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.example.apnamusicplayer.MainActivity.SONG_TO_FRAG;


public class nowplayingFragment extends Fragment {


    ImageView nextbtn, albumArt;
    TextView artistname, songname;
    FloatingActionButton playPausebtn;
    View view;

    public nowplayingFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_nowplaying, container, false);
        artistname = view.findViewById(R.id.song_artist_miniPlayer);
        songname = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextbtn = view.findViewById(R.id.skip_next_bottom);
        playPausebtn = view.findViewById(R.id.play_pause_miniplayer);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"next",Toast.LENGTH_SHORT).show();
            }
        });
        playPausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"playpause",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER)
        {
            if (PATH_TO_FRAG != null)
            {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null)
                {
                    Glide.with(getContext()).load(art).into(albumArt);
                }
                else
                {
                    Glide.with(getContext()).load(R.drawable.maja).into(albumArt);
                }
                songname.setText(SONG_TO_FRAG);
                artistname.setText(ARTIST_TO_FRAG);

            }
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }


}