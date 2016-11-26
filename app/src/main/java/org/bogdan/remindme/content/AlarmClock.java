package org.bogdan.remindme.content;

import android.app.PendingIntent;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Bodia on 21.11.2016.
 */

public class AlarmClock {
    final static String ATTRIBUTE_NAME_TIME = "time";
    final static String ATTRIBUTE_NAME_WHEN = "when";
    final static String ATTRIBUTE_NAME_DISC = "disc";
    final static String ATTRIBUTE_NAME_ENABLE = "checkbox";

    final static String[] from = { ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_WHEN, ATTRIBUTE_NAME_DISC ,ATTRIBUTE_NAME_ENABLE};

    private static List<AlarmClock> alarmList = new ArrayList<>();

    private boolean alarmDays[];
    private boolean active = false;
    private int hour;
    private int minute;
    private int alarmId;
    private String description;

    private Intent alarmIntent;
    private PendingIntent alarmPendingIntent;

    public AlarmClock(boolean checkedDays[], int hour , int minute, String description, boolean active){
        this.alarmDays =checkedDays;
        this.hour=hour;
        this.minute=minute;
        this.description=description;
        this.active = active;
        alarmId = getAlarmList().size();
    }

    private static ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

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

    @Override
    public String toString() {
        String strMinute=String.valueOf(minute);
        if(minute<10) strMinute = "0"+minute;

        return description+" "+String.valueOf(hour)+":"+strMinute;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getAlarmId() {
        return alarmId;
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

    public Intent getAlarmIntent() {
        return alarmIntent;
    }

    public void setAlarmIntent(Intent alarmIntent) {
        this.alarmIntent = alarmIntent;
    }

}
