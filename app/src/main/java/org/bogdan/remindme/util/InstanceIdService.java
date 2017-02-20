package org.bogdan.remindme.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by Bodia on 19.02.2017.
 */

public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TOKEN_PREFERENCE_KEY = "fmc_token";
    public static final String INFO_TOPIC_NAME =  "info";


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString(TOKEN_PREFERENCE_KEY, FirebaseInstanceId.getInstance().getToken())
                .apply();

        FirebaseMessaging.getInstance().subscribeToTopic(INFO_TOPIC_NAME);
    }


}
