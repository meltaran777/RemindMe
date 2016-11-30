package org.bogdan.remindme.content;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import org.bogdan.remindme.util.AlarmReceiver;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by Bodia on 21.11.2016.
 */

public class AlarmClock implements Comparable<AlarmClock> {
    private static final String DEBUG_TAG = "DebugAlarm";
    private static final String DEBUG_DELAY = "DebugDelay";

    final static String ATTRIBUTE_NAME_TIME = "time";
    final static String ATTRIBUTE_NAME_WHEN = "when";
    final static String ATTRIBUTE_NAME_DISC = "disc";
    final static String ATTRIBUTE_NAME_ENABLE = "checkbox";

    final static String[] from = { ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_WHEN, ATTRIBUTE_NAME_DISC ,ATTRIBUTE_NAME_ENABLE};

    private static List<AlarmClock> alarmList = new ArrayList<>();
    private static List<AlarmClock> alarmListFull = new ArrayList<>();
    private static ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    private boolean alarmDays[];
    private boolean active = false;
    private int hour;
    private int minute;
    private int alarmId;

    private String description;
    private Intent alarmIntent;

    private PendingIntent alarmPendingIntent;

    public AlarmClock(boolean checkedDays[], int hour , int minute, String description, boolean active){
        this.alarmDays = checkedDays;
        this.hour = hour;
        this.minute = minute;
        this.description = description;
        this.active = active;
        alarmId = getAlarmList().size();
    }

