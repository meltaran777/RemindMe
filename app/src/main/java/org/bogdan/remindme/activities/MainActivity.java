package org.bogdan.remindme.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.adapter.TabsFragmentAdapter;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.fragment.AlarmClockFragment;
import org.bogdan.remindme.fragment.BirhtdayFragment;
import org.bogdan.remindme.util.NotificationPublisher;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Collections;
import java.util.List;

/**
 * Created by Bodia on 09.06.2016.
 */
public class MainActivity extends AppCompatActivity {

    private static final int TAB_ALARM_CLOCK =0;
    private static final int TAB_BIRTHDAY =1;
    private static final int TAB_CALENDAR =2;

    public static final String APP_TAG = "RemindMeDebug" ;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    private static boolean VkLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(R.style.AppDefault);
        setContentView(R.layout.main_layout);

        fab = (FloatingActionButton) findViewById(R.id.btn_add_alarm);

        JodaTimeAndroid.init(this);

        vkLogin();

        initToolbar();
        initTabs();

        showHappyBirthdayDialog();
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
                Intent settingsIntent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
        });
        toolbar.inflateMenu(R.menu.menu);
    }

    @Override
    protected void onDestroy() {
        DBHelper.closeDB();
        super.onDestroy();
    }

    private void initTabs() {
        viewPager =(ViewPager) findViewById(R.id.ViewPager);
        tabLayout =(TabLayout) findViewById(R.id.TabLayout);

        TabsFragmentAdapter adapter = new TabsFragmentAdapter(this,getSupportFragmentManager());
        viewPager.setAdapter(adapter);

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
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }

    private void showNotificationTab(int tab){
        viewPager.setCurrentItem(tab);
    }

    private void initNavigationView(){

        drawerLayout=(DrawerLayout) findViewById(R.id.DrawerLayout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.view_navigation_open,R.string.view_navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        /*navigationView = (NavigationView) findViewById(R.id.navigation);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                drawerLayout.closeDrawers();

                switch (item.getItemId()){

                    case R.id.menu_item_alarm_clock:
                        showNotificationTab(TAB_ALARM_CLOCK);
                        break;

                    case R.id.menu_item_birthday:
                        showNotificationTab(TAB_BIRTHDAY);
                        break;

                    case R.id.menu_item_calendar:
                        showNotificationTab(TAB_CALENDAR);
                        break;

                    case R.id.menu_item_settings:
                        Intent settingsIntent = new Intent(getApplicationContext(),SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                }
                return true;
            }
        });
        */
    }

    private String[] vkScope = new String[]{VKScope.MESSAGES, VKScope.FRIENDS, VKScope.WALL};

    private VKRequest getVKFriendsListRequest =
            VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate,photo_100"));

    private void vkLogin() {
        //String[] fingetprints = VKUtil.getCertificateFingerprint(this,this.getPackageName());
        if (isInternetAvailable()) {
            if (!VKSdk.isLoggedIn()) VKSdk.login(this, vkScope);
            else {
                vkRequestExecute(getVKFriendsListRequest);
            }
        }else {
            DBHelper.readUserVKTable(getApplicationContext(), UserVK.getUsersList());
            Collections.sort(UserVK.getUsersList());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                setVklogin(true);
                vkRequestExecute(getVKFriendsListRequest);
            }

            @Override
            public void onError(VKError error) {
                Log.e("ERROR", "VK init error");
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
                DBHelper.updateTableUserVKValue(getApplicationContext());
                DBHelper.readUserVKTable(getApplicationContext(), UserVK.getUsersList());
                Collections.sort(UserVK.getUsersList());
                createNotification();

                BirhtdayFragment instanceFragment =
                        (BirhtdayFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.ViewPager + ":" + TAB_BIRTHDAY);

                if (instanceFragment != null) {

                        instanceFragment.getAdapter().notifyDataSetChanged();

                        if (!UserVK.getUsersList().isEmpty())
                            instanceFragment.getTvError().setVisibility(TextView.INVISIBLE);
                    }
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

    private void createNotification(){

        List<UserVK> userVKList=UserVK.getUsersList();

        if(!userVKList.isEmpty()) {

            Log.d("NotificationDebug", "Notification created");

            List<UserVK> userVKListFull = UserVK.getUserVKListFull(userVKList);
            Collections.sort(userVKListFull);
            UserVK userVK = userVKListFull.get(0);

            NotificationPublisher.scheduleNotification(getApplicationContext(), userVK);

        }else Log.d("NotificationDebug", "Create notification fail,empty list");

    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        Log.i("InternetConnection", "isInternetAvailable: "+(netInfo != null && netInfo.isConnectedOrConnecting()));
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean isVkLogin() {
        return VkLogin;
    }

    public void setVklogin(boolean vklogin) {
        VkLogin = vklogin;
    }

}
