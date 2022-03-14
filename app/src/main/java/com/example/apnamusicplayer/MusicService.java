package com.example.apnamusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static com.example.apnamusicplayer.Application.ACTION_NEXT;
import static com.example.apnamusicplayer.Application.ACTION_PLAY;
import static com.example.apnamusicplayer.Application.ACTION_PREVIOUS;
import static com.example.apnamusicplayer.Application.CHANNEL_ID_2;
import static com.example.apnamusicplayer.PlayerActivity.listSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MYBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String SONG_NAME = "SONG_NAME";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");
       // musicFiles = listSongs;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.e("Bind","method");
        return mBinder;
    }



    public class MYBinder extends Binder
    {
        MusicService getService()
        {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");

        if(myPosition != -1)
        {
            playMedia(myPosition);
        }
        if(actionName != null)
        {
            switch (actionName)
            {
                case "playPause":
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null)
                    {
                        Log.e("Inside","Action");
                        actionPlaying.playPauseBtnCLicked();
                    }
                    break;
                case "next":
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null)
                    {
                        Log.e("Inside","Action");
                        actionPlaying.nextBtnCLicked();
                    }
                    break;
                case "previous":
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    if(actionPlaying != null)
                    {
                        Log.e("Inside","Action");
                        actionPlaying.prevPauseBtnCLicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int Startposition)
    {
        musicFiles = listSongs;
        position = Startposition;
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else
            {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start()
    {
        mediaPlayer.start();
    }
    boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }
    void stop()
    {
        mediaPlayer.stop();
    }
    void release()
    {
        mediaPlayer.release();
    }
    void pause()
    {
        mediaPlayer.pause();
    }
    int getDuration()
    {
        return mediaPlayer.getDuration();
    }
    void seekTo(int position)
    {
       mediaPlayer.seekTo(position);
    }
    int getCurrentPosition()
    {
        return  mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner)
    {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE,uri.toString());
       // editor.apply();
        editor.putString(ARTIST_NAME,musicFiles.get(position).getArtist());
       // editor.apply();
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.apply();

        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }

    void Oncompleted()
    {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        if(actionPlaying != null)
        {
            actionPlaying.nextBtnCLicked();
            if(mediaPlayer != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
                Oncompleted();
            }
        }

    }

    void setCallback(ActionPlaying actionPlaying)
    {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPausebtn)
    {
        Intent intent = new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);

        Intent prevIntent = new Intent(this,Notificationreceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this,Notificationreceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this,Notificationreceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null)
        {
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);

        }
        else
        {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.maja);
        }

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPausebtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"Previous",prevPending)
                .addAction(playPausebtn,"Pause",pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);

        //startForeground(0,notification);




    }

    private  byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}


