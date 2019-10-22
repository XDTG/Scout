package com.example.tg.scout;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("MyService","MyService onCreate executed");
        NotificationManager manager = (NotificationManager)getSystemService
                (NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("default",
                    "mchannel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification_channel_description");
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this,MainActivity.class);
        Notification notification = new NotificationCompat.Builder(this,"default")
                .setContentTitle("WiFi-Map")
                .setContentText("WiFi-Map is running")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .build();
        startForeground(1,notification);
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d("MyService","MyService onStartCommand executed");

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){
        Log.d("MyService","MyService Destroy");
        super.onDestroy();
    }
}
