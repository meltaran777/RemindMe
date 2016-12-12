package org.bogdan.remindme.util;

import android.app.AlarmManager;
import android.app.IntentService;
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
import android.media.RingtoneManager;
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
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Notification.DEFAULT_VIBRATE;

/**
 * Created by Bodia on 12.11.2016.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static final String DISPLAY_NOTIFICATION_ACTION = "org.bogdan.remindme.DISPLAY_NOTIFICATION";
    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";

    static String ringtone;
    static boolean vibration;
    static boolean show;

    @Override
    public void onReceive(final Context context, Intent intent) {
        getNotificationPreference(context);
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
        if (!intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED") && show) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(notificationId, notification);
        }

    }
    static Bitmap avatar;
    public static void scheduleNotification(final Context context, int notificationId, UserVK userVK) {
        getNotificationPreference(context);
            NotificationCompat.Builder builder;
            long delay = dayToMillis(userVK.getDayToNextBirht());
            boolean original = userVK.isNotify();
            String largeIconUrl = userVK.getAvatarURL();
            Picasso.with(context).load(largeIconUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    setAvatar(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    setAvatar(context);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    setAvatar(context);
                }
            });

            builder = new NotificationCompat.Builder(context)
                    .setContentText(userVK.getName())
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_birthday)
                    .setLargeIcon(avatar)
                    .setSound(Uri.parse(ringtone));
            if (vibration) builder.setDefaults(DEFAULT_VIBRATE);
            if (original) builder.setContentTitle(context.getString(R.string.str_birthday));
            else builder.setContentTitle(context.getString(R.string.str_birthday_soon));

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
        getNotificationPreference(context);
        if (show) {
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
                            //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setSound(Uri.parse(ringtone))
                            .setAutoCancel(true)
                            .setContentIntent(actionPendingIntent);
            if (vibration) notificationBuilder.setDefaults(DEFAULT_VIBRATE);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationManagerCompat.from(context).cancelAll();
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    public static long dayToMillis(long day) {
        LocalDateTime now = new LocalDateTime();
        int hours, minute, second, millisOfSecond;
        hours = now.getHourOfDay();
        minute = now.getMinuteOfHour();
        second = now.getSecondOfMinute();
        //millisOfSecond = now.getMillisOfSecond();

        //long millis = day * 86400000 - (3600000 * hours + 60000 * minute + 1000 * second + millisOfSecond);
        long millis = day * 86400000 - (3600000 * hours + 60000 * minute + 1000 * second);
        return millis;
    }

    private static void setAvatar(Bitmap bitmap) {
        avatar = bitmap;
    }

    private static void setAvatar(Context context) {
        avatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_birthday);;
    }
    private static void getNotificationPreference(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        vibration = prefs.getBoolean("notifications_new_message_vibrate", true);
        show = prefs.getBoolean("notifications_new_message", true);
        ringtone = prefs.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
    }
}
