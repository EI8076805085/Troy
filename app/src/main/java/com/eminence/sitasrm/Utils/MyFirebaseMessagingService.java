package com.eminence.sitasrm.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.eminence.sitasrm.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String badge;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        PendingIntent pendingIntent = null;
       //  Log.i("notificationsss","hewkkh");
        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());

        //get data
        Map<String, String> data = remoteMessage.getData();
       //  String type = data.get("type").toString();
        String title = data.get("title").toString();
        String body = data.get("body").toString();
       // String id = data.get("id").toString();

        badge=yourPrefrence.getData("badge");

        if (badge.equalsIgnoreCase("")) {
            badge="0";
        }
        int plusbadge=Integer.parseInt(badge)+1;
        yourPrefrence.saveData("badge",String.valueOf(plusbadge));
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }
}