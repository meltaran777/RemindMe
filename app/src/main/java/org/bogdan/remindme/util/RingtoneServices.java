package org.bogdan.remindme.util;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Bodia on 19.11.2016.
 */

public class RingtoneServices extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            try {
                Uri alarmURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarmURI);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return super.onStartCommand(intent, flags, startId);
    }



}
