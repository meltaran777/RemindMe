package org.bogdan.remindme.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.activities.AlarmDialogActivity;
import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

/**
 * Created by Bodia on 18.11.2016.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        JodaTimeAndroid.init(context);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        List<AlarmClock> alarmList = new ArrayList<>();
        DBHelper.readTableAlarms(context, alarmList);
        DBHelper.closeDB();

        if (AlarmClock.createAlarm(context, alarmMgr, alarmList, true))
            startAlarmDialog(context, intent);
    }

    private void startAlarmDialog(Context context, Intent intent){
        boolean show = true;
        if (intent.getAction() != null)
            if (intent.getAction().equalsIgnoreCase("com.htc.intent.action.QUICKBOOT_POWERON") || intent.getAction().equalsIgnoreCase("android.intent.action.QUICKBOOT_POWERON") || intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED"))
                show = false;
        if (show) {
            WakeLocker w = new WakeLocker();
            w.acquire(context);

            Intent alarmDialogIntent = new Intent(context, AlarmDialogActivity.class);
            alarmDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarmDialogIntent.putExtra("description", intent.getStringExtra("description"));
            alarmDialogIntent.putExtra("ringtone", intent.getStringExtra("ringtone"));
            context.startActivity(alarmDialogIntent);

            w.release();
        }
    }

    private class WakeLocker {
        private PowerManager.WakeLock wakeLock;

        public void acquire(Context ctx) {
            if (wakeLock != null) wakeLock.release();

            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, MainActivity.APP_TAG);
            wakeLock.acquire();
        }

        public void release() {
            if (wakeLock != null) wakeLock.release(); wakeLock = null;
        }
    }
}
