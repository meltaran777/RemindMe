package org.bogdan.remindme.content;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import org.bogdan.remindme.activities.AlarmDialogActivity;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.util.AlarmReceiver;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bodia on 21.11.2016.
 */

public class AlarmClock implements Comparable<AlarmClock> {
    private static final String DEBUG_TAG = "DebugAlarm";
    private static final String DEBUG_DELAY = "DebugDelay";

    final static String ATTRIBUTE_NAME_TIME = "time";
    final static String ATTRIBUTE_NAME_MN = "mn";
    final static String ATTRIBUTE_NAME_TS = "ts";
    final static String ATTRIBUTE_NAME_WD = "wd";
    final static String ATTRIBUTE_NAME_TH = "th";
    final static String ATTRIBUTE_NAME_FR = "fr";
    final static String ATTRIBUTE_NAME_ST = "st";
    final static String ATTRIBUTE_NAME_SN = "sn";
    final static String ATTRIBUTE_NAME_DISC = "disc";
    final static String ATTRIBUTE_NAME_ENABLE = "checkbox";

    final static String[] from = { ATTRIBUTE_NAME_TIME,
            ATTRIBUTE_NAME_MN,ATTRIBUTE_NAME_TS,ATTRIBUTE_NAME_WD,ATTRIBUTE_NAME_TH,ATTRIBUTE_NAME_FR,ATTRIBUTE_NAME_ST,ATTRIBUTE_NAME_SN,
            ATTRIBUTE_NAME_DISC ,ATTRIBUTE_NAME_ENABLE};
    public static final String ALARM_CLOCK_CREATE_ACTION = "org.bogdan.remindme.CREATE_ALARM";
    private static final String ALARM_CANCEL_ACTION = "org.bogdan.remindme.CANCEL_ALARM";

    private static List<AlarmClock> alarmList = new ArrayList<>();
    private static List<AlarmClock> alarmListFull = new ArrayList<>();
    private static ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    private boolean alarmDays[];
    private boolean active = false;
    private int hour;
    private int minute;
    private int alarmId;
    private String ringtoneURI;
    private String description;
    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;

    boolean debug = false;

    public AlarmClock(boolean checkedDays[], int hour , int minute, String description, boolean active,String ringtoneURI, List<AlarmClock> alarmList){
        this.alarmDays = checkedDays;
        this.hour = hour;
        this.minute = minute;
        this.description = description;
        this.active = active;
        this.ringtoneURI = ringtoneURI;
        alarmId = alarmList.size();
    }

