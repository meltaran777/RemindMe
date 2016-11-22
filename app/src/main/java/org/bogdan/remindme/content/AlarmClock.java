package org.bogdan.remindme.content;

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

    private int hour;
    private int minute;
    private String description;
    private static int id=0;

    public AlarmClock(boolean checkedDays[], int hour , int minute, String description){
        this.alarmDays =checkedDays;
        this.hour=hour;
        this.minute=minute;
        this.description=description;
        id++;
    }

    @Override
    public String toString() {
        String strMinute=String.valueOf(minute);
        if(minute<10) strMinute = "0"+minute;

        return description+" "+String.valueOf(hour)+":"+strMinute;
    }

    public static List<String> getStringAlarmList() {
        List<String> alarmStringList=new ArrayList<>();
        for(AlarmClock alarm:getAlarmList()) alarmStringList.add(alarm.toString());
        return alarmStringList;
    }

    public static ArrayList<Map<String, Object>> getAlarmArrayMap(){

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(getAlarmList().size());
        Map<String, Object> m;

        for (int i=0; i<getAlarmList().size(); i++){

            int minute = getAlarmList().get(i).getMinute();
            int hour = getAlarmList().get(i).getHour();
            String strMinute=String.valueOf(minute);
            if(minute<10) strMinute = "0"+minute;

            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TIME, String.valueOf(hour)+":"+strMinute);
            m.put(ATTRIBUTE_NAME_WHEN, getAlarmList().get(i).getAlarmDays());
            m.put(ATTRIBUTE_NAME_DISC, getAlarmList().get(i).getDescription());
            m.put(ATTRIBUTE_NAME_ENABLE, getAlarmList().get(i).getAlarmDays());
            data.add(m);
        }

        return data;
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

}
