package org.bogdan.remindme.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Bodia on 18.11.2016.
 */

public class AlarmRecevier extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context,RingtoneServices.class);

            if(intent.getExtras().getInt("intentState") == 1)context.startService(serviceIntent);
        else if(intent.getExtras().getInt("intentState") == 0) context.stopService(serviceIntent);
    }
}
