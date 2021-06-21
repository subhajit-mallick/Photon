package com.subhajit.photon.activities;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.subhajit.photon.R;
import com.subhajit.photon.fragments.homeFrag;

public class notificationProvider {

    public static void displayNotification(Context context, String title, String body){

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, HomeActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_photon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1,builder.build());
    }
}
