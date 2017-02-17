package org.bogdan.remindme.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;
import com.vk.sdk.util.VKUtil;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.content.User;
import org.bogdan.remindme.fragment.CalendarFragment;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.adapter.TabsFragmentAdapter;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.fragment.BirhtdayFragment;
import org.bogdan.remindme.util.RestServiceAPI;
import org.bogdan.remindme.util.NotificationPublisher;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Bodia on 09.06.2016.
 */
public class MainActivity extends AppCompatActivity {

    private static final String URL = "http://192.168.0.101:8080";

    private final String[] vkScope = new String[]{VKScope.MESSAGES, VKScope.FRIENDS, VKScope.WALL};

    private final VKRequest getVKFriendsListRequest =
            VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate,photo_100"));

    private static final int TAB_ALARM_CLOCK =0;
    private static final int TAB_BIRTHDAY =1;
    private static final int TAB_CALENDAR =2;

    public static final String APP_TAG = "RemindMeDebug";

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(R.style.AppDefault);
        setContentView(R.layout.main_layout);

        JodaTimeAndroid.init(MainActivity.this);

        fab = (FloatingActionButton) findViewById(R.id.btn_add_alarm);

        initToolbar();
        initTabs();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        dowloadVkFriendsList();
    }

    private void showHappyBirthdayDialog() {

        Bundle bundle = getIntent().getExtras();

        if (bundle !=null) {

            String action = bundle.getString("action","");

            if (action.equalsIgnoreCase(NotificationPublisher.DISPLAY_HAPPY_BIRTHDAY_DIALOG_ACTION)) {

            String userName = getIntent().getStringExtra("userName");
            String userAvatarURL = getIntent().getStringExtra("userAvatarURL");
            int userId = getIntent().getIntExtra("userId", 0);

            Intent happyBirthdayDialogIntent = new Intent(getApplicationContext(), HappyBirthdayDialogActivity.class);

            happyBirthdayDialogIntent.putExtra("userId", userId);
            happyBirthdayDialogIntent.putExtra("userName", userName);
            happyBirthdayDialogIntent.putExtra("userAvatarURL", userAvatarURL);

            startActivity(happyBirthdayDialogIntent);
            }
        }
    }

    private void initToolbar() {
        toolbar=(Toolbar) findViewById(R.id.Toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.settings :
                    Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(settingsIntent);
                        return true;

                    case R.id.vkLogout:
                        VKSdk.logout();
                        return true;
                }
                return false;
            }
        });
        toolbar.inflateMenu(R.menu.menu);
    }

    @Override
    protected void onDestroy() {
        DBHelper.closeDB();
        super.onDestroy();
    }

    TabsFragmentAdapter tabsFragmentAdapter;
    private void initTabs() {
        viewPager = (ViewPager) findViewById(R.id.ViewPager);
        tabLayout = (TabLayout) findViewById(R.id.TabLayout);

        tabsFragmentAdapter = new TabsFragmentAdapter(MainActivity.this, getSupportFragmentManager());
        viewPager.setAdapter(tabsFragmentAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(APP_TAG, "onPageSelected: " + String.valueOf(position));

                if (position != TAB_ALARM_CLOCK)
                    fab.setVisibility(FloatingActionButton.INVISIBLE);
                else {
                    fab.setVisibility(FloatingActionButton.VISIBLE);
                }

                if (position == TAB_ALARM_CLOCK)
                    if (AlarmClock.getAlarmList().isEmpty()){
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.txtNoAlarm), Toast.LENGTH_LONG).show();
                    }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }

    public void dowloadVkFriendsList() {

        String[] fingetprints = VKUtil.getCertificateFingerprint(MainActivity.this, MainActivity.this.getPackageName());
        for (int i=0; i < fingetprints.length; i++) Log.i(APP_TAG, "dowloadVkFriendsList: " + fingetprints[i]);

        if (isInternetAvailable()) {
            if (VKSdk.isLoggedIn()) {
                vkRequestExecute(getVKFriendsListRequest);
            }
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBHelper.readUserVKTable(getApplicationContext(), UserVK.getUsersList());
                    Collections.sort(UserVK.getUsersList());
                }
            }).start();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {

                ((BirhtdayFragment) getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.ViewPager + ":" + TAB_BIRTHDAY))
                        .showProgressBar();

                vkRequestExecute(getVKFriendsListRequest);
            }

            @Override
            public void onError(VKError error) {
                Log.e("ERROR", "VK init error");

                BirhtdayFragment fragment = (BirhtdayFragment) getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.ViewPager + ":" + TAB_BIRTHDAY);
                fragment.showVkLogin();
                fragment.hideProgressBar();
            }
        }))
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void vkRequestExecute(VKRequest currentRequest){
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkApp", "onComplete " + response);

                VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                UserVK.getUsersList().clear();
                final String[] formats = new String[]{"dd.MM.yyyy", "dd.MM"};

                for (VKApiUserFull userFull : usersArray) {
                    DateTime birthDate = null;
                    String format = null;
                    String avatarURL = null;
                    if (!TextUtils.isEmpty(userFull.photo_100)) avatarURL=userFull.photo_100;

                    if (!TextUtils.isEmpty(userFull.bdate)) {
                        for (int i = 0; i < formats.length; i++) {
                            format = formats[i];
                            try {
                                birthDate = DateTimeFormat.forPattern(format).parseDateTime(userFull.bdate);
                                UserVK.getUsersList().add(new UserVK(userFull.id, userFull.toString(), birthDate, format, avatarURL, false));
                            } catch (Exception ignored) {
                                Log.d("VkApp", "Exception ignore " + response);
                            }
                            if (birthDate != null) {
                                break;
                            }
                        }
                    }
                }
                new UpdateDBTask().execute();

                sendUserListToServer();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkApp", "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

    private void sendUserListToServer() {

        UserVK testUser = UserVK.getUsersList().get(0);

        Gson gson = new GsonBuilder()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestServiceAPI restServiceAPI = retrofit.create(RestServiceAPI.class);

        restServiceAPI.saveUser(testUser.toUser()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(APP_TAG, "onResponse:User-User: response code " + String.valueOf(response.code()));

                if (response.isSuccessful())
                    Log.d(APP_TAG, "onResponse:User-User: " + response.body().getId());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(APP_TAG, "onFailure:User-User: request failed");
                t.printStackTrace();
            }
        });
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        Log.i("InternetConnection", "isInternetAvailable: "+(netInfo != null && netInfo.isConnectedOrConnecting()));
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void vkLogin() {
        VKSdk.login(MainActivity.this, vkScope);
    }

    private class UpdateDBTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            DBHelper.updateTableUserVKValue(getApplicationContext());

            DBHelper.readUserVKTable(getApplicationContext(), UserVK.getUsersList());

            Collections.sort(UserVK.getUsersList());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            BirhtdayFragment birhtdayFragment = (BirhtdayFragment) getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.ViewPager + ":" + TAB_BIRTHDAY);

            CalendarFragment calendarFragment = (CalendarFragment) getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.ViewPager + ":" + TAB_CALENDAR);

            if (birhtdayFragment != null) {
                birhtdayFragment.updateBirthdayFragmentUI();
            }

            if (calendarFragment != null)
                calendarFragment.refreshDecorator();
        }
    }
}
