package org.bogdan.remindme;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.os.Bundle;
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
import android.widget.Toast;

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

import org.bogdan.remindme.adapter.TabsPagerFragmentAdapter;
import org.bogdan.remindme.task.DownloadImageTask;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Collections;

/**
 * Created by Bodia on 09.06.2016.
 */
public class MainActivity extends AppCompatActivity {

    private static final int TAB_ONE=0;
    private static final int TAB_TWO=1;
    private static final int TAB_THREE=2;
    private static final int TAB_FOUR=3;


    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navigationView;





    VKRequest getVKFriendsList = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate,photo_100"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);



        vkLogin();
        vkRequestExecute(getVKFriendsList);
        initToolbar();
        initNavigationView();
        initTabs();
    }

    private void initTabs() {
        viewPager =(ViewPager) findViewById(R.id.ViewPager);
        tabLayout =(TabLayout) findViewById(R.id.TabLayout);

        TabsPagerFragmentAdapter adapter = new TabsPagerFragmentAdapter(this,getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void initToolbar() {
        toolbar=(Toolbar) findViewById(R.id.Toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        toolbar.inflateMenu(R.menu.menu);
    }


    private void initNavigationView(){
        drawerLayout=(DrawerLayout) findViewById(R.id.DrawerLayout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.view_navigation_open,R.string.view_navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Log.i("navigationView"," onItemSelected");
                drawerLayout.closeDrawers();
                switch (item.getItemId()){
                    case R.id.menu_item_notification:
                        Log.i("navigationView"," case");
                        showNotificationTab();
                        break;
                }
                return true;
            }
        });
    }

    private void showNotificationTab(){
        viewPager.setCurrentItem(TAB_TWO);
    }

    private String[] vkScope = new String[]{VKScope.MESSAGES,VKScope.FRIENDS,VKScope.WALL};
    private void vkLogin() {
        //String[] fingetprints = VKUtil.getCertificateFingerprint(this,this.getPackageName());     get VK fingerprint
        if(!VKSdk.isLoggedIn()) VKSdk.login(this,vkScope);
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
                int notifId=0;

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
                                UserVK.getUsersList().add(new UserVK(userFull.toString(), birthDate, format,avatarURL));
                                NotificationPublisher.scheduleNotification(getApplicationContext(),dayToMillis(UserVK.getDayToNextBirht(birthDate)),notifId,avatarURL);

                            } catch (Exception ignored) {
                            }
                            if (birthDate != null) {
                                break;
                            }
                        }

                    }

                    notifId++;
                }
                Collections.sort(UserVK.getUsersList());
                NotificationPublisher.scheduleNotification(getApplicationContext(),100000,0,UserVK.getUsersList().get(0).getAvatarURL()); //test Notification

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


    private long dayToMillis(int day){
        DateTime now = DateTime.now();
        int hours,minute,second;
        hours = now.getHourOfDay();
        minute = now.getMinuteOfHour();
        second = now.getSecondOfMinute();
        long millis = day*86400000-(hours*3600000+minute*60000+second*1000);
        return millis;
    }

}