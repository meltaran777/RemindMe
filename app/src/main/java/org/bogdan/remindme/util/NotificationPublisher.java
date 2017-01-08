package org.bogdan.remindme.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.R;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.app.Notification.DEFAULT_VIBRATE;

/**
 * Created by Bodia on 12.11.2016.
 */

public class NotificationPublisher extends BroadcastReceiver {

    public static final String DISPLAY_NOTIFICATION_ACTION = "org.bogdan.remindme.DISPLAY_NOTIFICATION";
    public static final String DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION = "org.bogdan.remindme.HAPPY_BIRTHDAY_DIALOG_SHOW";

    public static int NOTIFICATION_ID = 999;
    public static String NOTIFICATION_ID_TAG = "notification_id";
    public static String NOTIFICATION = "notification";

    static String ringtone = "content://settings/system/notification_sound";
    static boolean vibration = true;
    static Bitmap avatar;

    @Override
    public void onReceive(final Context context, Intent intent) {
        JodaTimeAndroid.init(context);

        //Show notification
        if (!intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")) {

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            int notificationId = intent.getIntExtra(NOTIFICATION_ID_TAG, 0);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(notificationId, notification);
        }

        //Create next notification
        List<UserVK> userVKList = new ArrayList<>();
        DBHelper.readUserVKTable(context, userVKList);
        DBHelper.closeDB();

        List<UserVK> userVKListFull = UserVK.getUserVKListFull(userVKList);
        Collections.sort(userVKListFull);
        UserVK userVK = userVKListFull.get(0);

        for (UserVK user : userVKListFull) {
            Log.d("NotificationDebug", "onReceive: "+ user.getName()+" "+String.valueOf(user.isNotify())+" "+user.getBirthDate().toString("dd/MM"));
        }

        scheduleNotification(context, userVK);

        for (int i = 1; i < userVKListFull.size(); i++){

            String firstUserBirthDate = userVK.getBirthDate().toString("dd/MM");
            String currentUserBirthDate = userVKListFull.get(i).getBirthDate().toString("dd/MM");

            if (firstUserBirthDate.equals(currentUserBirthDate))
                scheduleNotification(context, userVKListFull.get(i));
        }
    }

    private static int generateRandomId() {
        int notifId = 0;
        for (int i=0; i<10; i++) notifId = new Random().nextInt();
        return notifId;
    }

    public static void scheduleNotification(final Context context, UserVK userVK) {
        final NotificationCompat.Builder builder;

        final long delay = dayToMillis(userVK.getDayToNextBirht());
        final int notificationContentIntentId = generateRandomId();
        final int notificationId = userVK.getId();
        boolean original = userVK.isNotify();
        String largeIconUrl = userVK.getAvatarURL();

        Log.d("NotificationDebug", "scheduleNotification: "+userVK.getName()+" "+String.valueOf(original)+" "+userVK.getBirthDate().toString("dd/MM"));

        builder = new NotificationCompat.Builder(context)
                .setContentText(userVK.getName())
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_birthday)
                .setSound(Uri.parse(ringtone));
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

        Picasso.with(context).load(largeIconUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                builder.setLargeIcon(bitmap);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(context, NotificationPublisher.class);
                notificationIntent.setAction(DISPLAY_NOTIFICATION_ACTION);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID_TAG, notificationId);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                long futureInMillis = SystemClock.elapsedRealtime() + delay;
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(context, NotificationPublisher.class);
                notificationIntent.setAction(DISPLAY_NOTIFICATION_ACTION);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID_TAG, notificationId);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                long futureInMillis = SystemClock.elapsedRealtime() + delay;
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}

        });
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
                        .setSound(Uri.parse(ringtone))
                        .setAutoCancel(true)
                        .setContentIntent(actionPendingIntent);
        if (vibration) notificationBuilder.setDefaults(DEFAULT_VIBRATE);

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
        //millisOfSecond = now.getMillisOfSecond();
        //long millis = day * 86400000 - (3600000 * hours + 60000 * minute + 1000 * second + millisOfSecond);
        long millis = (day * 86400000 - (3600000 * hours + 60000 * minute + 1000 * second) + 5000);

        return millis;

    }



    private static void setAvatar(Bitmap bitmap) {
        avatar = bitmap;
    }
    private static void setAvatar(Context context) {
        avatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_birthday);;
    }
    public static void testScheduleNotification(final Context context, UserVK userVK) {
        final NotificationCompat.Builder builder;

        boolean original = userVK.isNotify();
        int notificationContentIntentId = generateRandomId();
        final int notificationId = userVK.getId();
        String largeIconUrl = userVK.getAvatarURL();

        builder = new NotificationCompat.Builder(context)
                .setContentText(userVK.getName())
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_birthday)
                .setSound(Uri.parse(ringtone));
        if (vibration) builder.setDefaults(DEFAULT_VIBRATE);
        if (original) builder.setContentTitle(context.getString(R.string.str_birthday));else builder.setContentTitle(context.getString(R.string.str_birthday_soon));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (original) intent.setAction(DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION); else intent.setAction(DISPLAY_NOTIFICATION_ACTION);
        if (original) intent.putExtra("action", DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION);
        intent.putExtra("userId", userVK.getId());
        intent.putExtra("userName", userVK.getName());
        intent.putExtra("userAvatarURL", userVK.getAvatarURL());

        PendingIntent activity = PendingIntent.getActivity(context, notificationContentIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Picasso.with(context).load(largeIconUrl).into(new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                builder.setLargeIcon(bitmap);

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(context, NotificationPublisher.class);
                notificationIntent.setAction(DISPLAY_NOTIFICATION_ACTION);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID_TAG, notificationId);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                long futureInMillis = SystemClock.elapsedRealtime();
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

                Notification notification = builder.build();

                Intent notificationIntent = new Intent(context, NotificationPublisher.class);
                notificationIntent.setAction(DISPLAY_NOTIFICATION_ACTION);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID_TAG, notificationId);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                long futureInMillis = SystemClock.elapsedRealtime();
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });
    }
}