    public static ArrayList<Map<String, Object>> getAlarmArrayMap(){
        data.clear();
        Map<String, Object> m;

        for (int i=0; i<getAlarmList().size(); i++){

            int minute = getAlarmList().get(i).getMinute();
            int hour = getAlarmList().get(i).getHour();
            int arrayInt[] = {hour,minute};

            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TIME, arrayInt);
            m.put(ATTRIBUTE_NAME_WHEN, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_DISC, getAlarmList().get(i).getDescription());
            m.put(ATTRIBUTE_NAME_ENABLE, getAlarmList().get(i).isActive());
            data.add(m);
        }
        return data;
    }

    public static List<AlarmClock> getAlarmListFull(List<AlarmClock> alarmList){
        alarmListFull.clear();
        /*
        for(AlarmClock alarmClock : alarmList) {
            Log.d(DEBUG_TAG, "getAlarmListFull:StartDaysArray");
            for (boolean day : alarmClock.getAlarmDays()) {
                Log.d(DEBUG_TAG, "Day =  " + day);
            }
        }
        */
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
                    /*
                    Log.d(DEBUG_TAG, "getAlarmListFull:alarmDaysForFullList Before Add");
                    for (boolean day1 : alarmDaysForFullList) {
                        Log.d(DEBUG_TAG, "Day =  " + day1);
                    }
                    */
                    AlarmClock alarmClockFullList = new AlarmClock(alarmDaysForFullList, alarmClock.getHour(), alarmClock.getMinute(), alarmClock.getDescription(), alarmClock.isActive());
                    alarmListFull.add(alarmClockFullList);
                    //for (int j=0; j<alarmDaysForFullList.length; j++) alarmDaysForFullList[j] = false;

                    /*
                    Log.d(DEBUG_TAG, "getAlarmListFull:AfterAddToFullList "+alarmListFull.indexOf(alarmClockFullList));
                    for (boolean day1 : alarmClockFullList.getAlarmDays()) {
                        Log.d(DEBUG_TAG, "Day =  " + day1);
                    }
                    */
                }
                i++;
            }
            if(single) alarmListFull.add(new AlarmClock(alarmClock.getAlarmDays(), alarmClock.getHour(), alarmClock.getMinute(), alarmClock.getDescription(), alarmClock.isActive()));
        }
        /*
        for(AlarmClock alarmClock : alarmListFull) {
            Log.d(DEBUG_TAG, "getAlarmListFull:EndDaysArray "+alarmClock.getDescription());
            for (boolean day : alarmClock.getAlarmDays()) {
                Log.d(DEBUG_TAG, "Day =  " + day);
            }
        }
        */
        return alarmListFull;
    }

    @Override
    public int compareTo(AlarmClock another) {

        Long alarmTimeThis = getAlarmTimeInMillis(this);
        Long alarmTimeAnother = getAlarmTimeInMillis(another);

        return alarmTimeThis.compareTo(alarmTimeAnother);
    }

    @Override
    public String toString() {
        String strHour=String.valueOf(hour);
        if(hour<10) strHour = "0"+hour;
        String strMinute=String.valueOf(minute);
        if(minute<10) strMinute = "0"+minute;

        String strAlarm = description+" "+strHour+":"+strMinute+" Active"+active;
        for (boolean day : alarmDays) strAlarm += "\n"+day;
        return strAlarm;
    }

    public static long getAlarmTimeInMillis(AlarmClock alarmClock) {
        Calendar alarmCalendar = GregorianCalendar.getInstance();
        //alarmCalendar.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        boolean[] alarmDays = alarmClock.getAlarmDays();
        int dayOfWeek;
        int hour = alarmClock.getHour();
        int minute = alarmClock.getMinute();
        boolean single = true;
        LocalDateTime now;
        LocalDateTime alarmTime;

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
                    //Log.d(DEBUG_DELAY, "getAlarmTimeInMillis:AlarmCalendarTimeBeforeNow "+alarmCalendar.toString());
                    //Log.d(DEBUG_DELAY, "getAlarmTimeInMillis:AlarmJodaTimeBeforeNow "+alarmTime.toString());
                }else {
                    int dayOfMonth = alarmTime.getDayOfMonth();
                    int month = alarmTime.getMonthOfYear();
                    int year = alarmTime.getYear();
                    alarmCalendar.set(year, month-1, dayOfMonth);
                    //Log.d(DEBUG_DELAY, "getAlarmTimeInMillis:AlarmCalendarTimeAfterNow "+alarmCalendar.toString());
                    //Log.d(DEBUG_DELAY, "getAlarmTimeInMillis:AlarmJodaTimeAfterNow "+alarmTime.toString());
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
                /*
                dayOfWeek = alarmTime.getDayOfWeek();
                switch (dayOfWeek){
                    case 1: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    case 2: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                    case 3: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                    case 4: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                    case 5: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FEBRUARY);
                    case 6: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                    case 7: alarmCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }
                */
            }else {
                int dayOfMonth = alarmTime.getDayOfMonth();
                int month = alarmTime.getMonthOfYear();
                int year = alarmTime.getYear();
                alarmCalendar.set(year, month-1, dayOfMonth);
                //Log.d(DEBUG_DELAY, "getAlarmTimeInMillis:AlarmCalendarTimeAfterNow:Single "+alarmCalendar.toString());
            }
        }

        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
        alarmCalendar.set(Calendar.MINUTE, minute);
        alarmCalendar.set(Calendar.SECOND, 0);
        alarmCalendar.set(Calendar.MILLISECOND, 0);

        Long alarmTimeMillis = alarmCalendar.getTimeInMillis();
        Long nowMillis = GregorianCalendar.getInstance().getTimeInMillis();
        Long diff = alarmTimeMillis - nowMillis;
        Log.d(DEBUG_DELAY, "InMillis:Calendar "+alarmCalendar.toString());
        Log.d(DEBUG_DELAY, "InMillis:Diff "+diff);

        return alarmTimeMillis;
    }

    public static void createAlarm(Context context ,AlarmManager am, List<AlarmClock> alarmList){
        List<AlarmClock> alarmListFull = getAlarmListFull(alarmList);
        Collections.sort(alarmListFull);

        Log.d(DEBUG_TAG, "createAlarm:alarmFullList");
        for (AlarmClock alarmClock : alarmListFull) {
            Log.d(DEBUG_TAG, "Active = " + alarmClock.isActive() + " Index = " + alarmListFull.indexOf(alarmClock) + " Desc " + alarmClock.getDescription());
            //for (boolean day : alarmClock.getAlarmDays()) Log.d(DEBUG_TAG, "Day = " + day);
            Long diff = getAlarmTimeInMillis(alarmClock) - GregorianCalendar.getInstance().getTimeInMillis();
            Log.d(DEBUG_TAG, "createAlarm:DelayIs:"+" Hour = "+diff/(1000*60*60)+" Second = "+diff/1000);
        }

        for (AlarmClock alarmClock : alarmListFull) {
            if (alarmClock.isActive()) {
                Log.d(DEBUG_TAG, "createAlarm");
                //for (boolean day : alarmClock.getAlarmDays()) Log.d(DEBUG_TAG, "Day = "+day);
                Log.d(DEBUG_TAG, "Active = "+alarmClock.isActive() + " Index = "+alarmListFull.indexOf(alarmClock)+" Desc "+alarmClock.getDescription());

                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                alarmIntent.putExtra("description",alarmClock.getDescription());
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                am.set(AlarmManager.RTC_WAKEUP, getAlarmTimeInMillis(alarmClock), alarmPendingIntent);
                return;
            }
        }
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

    public PendingIntent getAlarmPendingIntent() {
        return alarmPendingIntent;
    }

    public void setAlarmPendingIntent(PendingIntent alarmPendingIntent) {
        this.alarmPendingIntent = alarmPendingIntent;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getAlarmId() {
        return alarmId;
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
        m.put(ATTRIBUTE_NAME_WHEN, getAlarmList().get(i).getAlarmDays());
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
        m.put(ATTRIBUTE_NAME_WHEN, getAlarmList().get(i).getAlarmDays());
        m.put(ATTRIBUTE_NAME_DISC, getAlarmList().get(i).getDescription());
        m.put(ATTRIBUTE_NAME_ENABLE, getAlarmList().get(i).isActive());
        data.add(m);
    }

    public static List<String> getStringAlarmList() {
        List<String> alarmStringList=new ArrayList<>();
        for(AlarmClock alarm:getAlarmList()) alarmStringList.add(alarm.toString());
        return alarmStringList;
    }
    public static  void createAlarm(Context context, AlarmManager alarmMgr, long delay, int id, String description){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("description",description);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context,id,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmClock.getAlarmList().get(id).setAlarmPendingIntent(alarmPendingIntent);

        //Log.d(DEBUG_TAG,"Create alarm: "+"ID "+id+" Delay "+delay);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, AlarmClock.getAlarmList().get(id).getAlarmPendingIntent());
    }

    public static  void cancelAlarm(AlarmManager alarmMgr, PendingIntent alarmPendingIntent){
        //Log.d(DEBUG_TAG,"Cancel pendingIntent");
        alarmMgr.cancel(alarmPendingIntent);
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