    public static ArrayList<Map<String, Object>> getAlarmArrayMap(){
        data.clear();
        Map<String, Object> m;

        for (int i=0; i<getAlarmList().size(); i++){

            int minute = getAlarmList().get(i).getMinute();
            int hour = getAlarmList().get(i).getHour();
            int id = getAlarmList().get(i).getAlarmId();
            int arrayInt[] = {hour,minute,id};

            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TIME, arrayInt);
            m.put(ATTRIBUTE_NAME_MN, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_TS, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_WD, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_TH, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_FR, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_ST, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_SN, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_DISC, getAlarmList().get(i).getDescription());
            m.put(ATTRIBUTE_NAME_ENABLE, getAlarmList().get(i).isActive());
            data.add(m);
        }
        return data;
    }

    public static List<AlarmClock> getAlarmListFull(List<AlarmClock> alarmList){
        alarmListFull.clear();
        for (AlarmClock alarmClock : alarmList) {
            boolean alarmDays[] = alarmClock.getAlarmDays();
            int i=0;
            boolean single = true;
            for (boolean day : alarmDays) {
                boolean alarmDaysForFullList[] = new boolean[7];
                for (int j=0; j<alarmDaysForFullList.length; j++) alarmDaysForFullList[j] = false;
                if (day) {
                    single = false;
                    alarmDaysForFullList[i] = true;
                    AlarmClock alarmClockFullList = new AlarmClock(alarmDaysForFullList, alarmClock.getHour(), alarmClock.getMinute(), alarmClock.getDescription(), alarmClock.isActive(),alarmClock.getRingtoneURI(), alarmList);
                    alarmClockFullList.setAlarmId(alarmClock.getAlarmId());
                    alarmListFull.add(alarmClockFullList);
                }
                i++;
            }
            if(single) {
                AlarmClock alarmClockFullList = new AlarmClock(alarmClock.getAlarmDays(), alarmClock.getHour(), alarmClock.getMinute(), alarmClock.getDescription(), alarmClock.isActive(), alarmClock.getRingtoneURI(), alarmList);
                alarmClockFullList.setAlarmId(alarmClock.getAlarmId());
                alarmListFull.add(alarmClockFullList);
            }
        }
        return alarmListFull;
    }

    @Override
    public int compareTo(AlarmClock another) {

        Long alarmTimeThis = this.getAlarmTimeInMillis();
        Long alarmTimeAnother = another.getAlarmTimeInMillis();

        return alarmTimeThis.compareTo(alarmTimeAnother);
    }

    @Override
    public String toString() {
        String strHour=String.valueOf(hour);
        if(hour<10) strHour = "0"+hour;
        String strMinute=String.valueOf(minute);
        if(minute<10) strMinute = "0"+minute;

        String strAlarm = description+" "+strHour+":"+strMinute+" Active"+active;
        //for (boolean day : alarmDays) strAlarm += "\n"+day;
        return strAlarm;
    }

    public long getAlarmTimeInMillis() {
        Calendar alarmCalendar = Calendar.getInstance();
        boolean[] alarmDays = getAlarmDays();
        int dayOfWeek;
        int hour = getHour();
        int minute = getMinute();
        boolean single = true;
        LocalDateTime now;
        LocalDateTime alarmTime = new LocalDateTime();

        for (int i=0; i<alarmDays.length; i++) {
            if (alarmDays[i]) {
                single = false;
                dayOfWeek = i + 1;
                alarmTime = new LocalDateTime().withDayOfWeek(dayOfWeek).withHourOfDay(hour).withMinuteOfHour(minute);
                now = new LocalDateTime().plusMillis(1);
                if(alarmTime.isBefore(now)) {
                    alarmTime = alarmTime.plusWeeks(1);
                    int dayOfMonth = alarmTime.getDayOfMonth();
                    int month = alarmTime.getMonthOfYear();
                    int year = alarmTime.getYear();
                    alarmCalendar.set(year, month-1, dayOfMonth);
                }else {
                    int dayOfMonth = alarmTime.getDayOfMonth();
                    int month = alarmTime.getMonthOfYear();
                    int year = alarmTime.getYear();
                    alarmCalendar.set(year, month-1, dayOfMonth);
                }
                break;
            }
        }
        if (single) {
            alarmTime = new LocalDateTime().withHourOfDay(hour).withMinuteOfHour(minute);
            now = new LocalDateTime().plusMillis(1);
            if(alarmTime.isBefore(now)) {
                alarmTime = alarmTime.plusDays(1);
                int dayOfMonth = alarmTime.getDayOfMonth();
                int month = alarmTime.getMonthOfYear();
                int year = alarmTime.getYear();
                alarmCalendar.set(year, month-1, dayOfMonth);
            }else {
                int dayOfMonth = alarmTime.getDayOfMonth();
                int month = alarmTime.getMonthOfYear();
                int year = alarmTime.getYear();
                alarmCalendar.set(year, month-1, dayOfMonth);
            }
        }

        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
        alarmCalendar.set(Calendar.MINUTE, minute);
        alarmCalendar.set(Calendar.SECOND, 0);
        alarmCalendar.set(Calendar.MILLISECOND, 0);

        Long alarmTimeMillis = alarmCalendar.getTimeInMillis();
        Long nowMillis = GregorianCalendar.getInstance().getTimeInMillis();
        Long diff = alarmTimeMillis - nowMillis;
        if (debug) {
            Log.d(DEBUG_DELAY, "InMillis:AlarmTimeJoda "+alarmTime.toString());
            Log.d(DEBUG_DELAY, "InMillis:Calendar " + alarmCalendar.toString());
            Log.d(DEBUG_DELAY, "InMillis:Diff: " +"Hour = "+diff/(1000*36000) +" Second = " +diff/1000);
        }
        return alarmTimeMillis;
    }

    public long getAlarmDiffInMillis() {
        Calendar alarmCalendar = Calendar.getInstance();
        boolean[] alarmDays = getAlarmDays();
        int dayOfWeek;
        int hour = getHour();
        int minute = getMinute();
        boolean single = true;
        LocalDateTime now;
        LocalDateTime alarmTime = new LocalDateTime();

        for (int i=0; i<alarmDays.length; i++) {
            if (alarmDays[i]) {
                single = false;
                dayOfWeek = i + 1;
                alarmTime = new LocalDateTime().withDayOfWeek(dayOfWeek).withHourOfDay(hour).withMinuteOfHour(minute);
                now = new LocalDateTime().plusMillis(1);
                if(alarmTime.isBefore(now)) {
                    alarmTime = alarmTime.plusWeeks(1);
                    int dayOfMonth = alarmTime.getDayOfMonth();
                    int month = alarmTime.getMonthOfYear();
                    int year = alarmTime.getYear();
                    alarmCalendar.set(year, month-1, dayOfMonth);
                }else {
                    int dayOfMonth = alarmTime.getDayOfMonth();
                    int month = alarmTime.getMonthOfYear();
                    int year = alarmTime.getYear();
                    alarmCalendar.set(year, month-1, dayOfMonth);
                }
            }
        }
        if (single) {
            alarmTime = new LocalDateTime().withHourOfDay(hour).withMinuteOfHour(minute);
            now = new LocalDateTime().plusMillis(1);
            if(alarmTime.isBefore(now)) {
                alarmTime = alarmTime.plusDays(1);
                int dayOfMonth = alarmTime.getDayOfMonth();
                int month = alarmTime.getMonthOfYear();
                int year = alarmTime.getYear();
                alarmCalendar.set(year, month-1, dayOfMonth);
            }else {
                int dayOfMonth = alarmTime.getDayOfMonth();
                int month = alarmTime.getMonthOfYear();
                int year = alarmTime.getYear();
                alarmCalendar.set(year, month-1, dayOfMonth);
            }
        }

        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
        alarmCalendar.set(Calendar.MINUTE, minute);
        alarmCalendar.set(Calendar.SECOND, 0);
        alarmCalendar.set(Calendar.MILLISECOND, 0);

        Long alarmTimeMillis = alarmCalendar.getTimeInMillis();
        Long nowMillis = GregorianCalendar.getInstance().getTimeInMillis();
        Long diff = alarmTimeMillis - nowMillis;
        if (debug) {
            Log.d(DEBUG_DELAY, "InMillis:AlarmTimeJoda "+alarmTime.toString());
            Log.d(DEBUG_DELAY, "InMillis:Calendar " + alarmCalendar.toString());
            Log.d(DEBUG_DELAY, "InMillis:Diff: " +"Hour = "+diff/(1000*36000) +" Second = " +diff/1000);
        }
        return diff;
    }

    public static boolean createAlarm(Context context ,AlarmManager am, List<AlarmClock> alarmList, boolean receiverMod){
        List<AlarmClock> alarmListActive = new ArrayList<>();
        List<AlarmClock> alarmListFull = getAlarmListFull(alarmList);
        Collections.sort(alarmListFull);
        for (AlarmClock alarmClock : alarmListFull) {
            //Log.d(DEBUG_TAG, "createAlarm:DelayIs:"+" Hour = "+diff/(1000*60*60)+" Second = "+diff/1000);
            if (alarmClock.isActive() && alarmClock.isSingle()) alarmListActive.add(alarmClock);
        }
        Collections.sort(alarmListActive);
        for (AlarmClock alarmClock : alarmListFull) {
            if (alarmClock.isActive()) {
                //When call from receiver off single alarm(Update DB data)
                if (receiverMod){
                    if (alarmClock.isSingle()){

                        AlarmClock alarmClockActive = alarmListActive.get(alarmListActive.size()-1);

                        int alarmIdDB = alarmClockActive.getAlarmId()+1;
                        String strAlarmIdDb = String.valueOf(alarmIdDB);
                        ContentValues contentValues = new ContentValues();
                        //DBHelper.putAlarmValue(context, contentValues, alarmClockActive.getDescription(), alarmClockActive.getRingtoneURI(), false, alarmClockActive.getAlarmDays(), alarmClockActive.getHour(), alarmClockActive.getMinute());
                        DBHelper.putAlarmValue(context, contentValues, alarmClockActive);

                        Log.d(DEBUG_TAG, "Active = "+alarmClockActive.isActive() + " ID = "+alarmClockActive.getAlarmId()+" Desc "+alarmClockActive.getDescription());
                        Log.d(DEBUG_TAG, "AlarmId DB = "+strAlarmIdDb);
                        //DBHelper.getDatabase(context).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
                        DBHelper.getDatabase(context).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[] {strAlarmIdDb} );
                    }
                }
                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                alarmIntent.setAction(ALARM_CLOCK_CREATE_ACTION);
                alarmIntent.putExtra("description",alarmClock.getDescription());
                alarmIntent.putExtra("ringtone",alarmClock.getRingtoneURI());
                alarmIntent.putExtra("hour",alarmClock.getHour());
                alarmIntent.putExtra("minute",alarmClock.getMinute());
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                am.set(AlarmManager.RTC_WAKEUP, alarmClock.getAlarmTimeInMillis(), alarmPendingIntent);

                return true;
            }
        }
        return false;
    }

    public static void createAlarm(Context context, AlarmManager alarmMgr,Intent alarmIntent, long delay, int id){
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,id,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);
    }


    public static  void cancelAlarmDialogShowAction(Context context, AlarmManager alarmMgr, int id){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("show",false);
        alarmIntent.setAction(AlarmDialogActivity.ALARM_DIALOG_ACTION);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), alarmPendingIntent);
    }

    public static void recreateAlarmListId() {
        int i=0;
        for (AlarmClock clock : getAlarmList()){
            clock.setAlarmId(i);
            i++;
        }
    }

    public boolean isSingle(){
        boolean single = true;
        for (boolean day : getAlarmDays()) if (day) single = false;
        return single;
    }

    public static List<AlarmClock> getAlarmList() {
        return alarmList;
    }

    public boolean[] getAlarmDays() {
        return alarmDays;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getDescription() {
        return description;
    }

    public static String[] getFrom() {
        return from;
    }

    public boolean isActive() {
        return active;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRingtoneURI() {
        return ringtoneURI;
    }

    public PendingIntent getAlarmPendingIntent() {
        return alarmPendingIntent;
    }

    public void setAlarmPendingIntent(PendingIntent alarmPendingIntent) {
        this.alarmPendingIntent = alarmPendingIntent;
    }

    public Intent getAlarmIntent() {
        return alarmIntent;
    }

    public void setAlarmIntent(Intent alarmIntent) {
        this.alarmIntent = alarmIntent;
    }
    public static void setArrayMapElem(int i){
        Map<String, Object> m;
        m = new HashMap<String, Object>();

        int minute = getAlarmList().get(i).getMinute();
        int hour = getAlarmList().get(i).getHour();
        int arrayInt[] = {hour,minute};

        m.put(ATTRIBUTE_NAME_TIME, arrayInt);
        //m.put(ATTRIBUTE_NAME_WHEN, getAlarmList().get(i).getAlarmDays());
        m.put(ATTRIBUTE_NAME_DISC, getAlarmList().get(i).getDescription());
        m.put(ATTRIBUTE_NAME_ENABLE, getAlarmList().get(i).isActive());
        data.set(i,m);
    }

    public static void addArrayMapElem(int i){
        Map<String, Object> m;
        m = new HashMap<String, Object>();

        int minute = getAlarmList().get(i).getMinute();
        int hour = getAlarmList().get(i).getHour();
        int arrayInt[] = {hour,minute};

        m.put(ATTRIBUTE_NAME_TIME, arrayInt);
        //m.put(ATTRIBUTE_NAME_WHEN, getAlarmList().get(i).getAlarmDays());
        m.put(ATTRIBUTE_NAME_DISC, getAlarmList().get(i).getDescription());
        m.put(ATTRIBUTE_NAME_ENABLE, getAlarmList().get(i).isActive());
        data.add(m);
    }

    public static List<String> getStringAlarmList() {
        List<String> alarmStringList=new ArrayList<>();
        for(AlarmClock alarm:getAlarmList()) alarmStringList.add(alarm.toString());
        return alarmStringList;
    }

    public static long calcDelay(int hour, int minute){
        long delay;
        LocalTime localTime = new LocalTime();
        //if (hour < 12) hour+=12;
        delay = (hour-localTime.getHourOfDay())*3600000+
                (minute-localTime.getMinuteOfHour())*60000-
                (localTime.getSecondOfMinute()*1000)-
                (localTime.getMillisOfSecond());
        if (delay < 0) delay += 3600000*24;
        //Log.d(DEBUG_TAG, "Hour = "+hour);
        //Log.d(DEBUG_TAG, "Minute = "+minute);
        //Log.d(DEBUG_TAG, "Delay = "+delay);
        return delay;
    }
}
