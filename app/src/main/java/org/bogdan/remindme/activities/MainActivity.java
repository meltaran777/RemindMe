package org.bogdan.remindme.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

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
import org.bogdan.remindme.util.NotificationPublisher;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Bodia on 09.06.2016.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int TAB_ONE=0;
    private static final int TAB_TWO=1;
    private static final int TAB_THREE=2;
    private static final int TAB_FOUR=3;
    public static final String APP_TAG = "RemindMe" ;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navigationView;
    private ProgressBar progressBar;

    DBHelper dbHelper = null;
    SQLiteDatabase database = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(R.style.AppDefault);
        setContentView(R.layout.main_layout);
        JodaTimeAndroid.init(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        initDB();
        vkLogin();
        initToolbar();
        initNavigationView();
        if(readDB()) {
            initTabs();
            closeDB();
        }
    }




    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                initDB();
                if(!readDB()) {
                    vkRequestExecute(getVKFriendsList);
                }
            }

            @Override
            public void onError(VKError error) {

            }
        }))
            super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean readDB() {
        UserVK.getUsersList().clear();
        Cursor cursor = database.query(DBHelper.TABLE_USERS ,null ,null ,null ,null ,null ,null);
        if(cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
            int avatarURLIndex = cursor.getColumnIndex(DBHelper.KEY_AVATAR_URL);
            int bdateIndex = cursor.getColumnIndex(DBHelper.KEY_BDATE);
            int dateFormatIndex = cursor.getColumnIndex(DBHelper.KEY_DATE_FORMAT);

            do{
                DateTime birthDate = DateTimeFormat.forPattern(cursor.getString(dateFormatIndex)).parseDateTime(cursor.getString(bdateIndex));
                UserVK.getUsersList().add(new UserVK(cursor.getString(nameIndex), birthDate, cursor.getString(dateFormatIndex), cursor.getString(avatarURLIndex)));

            }while (cursor.moveToNext());
        }else Log.d("DB","0 rows");
        cursor.close();
        if(UserVK.getUsersList().isEmpty()) {
            return false;
        }else return true;
    }

    private void initDB(){
        if(dbHelper == null) dbHelper = new DBHelper(getApplicationContext());
        if(database == null) database = dbHelper.getWritableDatabase();
    }

    private void closeDB(){
        if(dbHelper != null) dbHelper.close();
        if(database != null) database.close();
    }

    private void insertDB() {
        ContentValues contentValues = new ContentValues();

        for(UserVK userVK : UserVK.getUsersList()) {

            DateTime birthDate = userVK.getBirthDate();
            DateTimeFormatter fmt = DateTimeFormat.forPattern(userVK.getDateFormat());
            String bdate = fmt.print(birthDate);

            contentValues.put(dbHelper.KEY_NAME, userVK.getName());
            contentValues.put(dbHelper.KEY_AVATAR_URL, userVK.getAvatarURL());
            contentValues.put(dbHelper.KEY_BDATE, bdate);
            contentValues.put(dbHelper.KEY_DATE_FORMAT, userVK.getDateFormat());

            database.insert(DBHelper.TABLE_USERS, null, contentValues);
        }
    }

    private void initTabs() {
        Log.d("VkAppDP", "initTabs ");
        viewPager =(ViewPager) findViewById(R.id.ViewPager);
        tabLayout =(TabLayout) findViewById(R.id.TabLayout);

        TabsFragmentAdapter adapter = new TabsFragmentAdapter(this,getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
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


    private void initNavigationView(){
        drawerLayout=(DrawerLayout) findViewById(R.id.DrawerLayout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.view_navigation_open,R.string.view_navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
    }


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

    private void showNotificationTab(){
        viewPager.setCurrentItem(TAB_TWO);
    }

    private String[] vkScope = new String[]{VKScope.MESSAGES,VKScope.FRIENDS,VKScope.WALL};

    private void vkLogin() {
        //String[] fingetprints = VKUtil.getCertificateFingerprint(this,this.getPackageName());     get VK fingerprint
        if(!VKSdk.isLoggedIn()) VKSdk.login(this,vkScope);
    }

    private VKRequest getVKFriendsList = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate,photo_100"));

    private void vkRequestExecute(VKRequest currentRequest){
        Log.d("VkAppDP", "vkRequestExecute ");

        progressBar.setVisibility(ProgressBar.VISIBLE);

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
                                UserVK.getUsersList().add(new UserVK(userFull.toString(), birthDate, format,avatarURL));

                            } catch (Exception ignored) {
                                Log.d("VkApp", "Exception ignore " + response);
                            }
                            if (birthDate != null) {
                                break;
                            }
                        }
                    }
                }
                Collections.sort(UserVK.getUsersList());
                insertDB();
                closeDB();
                createNotificationFromUserList();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                initTabs();
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


    private void createNotificationFromUserList(){

        List<UserVK> userVKList=UserVK.getUsersList();

        if(!userVKList.isEmpty()) {

            Log.d("VkAppDP", "createNotificationFromUserList ");

            for (int position = 0; position < userVKList.size(); position++) {

                long day = UserVK.getDayToNextBirht(userVKList.get(position).getBirthDate());
                NotificationPublisher.scheduleNotification(getApplicationContext(), dayToMillis(day), position, userVKList.get(position).getAvatarURL());
            }
        }else Log.d("VkAppDP", "createNotificationFromUserList -- Empty ");
    }

    private long dayToMillis(long day){

        LocalDateTime now = new LocalDateTime();

        int hours,minute,second;
        hours = now.getHourOfDay();
        minute = now.getMinuteOfHour();
        second = now.getSecondOfMinute();

        long millis = day*86400000-(3600000*hours+60000*minute+1000*second);
        return millis;
    }
}
