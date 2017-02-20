package org.bogdan.remindme.util;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.bogdan.remindme.content.AlarmClock;

/**
 * Created by Bodia on 19.11.2016.
 */

public class NotificationActionService extends IntentService {
    public NotificationActionService() {
        super(NotificationActionService.class.getSimpleName());
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (NotificationPublisher.DISPLAY_NOTIFICATION_ACTION.equals(action)) {
            int id = intent.getIntExtra("id", 123);
            AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            AlarmClock.cancelAlarmDialogShowAction(getApplicationContext(), alarmMgr, id);
            NotificationManagerCompat.from(getApplicationContext()).cancel(id);
        }
    }
}
