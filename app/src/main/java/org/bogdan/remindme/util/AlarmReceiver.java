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

        AlarmClock.createAlarm(context, alarmMgr, alarmList, true);
    }
}
