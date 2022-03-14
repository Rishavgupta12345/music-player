package com.example.apnamusicplayer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Random;
import static com.example.apnamusicplayer.Application.ACTION_NEXT;
import static com.example.apnamusicplayer.Application.ACTION_PLAY;
import static com.example.apnamusicplayer.Application.ACTION_PREVIOUS;
import static com.example.apnamusicplayer.Application.CHANNEL_ID_2;
import static com.example.apnamusicplayer.MainActivity.musicFiles;
import static com.example.apnamusicplayer.MainActivity.repeatBoolean;
import static com.example.apnamusicplayer.MainActivity.shuffleBoolean;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    private TextView song_name,artist_name,duration_played,duration_total;
    private ImageView cover_art,nextBtn,Prevbtn,backBtn,shuffleBtn,repeatBtn;
    private FloatingActionButton playPauseBrn;
    private SeekBar seekBar;
    private int position = -1;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    public static Uri uri;
    //static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread,prevThread,nextThread;
    MusicService musicService;

    //MediaSessionCompat mediaSessionCompat;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        initView();
        getIntentMethod();
        //mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"MY audio");
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if(musicService != null && fromUser)
                {
                    musicService.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                if(musicService != null)
                {
                    int mcurrentPosition = musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mcurrentPosition);
                    duration_played.setText(formattedTIme(mcurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               if(shuffleBoolean)
               {
                   shuffleBoolean = false;
                   shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_24);
               }
               else
               {
                   shuffleBoolean = true;
                   shuffleBtn.setImageResource(R.drawable.suffleon);
               }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(repeatBoolean)
                {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24);
                }
                else
                {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                }
            }
        });
    }

    private void setFullScreen()
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume()
    {
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playTHreadBtn();
        nextThreadben();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn()
    {
        prevThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                Prevbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevPauseBtnCLicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevPauseBtnCLicked()
    {
        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();

            if(shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean)
            {
                position = ((position-1) <0 ?(listSongs.size() - 1) :(position -1));
            }


            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());

            seekBar.setMax(musicService.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    if(musicService != null)
                    {
                        int mcurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mcurrentPosition);

                    }
                    handler.postDelayed(this,1000);
                }
            });
            playPauseBrn.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
        }
        else
        {
            musicService.stop();
            musicService.release();

            if(shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean)
            {
                position = ((position-1) <0 ?(listSongs.size() - 1) :(position -1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());

            seekBar.setMax(musicService.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    if(musicService != null)
                    {
                        int mcurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mcurrentPosition);

                    }
                    handler.postDelayed(this,1000);
                }
            });
            playPauseBrn.setImageResource(R.drawable.ic_baseline_play_arrow_24);

        }
    }

    private void nextThreadben()
    {
        nextThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnCLicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnCLicked()
    {
       if(musicService.isPlaying())
       {
           musicService.stop();
           musicService.release();

           if(shuffleBoolean && !repeatBoolean)
           {
               position = getRandom(listSongs.size() - 1);
           }
           else if (!shuffleBoolean && !repeatBoolean)
           {
               position = ((position + 1) % listSongs.size());
           }



           uri = Uri.parse(listSongs.get(position).getPath());
           musicService.createMediaPlayer(position);
           metaData(uri);
           song_name.setText(listSongs.get(position).getTitle());
           artist_name.setText(listSongs.get(position).getArtist());

           seekBar.setMax(musicService.getDuration()/1000);

           PlayerActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run()
               {
                   if(musicService != null)
                   {
                       int mcurrentPosition = musicService.getCurrentPosition()/1000;
                       seekBar.setProgress(mcurrentPosition);

                   }
                   handler.postDelayed(this,1000);
               }
           });
           musicService.Oncompleted();
           musicService.showNotification(R.drawable.ic_baseline_pause_24);
           playPauseBrn.setImageResource(R.drawable.ic_baseline_pause_24);
           musicService.start();
       }
       else
       {
           musicService.stop();
           musicService.release();

           if(shuffleBoolean && !repeatBoolean)
           {
               position = getRandom(listSongs.size()-1);
           }
           else if (!shuffleBoolean && !repeatBoolean)
           {
               position = ((position + 1) % listSongs.size());
           }

           uri = Uri.parse(listSongs.get(position).getPath());
           musicService.createMediaPlayer(position);
           metaData(uri);
           song_name.setText(listSongs.get(position).getTitle());
           artist_name.setText(listSongs.get(position).getArtist());

           seekBar.setMax(musicService.getDuration()/1000);

           PlayerActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run()
               {
                   if(musicService != null)
                   {
                       int mcurrentPosition = musicService.getCurrentPosition()/1000;
                       seekBar.setProgress(mcurrentPosition);

                   }
                   handler.postDelayed(this,1000);
               }
           });
           musicService.Oncompleted();
           musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
           playPauseBrn.setImageResource(R.drawable.ic_baseline_play_arrow_24);

       }
    }

    private int getRandom(int i)
    {
        Random random = new Random();
        return random.nextInt( i + 1);

    }

    private void playTHreadBtn()
    {
        playThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                playPauseBrn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnCLicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnCLicked()
    {
       if(musicService.isPlaying())
       {
           playPauseBrn.setImageResource(R.drawable.ic_baseline_play_arrow_24);

           musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
           musicService.pause();
           seekBar.setMax(musicService.getDuration()/1000);

           PlayerActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run()
               {
                   if(musicService != null)
                   {
                       int mcurrentPosition = musicService.getCurrentPosition()/1000;
                       seekBar.setProgress(mcurrentPosition);

                   }
                   handler.postDelayed(this,1000);
               }
           });
       }
       else
       {

           musicService.showNotification(R.drawable.ic_baseline_pause_24);
           playPauseBrn.setImageResource(R.drawable.ic_baseline_pause_24);
           musicService.start();
           seekBar.setMax(musicService.getDuration()/1000);

           PlayerActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run()
               {
                   if(musicService != null)
                   {
                       int mcurrentPosition = musicService.getCurrentPosition()/1000;
                       seekBar.setProgress(mcurrentPosition);

                   }
                   handler.postDelayed(this,1000);
               }
           });
       }
    }

    private String formattedTIme(int mcurrentPosition)
    {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mcurrentPosition % 60);
        String minutes = String.valueOf(mcurrentPosition/60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1)
        {
            return totalNew;
        }
        else
        {
           return totalout;
        }

    }


    private void getIntentMethod()
    {
        position = getIntent().getIntExtra("position",-1);
        listSongs = musicFiles;
//        String sender = getIntent().getStringExtra("sender");
//        if(sender !=null && sender.equals("albumDetails"))
//        {
//            listSongs = albumFiles;
//        }
//        else
//        {
//            listSongs = mFiles;
//        }
//
//      //  listSongs = musicFiles;

        if(listSongs != null)
        {
            playPauseBrn.setImageResource(R.drawable.ic_baseline_pause_24);
            uri = Uri.parse(listSongs.get(position).getPath());
        }

        Intent intent = new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

//        if(musicService != null)
//        {
//            musicService.stop();
//            musicService.release();
//            musicService.createMediaPlayer(position);
//            musicService.start();
//        }
//        else
//        {
//            musicService.createMediaPlayer(position);
//            musicService.start();
//        }


    }


    private void initView()
    {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        Prevbtn = findViewById(R.id.id_prev);
       // backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBrn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);

    }

    private void metaData(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationToral = Integer.parseInt(listSongs.get(position).getDuration())/1000;
        duration_total.setText(formattedTIme(durationToral));
        byte[] art = retriever.getEmbeddedPicture();

        Bitmap bitmap;

        if(art != null)
        {
            Glide.with(this).asBitmap().load(art).into(cover_art);


        }
        else
        {
            Glide.with(this).asBitmap().load(R.drawable.maja).into(cover_art);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
       MusicService.MYBinder myBinder = (MusicService.MYBinder)service;
       musicService = myBinder.getService();
       musicService.setCallback(this);
       // Toast.makeText(this,"Connected" + musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.Oncompleted();

        musicService.showNotification(R.drawable.ic_baseline_pause_24);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        musicService = null;
    }




}