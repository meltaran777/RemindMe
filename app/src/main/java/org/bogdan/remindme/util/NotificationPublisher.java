package org.bogdan.remindme.util;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.R;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bodia on 12.11.2016.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static final String DISPLAY_NOTIFICATION_ACTION = "org.bogdan.remindme.DISPLAY_NOTIFICATION";
    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(final Context context, Intent intent) {
        JodaTimeAndroid.init(context);
        //Create next notification
        List<UserVK> userVKList = new ArrayList<>();
        DBHelper.readUserVKTable(context, userVKList);
        DBHelper.closeDB();

        List<UserVK> userVKListFull = UserVK.getUserVKListFull(userVKList);
        Collections.sort(userVKListFull);
        UserVK userVK = userVKListFull.get(0);

        scheduleNotification(context, 0, userVK);
        //Show notification
        if (!intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(notificationId, notification);
        }

    }

    public static void scheduleNotification(Context context, int notificationId, UserVK userVK) {
        final NotificationCompat.Builder builder;
        long delay = dayToMillis(userVK.getDayToNextBirht());
        boolean original = userVK.isNotify();
        String largeIconUrl = userVK.getAvatarURL();

        builder = new NotificationCompat.Builder(context)
                .setContentText(userVK.getName())
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (original) builder.setContentTitle(context.getString(R.string.str_birthday));
        else builder.setContentTitle(context.getString(R.string.str_birthday_soon));
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

    public static void displayAlarmNotification(Context context, int notificationId) {

        Intent actionIntent = new Intent(context, NotificationActionService.class)
                .putExtra("id",notificationId)
                .setAction(DISPLAY_NOTIFICATION_ACTION);


        PendingIntent actionPendingIntent = PendingIntent.getService(context, notificationId,
                actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Alarm(Set aside)")
                        .setContentText("Push to cancel alarm")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true)
                        .setContentIntent(actionPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationManagerCompat.from(context).cancelAll();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public static long dayToMillis(long day) {
        LocalDateTime now = new LocalDateTime();
        int hours, minute, second, millisOfSecond;
        hours = now.getHourOfDay();
        minute = now.getMinuteOfHour();
        second = now.getSecondOfMinute();
        millisOfSecond = now.getMillisOfSecond();

        long millis = day * 86400000 - (3600000 * hours + 60000 * minute + 1000 * second + millisOfSecond);
        return millis;
    }

}
