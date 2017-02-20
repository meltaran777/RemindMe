package org.bogdan.remindme.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.R;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.app.Notification.DEFAULT_VIBRATE;

/**
 * Created by Bodia on 12.11.2016.
 */

public class NotificationPublisher {

    public static final String DISPLAY_NOTIFICATION_ACTION = "org.bogdan.remindme.DISPLAY_NOTIFICATION";
    public static final String DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION = "org.bogdan.remindme.HAPPY_BIRTHDAY_DIALOG_SHOW";

    static String notificationSound = "content://settings/system/notification_sound";
    static boolean vibration = true;

    private static int generateRandomId() {
        int notifId = 0;
        for (int i=0; i<10; i++) notifId = new Random().nextInt();
        return notifId;
    }

    public static void showUserVkBirthdayNotification(final Context context, UserVK userVK) throws IOException {

        final NotificationCompat.Builder builder;

        final int notificationContentIntentId = generateRandomId();
        final int notificationId = userVK.getId();
        boolean original = userVK.isNotify();
        String largeIconUrl = userVK.getAvatarURL();

        builder = new NotificationCompat.Builder(context)
                .setContentText(userVK.getName())
                .setAutoCancel(true)
                .setLargeIcon(Picasso.with(context).load(largeIconUrl).get())
                .setSmallIcon(R.drawable.ic_birthday)
                .setSound(Uri.parse(notificationSound));
        if (vibration) builder.setDefaults(DEFAULT_VIBRATE);
        if (original) builder.setContentTitle(context.getString(R.string.str_birthday));
        else builder.setContentTitle(context.getString(R.string.str_birthday_soon));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (original) intent.setAction(DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION); else intent.setAction(DISPLAY_NOTIFICATION_ACTION);
        if (original) intent.putExtra("action", DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION);
        intent.putExtra("userId", userVK.getId());
        intent.putExtra("userName", userVK.getName());
        intent.putExtra("userAvatarURL", userVK.getAvatarURL());

        PendingIntent activity = PendingIntent.getActivity(context, notificationContentIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, notification);
    }

    public static void displayAlarmNotification(Context context, int notificationId) {

        JodaTimeAndroid.init(context);

        LocalTime time = new LocalTime();
        time = time.plusMinutes(10);
        String strHour = String.valueOf(time.getHourOfDay());
        String strMinute = String.valueOf(time.getMinuteOfHour());
        if (time.getMinuteOfHour() < 10) strMinute = "0" + time.getMinuteOfHour();
        if (time.getHourOfDay() < 10) strHour = "0" + time.getHourOfDay();
        String strTime = strHour + ":" + strMinute;

        Intent actionIntent = new Intent(context, NotificationActionService.class)
                .putExtra("id", notificationId)
                .setAction(DISPLAY_NOTIFICATION_ACTION);

        PendingIntent actionPendingIntent = PendingIntent.getService(context, notificationId,
                actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alarm_notif)
                        .setContentTitle("Alarm(Set aside) " + strTime)
                        .setContentText("Push to cancel alarm")
                        .setSound(Uri.parse(notificationSound))
                        .setAutoCancel(true)
                        .setContentIntent(actionPendingIntent);
        if (vibration) notificationBuilder.setDefaults(DEFAULT_VIBRATE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationManagerCompat.from(context).cancelAll();

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
