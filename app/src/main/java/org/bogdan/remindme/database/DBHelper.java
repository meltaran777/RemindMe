package org.bogdan.remindme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.content.UserVK;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 15.11.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "remindDB";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ALARMS = "alarms";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_BDATE = "bdate";
    public static final String KEY_DATE_FORMAT = "date_format";
    public static final String KEY_AVATAR_URL = "avatar_url";
    public static final String KEY_NOTIFY = "notify";

    public static final String KEY_ID_ALARM = "_id";
    public static final String KEY_ID_ALARM_UPDATE = "id_update";
    public static final String KEY_HOUR_ALARM = "hour";
    public static final String KEY_MINUTE_ALARM = "minute";
    public static final String KEY_DESC_ALARM = "description";
    public static final String KEY_TIME_TEXT_ALARM = "time_text";
    public static final String KEY_ACTIVE_ALARM = "active";
    public static final String KEY_RINGTONE_URI_ALARM = "ringtone_uri";
    public static final String KEY_MONDAY_ALARM = "monday";
    public static final String KEY_TUESDAY_ALARM = "tuesday";
    public static final String KEY_WEDNESDAY_ALARM = "wednesday";
    public static final String KEY_THURSDAY_ALARM = "thursday";
    public static final String KEY_FRIDAY_ALARM = "friday";
    public static final String KEY_SATURDAY_ALARM = "saturday";
    public static final String KEY_SUNDAY_ALARM = "sunday";

    private static DBHelper  dbHelper;
    private static SQLiteDatabase  database;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table "+ TABLE_USERS
                +"(" + KEY_ID +" integer primary key,"
                + KEY_NAME + " text,"
                + KEY_DATE_FORMAT + " text,"
                + KEY_AVATAR_URL + " text,"
                + KEY_NOTIFY + " numeric,"
                + KEY_BDATE +" text"+")");

        db.execSQL("create table "+ TABLE_ALARMS
                +"(" + KEY_ID_ALARM +" integer primary key,"
                + KEY_TIME_TEXT_ALARM + " text,"
                + KEY_MONDAY_ALARM + " numeric,"
                + KEY_TUESDAY_ALARM + " numeric,"
                + KEY_WEDNESDAY_ALARM + " numeric,"
                + KEY_THURSDAY_ALARM + " numeric,"
                + KEY_FRIDAY_ALARM + " numeric,"
                + KEY_SATURDAY_ALARM + " numeric,"
                + KEY_SUNDAY_ALARM + " numeric,"
                + KEY_RINGTONE_URI_ALARM + " numeric,"
                + KEY_DESC_ALARM + " text,"
                + KEY_HOUR_ALARM + " integer,"
                + KEY_MINUTE_ALARM + " integer,"
                + KEY_ID_ALARM_UPDATE + " integer,"
                + KEY_ACTIVE_ALARM +" numeric"+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exist "+ TABLE_USERS);
        db.execSQL("drop table if exist "+ TABLE_ALARMS);
        onCreate(db);
    }

    public static DBHelper getDbHelper(Context context) {
        if (dbHelper == null) {dbHelper = new DBHelper(context);}
        return dbHelper;
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (dbHelper != null) {
            if (database == null){
            database = dbHelper.getWritableDatabase();
            return database;
            }else return database;
        }else {
            dbHelper = getDbHelper(context);
            return database = dbHelper.getWritableDatabase();
        }
    }

    public static void closeDB(){
        if (dbHelper != null) {dbHelper.close();dbHelper = null;}
        if (database != null) {database.close();database = null;}
    }

    public static List<AlarmClock> readTableAlarms(Context context,List<AlarmClock> alarmList){

        Cursor cursor = DBHelper.getDatabase(context).query(DBHelper.TABLE_ALARMS ,null ,null ,null ,null ,null ,null);

        if(cursor.moveToFirst()){
            int activeInd = cursor.getColumnIndex(DBHelper.KEY_ACTIVE_ALARM);
            int daysArrayInd[] = {
                    cursor.getColumnIndex(DBHelper.KEY_MONDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_TUESDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_WEDNESDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_THURSDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_FRIDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_SATURDAY_ALARM),
                    cursor.getColumnIndex(DBHelper.KEY_SUNDAY_ALARM)
            };
            int deskInd = cursor.getColumnIndex(DBHelper.KEY_DESC_ALARM);
            int updateInd = cursor.getColumnIndex(DBHelper.KEY_ID_ALARM_UPDATE);
            int idInd = cursor.getColumnIndex(DBHelper.KEY_ID_ALARM);
            int hourInd = cursor.getColumnIndex(DBHelper.KEY_HOUR_ALARM);
            int minuteInd = cursor.getColumnIndex(DBHelper.KEY_MINUTE_ALARM);
            int ringtoneUriInd = cursor.getColumnIndex(DBHelper.KEY_RINGTONE_URI_ALARM);

            do{
                int id = cursor.getInt(updateInd);
                Log.d("DebugDB","DB record id_update = "+id);

                boolean active;
                if (cursor.getInt(activeInd) == 0) {active=false;}else active=true;

                boolean alarmDays[] = new boolean[7];
                for(int i=0; i < alarmDays.length; i++)
                    if (cursor.getInt(daysArrayInd[i]) == 0) {alarmDays[i]=false;}else alarmDays[i]=true;

                String desc = cursor.getString(deskInd);

                int hour = cursor.getInt(hourInd);
                int minute = cursor.getInt(minuteInd);

                String ringtoneURI = cursor.getString(ringtoneUriInd);

                AlarmClock alarmClock = new AlarmClock(alarmDays, hour, minute, desc, active, ringtoneURI, alarmList);
                alarmList.add(alarmClock);
            }while (cursor.moveToNext());
        }else Log.d("DebugDB","0 rows");
        cursor.close();
        return alarmList;

    }

    public static void putAlarmValue(Context context, ContentValues contentValues, String descString, String ringtoneURI, boolean active, boolean[] daysArray, int hour, int minute) {

        contentValues.put(DBHelper.getDbHelper(context).KEY_DESC_ALARM, descString);

        contentValues.put(DBHelper.getDbHelper(context).KEY_RINGTONE_URI_ALARM, ringtoneURI);

        contentValues.put(DBHelper.getDbHelper(context).KEY_TIME_TEXT_ALARM, hour+":"+minute);

        contentValues.put(DBHelper.getDbHelper(context).KEY_HOUR_ALARM, hour);
        contentValues.put(DBHelper.getDbHelper(context).KEY_MINUTE_ALARM, minute);

        if (active) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_ACTIVE_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_ACTIVE_ALARM, 0);

        if (daysArray[0]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_MONDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_MONDAY_ALARM, 0);

        if (daysArray[1]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_TUESDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_TUESDAY_ALARM, 0);

        if (daysArray[2]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_WEDNESDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_WEDNESDAY_ALARM, 0);

        if (daysArray[3]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_THURSDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_THURSDAY_ALARM, 0);

        if (daysArray[4]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_FRIDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_FRIDAY_ALARM, 0);

        if (daysArray[5]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_SATURDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_SATURDAY_ALARM, 0);

        if (daysArray[6]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_SUNDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_SUNDAY_ALARM, 0);
    }




    public static void putAlarmValue(Context context, ContentValues contentValues, AlarmClock alarmClock) {
        String descString = alarmClock.getDescription();
        String ringtoneURI = alarmClock.getRingtoneURI();
        int hour = alarmClock.getHour();
        int minute = alarmClock.getMinute();
        int id = alarmClock.getAlarmId()+1;
        boolean daysArray[] = alarmClock.getAlarmDays();
        boolean active = alarmClock.isActive();

        contentValues.put(DBHelper.getDbHelper(context).KEY_ID_ALARM_UPDATE, id);

        contentValues.put(DBHelper.getDbHelper(context).KEY_DESC_ALARM, descString);

        contentValues.put(DBHelper.getDbHelper(context).KEY_RINGTONE_URI_ALARM, ringtoneURI);

        contentValues.put(DBHelper.getDbHelper(context).KEY_TIME_TEXT_ALARM, hour+":"+minute);

        contentValues.put(DBHelper.getDbHelper(context).KEY_HOUR_ALARM, hour);
        contentValues.put(DBHelper.getDbHelper(context).KEY_MINUTE_ALARM, minute);

        if (active) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_ACTIVE_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_ACTIVE_ALARM, 0);

        if (daysArray[0]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_MONDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_MONDAY_ALARM, 0);

        if (daysArray[1]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_TUESDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_TUESDAY_ALARM, 0);

        if (daysArray[2]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_WEDNESDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_WEDNESDAY_ALARM, 0);

        if (daysArray[3]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_THURSDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_THURSDAY_ALARM, 0);

        if (daysArray[4]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_FRIDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_FRIDAY_ALARM, 0);

        if (daysArray[5]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_SATURDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_SATURDAY_ALARM, 0);

        if (daysArray[6]) {
            contentValues.put(DBHelper.getDbHelper(context).KEY_SUNDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(context).KEY_SUNDAY_ALARM, 0);
    }

    public static boolean readUserVKTable(Context context, List<UserVK> userVKList) {
        userVKList.clear();
        Cursor cursor = DBHelper.getDatabase(context).query(DBHelper.TABLE_USERS ,null ,null ,null ,null ,null ,null);
        if(cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
            int avatarURLIndex = cursor.getColumnIndex(DBHelper.KEY_AVATAR_URL);
            int bdateIndex = cursor.getColumnIndex(DBHelper.KEY_BDATE);
            int dateFormatIndex = cursor.getColumnIndex(DBHelper.KEY_DATE_FORMAT);
            int notifyIndex = cursor.getColumnIndex(DBHelper.KEY_NOTIFY);

            do{
                boolean notify;
                if (cursor.getInt(notifyIndex) == 1) notify = true; else notify = false;
                DateTime birthDate = DateTimeFormat.forPattern(cursor.getString(dateFormatIndex)).parseDateTime(cursor.getString(bdateIndex));
                userVKList.add(new UserVK(cursor.getInt(idIndex), cursor.getString(nameIndex), birthDate, cursor.getString(dateFormatIndex), cursor.getString(avatarURLIndex),notify));

            }while (cursor.moveToNext());
        }else Log.d("DB","0 rows");
        cursor.close();
        if(userVKList.isEmpty()) {
            return false;
        }else return true;
    }
    public static void putUserValue(Context context, UserVK userVK, ContentValues contentValues){
        DateTime birthDate = userVK.getBirthdayDate();
        DateTimeFormatter fmt = DateTimeFormat.forPattern(userVK.getDateFormat());
        String bdate = fmt.print(birthDate);

        int notify;
        if (userVK.isNotify()) notify = 1; else notify = 0;

        contentValues.put(DBHelper.getDbHelper(context).KEY_ID, userVK.getId());
        contentValues.put(DBHelper.getDbHelper(context).KEY_NAME, userVK.getName());
        contentValues.put(DBHelper.getDbHelper(context).KEY_AVATAR_URL, userVK.getAvatarURL());
        contentValues.put(DBHelper.getDbHelper(context).KEY_BDATE, bdate);
        contentValues.put(DBHelper.getDbHelper(context).KEY_DATE_FORMAT, userVK.getDateFormat());
        contentValues.put(DBHelper.getDbHelper(context).KEY_NOTIFY, notify);
    }
    public static void updateTableUserVKValue(Context context) {
        List<UserVK> oldUserVKList = new ArrayList<>();
        readUserVKTable(context, oldUserVKList);

        ContentValues contentValues = new ContentValues();

        for (UserVK userVK : UserVK.getUsersList()) {
            boolean userExist = false;
            for (UserVK oldUserVK : oldUserVKList){
                if (oldUserVK.getName().equals(userVK.getName())) {
                    userExist = true;
                }
            }
            if (!userExist) {
                Log.d("VKDebug", "updateTableUserVKValue:Add "+userVK.getName());
                DateTime birthDate = userVK.getBirthdayDate();
                DateTimeFormatter fmt = DateTimeFormat.forPattern(userVK.getDateFormat());
                String bdate = fmt.print(birthDate);

                int notify;
                if (userVK.isNotify()) notify = 1;
                else notify = 0;

                contentValues.put(DBHelper.getDbHelper(context).KEY_ID, userVK.getId());
                contentValues.put(DBHelper.getDbHelper(context).KEY_NAME, userVK.getName());
                contentValues.put(DBHelper.getDbHelper(context).KEY_AVATAR_URL, userVK.getAvatarURL());
                contentValues.put(DBHelper.getDbHelper(context).KEY_BDATE, bdate);
                contentValues.put(DBHelper.getDbHelper(context).KEY_DATE_FORMAT, userVK.getDateFormat());
                contentValues.put(DBHelper.getDbHelper(context).KEY_NOTIFY, notify);

                DBHelper.getDatabase(context).insert(DBHelper.TABLE_USERS, null, contentValues);
            }
        }
        for (UserVK oldUserVK : oldUserVKList) {
            boolean userWasDeleted = true;
            for (UserVK userVK : UserVK.getUsersList()) {
                if (oldUserVK.getName().equals(userVK.getName())) userWasDeleted = false;
            }
                if (userWasDeleted){
                    Log.d("VKDebug", "updateTableUserVKValue:Delete "+oldUserVK.getName());
                    DBHelper.getDatabase(context).delete(DBHelper.TABLE_USERS, DBHelper.KEY_NAME + "=?", new String[]{oldUserVK.getName()});
                }
        }
    }
    public static void insertTableUserVKValue(Context context) {
        ContentValues contentValues = new ContentValues();

        for(UserVK userVK : UserVK.getUsersList()) {

            DateTime birthDate = userVK.getBirthdayDate();
            DateTimeFormatter fmt = DateTimeFormat.forPattern(userVK.getDateFormat());
            String bdate = fmt.print(birthDate);

            int notify;
            if (userVK.isNotify()) notify = 1; else notify = 0;

            contentValues.put(DBHelper.getDbHelper(context).KEY_NAME, userVK.getName());
            contentValues.put(DBHelper.getDbHelper(context).KEY_AVATAR_URL, userVK.getAvatarURL());
            contentValues.put(DBHelper.getDbHelper(context).KEY_BDATE, bdate);
            contentValues.put(DBHelper.getDbHelper(context).KEY_DATE_FORMAT, userVK.getDateFormat());
            contentValues.put(DBHelper.getDbHelper(context).KEY_NOTIFY, notify);

            DBHelper.getDatabase(context).insert(DBHelper.TABLE_USERS, null, contentValues);
        }
    }
}
