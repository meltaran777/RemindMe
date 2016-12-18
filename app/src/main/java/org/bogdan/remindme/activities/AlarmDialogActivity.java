package org.bogdan.remindme.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.AlteredCharSequence;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.util.AlarmReceiver;
import org.bogdan.remindme.util.NotificationPublisher;

import java.util.Random;

/**
 * Created by Bodia on 23.11.2016.
 */
public class AlarmDialogActivity extends AppCompatActivity implements View.OnClickListener {
    private static final long DELAY = 10*60*1000;
    public static final String ALARM_DIALOG_ACTION = "org.bogdan.remindme.ALARMDIALOG_DELAY";

    TextView timeView;
    TextView discView;
    Button btnClose;
    Button btnSetAside;

    MediaPlayer mp;
    String ringtone;
    String desc;
    int hour;
    int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_dialog_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
       // getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
       // getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        timeView = (TextView) findViewById(R.id.textViewTimeDialog);
        discView = (TextView) findViewById(R.id.textViewDescriptionDialog);
        btnClose = (Button) findViewById(R.id.btn_close);
        btnSetAside = (Button) findViewById(R.id.btn_set_aside);

        btnClose.setOnClickListener(this);
        btnSetAside.setOnClickListener(this);

        ringtone = getIntent().getStringExtra("ringtone");
        desc = getIntent().getStringExtra("description");
        hour = getIntent().getIntExtra("hour", 0);
        minute = getIntent().getIntExtra("minute", 0);
        String strHour = String.valueOf(hour);
        String strMinute = String.valueOf(minute);
        if (minute<10) strMinute = "0"+minute;
        if (hour<10)   strHour = "0"+hour;

        discView.setText(desc);
        timeView.setText(strHour+":"+strMinute);

        Uri uri = Uri.parse(ringtone);
        mp = MediaPlayer.create(this,uri);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
        mp.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mp.stop();
            }
        }, 10*60*1000);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                mp.stop();
                finish();
                break;
            case R.id.btn_set_aside:
                mp.stop();

                AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
                alarmIntent.setAction(ALARM_DIALOG_ACTION);
                alarmIntent.putExtra("description",desc);
                alarmIntent.putExtra("ringtone",ringtone);
                alarmIntent.putExtra("hour",hour);
                alarmIntent.putExtra("minute",minute);

                int id = new Random().nextInt();

                AlarmClock.createAlarm(getApplicationContext(), alarmMgr, alarmIntent, DELAY, id);
                NotificationPublisher.displayAlarmNotification(getApplicationContext(), id);
                Toast.makeText(getApplicationContext(), R.string.toast_text3, Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
    }
}
