package org.bogdan.remindme.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;

import org.bogdan.remindme.util.AlarmReceiver;

/**
 * Created by Bodia on 23.11.2016.
 */
public class AlarmDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        Log.d("Debug Alarm","Start alarmDialogActivity");

        displayAlert();
    }

    private void displayAlert() {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alarm");
        builder.setMessage(getIntent().getStringExtra("description"))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ringtone.stop();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
