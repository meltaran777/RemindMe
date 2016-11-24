package org.bogdan.remindme.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

/**
 * Created by Bodia on 18.11.2016.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        Intent serviceIntent = new Intent(context,RingtoneServices.class);

            if(intent.getExtras().getInt("intentState") == 1)context.startService(serviceIntent);
        else if(intent.getExtras().getInt("intentState") == 0) context.stopService(serviceIntent); */

        Log.d("AlarmDebud","AlarmRecivier");

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
