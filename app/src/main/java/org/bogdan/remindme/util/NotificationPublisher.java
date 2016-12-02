package org.bogdan.remindme.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.R;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bodia on 12.11.2016.
 */

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";

    @Override
     public void onReceive(final Context context, Intent intent) {
        JodaTimeAndroid.init(context);
        //Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(notificationId, notification);

        //Create next notification
        List<UserVK> userVKList = new ArrayList<>();
        DBHelper.readUserVKTable(context, userVKList);

        List<UserVK> userVKListFull = UserVK.getUserVKListFull(userVKList);
        Collections.sort(userVKListFull);

        scheduleNotification(context, dayToMillis(userVKListFull.get(0).getDayToNextBirht()),0,userVKListFull.get(0).getAvatarURL());
    }

    private static NotificationCompat.Builder builder;

    public static void scheduleNotification(Context context, long delay, int notificationId, String largeIconUrl) {

        builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.str_birthday))
                .setContentText(UserVK.getUsersList().get(notificationId).getName())
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Picasso.with(context).load(largeIconUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.setLargeIcon(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {


            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    public static long dayToMillis(long day){
        LocalDateTime now = new LocalDateTime();
        int hours,minute,second;
        hours = now.getHourOfDay();
        minute = now.getMinuteOfHour();
        second = now.getSecondOfMinute();
        long millis = day*86400000-(3600000*hours+60000*minute+1000*second);

        return millis;
    }
}
