package org.bogdan.remindme.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.bogdan.remindme.content.AlarmClock;

import java.util.List;

/**
 * Created by Bodia on 15.11.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "remindDB";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ALARMS = "alarms";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_BDATE = "bdate";
    public static final String KEY_DATE_FORMAT = "date_format";
    public static final String KEY_AVATAR_URL = "avatar_url";

    public static final String KEY_ID_ALARM = "_id";
    public static final String KEY_HOUR_ALARM = "hour";
    public static final String KEY_MINUTE_ALARM = "minute";
    public static final String KEY_DESC_ALARM = "description";
    public static final String KEY_TIME_TEXT_ALARM = "time_text";
    public static final String KEY_ACTIVE_ALARM = "active";
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
                + KEY_DESC_ALARM + " text,"
                + KEY_HOUR_ALARM + " integer,"
                + KEY_MINUTE_ALARM + " integer,"
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
            int textTimeInd = cursor.getColumnIndex(DBHelper.KEY_TIME_TEXT_ALARM);
            int idInd = cursor.getColumnIndex(DBHelper.KEY_ID_ALARM);
            int hourInd = cursor.getColumnIndex(DBHelper.KEY_HOUR_ALARM);
            int minuteInd = cursor.getColumnIndex(DBHelper.KEY_MINUTE_ALARM);

            do{
                int id = cursor.getInt(idInd);
                Log.d("DebugDB","DB record id = "+id);

                boolean active;
                if (cursor.getInt(activeInd) == 0) {active=false;}else active=true;

                boolean alarmDays[] = new boolean[7];
                for(int i=0; i < alarmDays.length; i++)
                    if (cursor.getInt(daysArrayInd[i]) == 0) {alarmDays[i]=false;}else alarmDays[i]=true;

                String desc = cursor.getString(deskInd);

                int hour = cursor.getInt(hourInd);
                int minute = cursor.getInt(minuteInd);

                AlarmClock alarmClock = new AlarmClock(alarmDays,hour,minute,desc,active);
                alarmList.add(alarmClock);
            }while (cursor.moveToNext());
        }else Log.d("DebugDB","0 rows");
        cursor.close();
        return alarmList;

    }

}
