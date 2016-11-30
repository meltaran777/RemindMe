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

/**
 * Created by Bodia on 18.11.2016.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        //Start read data from db
        DBHelper.getDbHelper(context);
        Cursor cursor = DBHelper.getDatabase(context).query(DBHelper.TABLE_ALARMS ,null ,null ,null ,null ,null ,null);

        if(cursor.moveToFirst()){
            int activeInd = cursor.getColumnIndex(DBHelper.KEY_ACTIVE_ALARM);
            int daysArrayInd[] = {
                    cursor.getColumnIndex(DBHelper.KEY_MONDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_TUESDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_WEDNESDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_THURSDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_FRIDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_SATURDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_SUNDAY_ALARM)
            };
            int deskInd = cursor.getColumnIndex(DBHelper.KEY_DESC_ALARM);
            int textTimeInd = cursor.getColumnIndex(DBHelper.KEY_TIME_TEXT_ALARM);
            int idInd = cursor.getColumnIndex(DBHelper.KEY_ID_ALARM);
            int hourInd = cursor.getColumnIndex(DBHelper.KEY_HOUR_ALARM);
            int minuteInd = cursor.getColumnIndex(DBHelper.KEY_MINUTE_ALARM);

            do{
                int id = cursor.getInt(idInd);
                Log.d("DebugDB","DB record id = "+id);

                boolean active;
                if (cursor.getInt(activeInd) == 0) {active=false;}else active=true;

                boolean alarmDays[] = new boolean[7];
                for(int i=0; i < alarmDays.length; i++)
                    if (cursor.getInt(daysArrayInd[i]) == 0) {alarmDays[i]=false;}else alarmDays[i]=true;

                String desc = cursor.getString(deskInd);

                int hour = cursor.getInt(hourInd);
                int minute = cursor.getInt(minuteInd);

                AlarmClock alarmClock = new AlarmClock(alarmDays,hour,minute,desc,active);
                alarmList.add(alarmClock);
            }while (cursor.moveToNext());
        }else Log.d("DebugDB","0 rows");

        cursor.close();
        DBHelper.closeDB();

        //for (AlarmClock alarmClock : alarmList) Log.d("DebugDB",alarmClock.toString());

        //End read data from db
        */
        List<AlarmClock> alarmList = new ArrayList<>();
        DBHelper.readTableAlarms(context, alarmList);
        DBHelper.closeDB();

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //AlarmClock.createAlarm(context, alarmMgr, alarmList);  //Create next alarm


        WakeLocker w = new WakeLocker();
        w.acquire(context);

        Intent alarmDialogIntent = new Intent(context, AlarmDialogActivity.class);
        alarmDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmDialogIntent.putExtra("description",intent.getStringExtra("description"));
        context.startActivity(alarmDialogIntent);

        w.release();
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
