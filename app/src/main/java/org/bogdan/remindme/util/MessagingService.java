package org.bogdan.remindme.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 19.02.2017.
 */

public class MessagingService extends FirebaseMessagingService {

    private static final String DEBUG_TAG = "FirebaseDebug";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(DEBUG_TAG, "onMessageReceived: " + remoteMessage.getFrom().toString());

        //if (remoteMessage.getFrom().equals("/topics/" + InstanceIdService.INFO_TOPIC_NAME)){
        //Log.d(DEBUG_TAG, "onMessageReceived: User");

        List<UserVK> users = new ArrayList<>();
        DBHelper.readUserVKTable(this, users);

        UserVK user = users.get(0);

        try {
            NotificationPublisher.showUserVkBirthdayNotification(this, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }
}
