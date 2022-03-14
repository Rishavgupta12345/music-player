package com.example.apnamusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.apnamusicplayer.Application.ACTION_NEXT;
import static com.example.apnamusicplayer.Application.ACTION_PLAY;
import static com.example.apnamusicplayer.Application.ACTION_PREVIOUS;

public class Notificationreceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String actionname = intent.getAction();
        Intent serviceIntent = new Intent(context,MusicService.class);
        if(actionname != null)
        {
            switch (actionname)
            {
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName","playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName","next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName","previous");
                    context.startService(serviceIntent);
                    break;
            }
        }

    }
}
