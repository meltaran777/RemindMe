package org.bogdan.remindme.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.AddAlarmActivity;
import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.util.AlarmRecevier;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Bodia on 28.10.2016.
 */
public class AlarmClockFragment extends AbstractTabFragment implements View.OnClickListener {
    private static final int LAYOUT=R.layout.alarm_clock_fragment_layout;

    private static String title;

    private Button btnAdd;
    private ListView alarmList;

    private SimpleAdapter adapter;
    private int[] to = {R.id.textTime, R.id.textWhen, R.id.textDescription, R.id.cbEnable };

    public static AlarmClockFragment getInstance(Context context){
        Bundle args=new Bundle();
        AlarmClockFragment fragment=new AlarmClockFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_item_Alarm_Clock));

        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);

        btnAdd = (Button) view.findViewById(R.id.btn_add_alarm);
        alarmList = (ListView) view.findViewById(R.id.listView_alarmList);

        btnAdd.setOnClickListener(this);

        adapter = new SimpleAdapter(getContext(),AlarmClock.getAlarmArrayMap(),R.layout.alarmlist_item,AlarmClock.getFrom(),to);
        adapter.setViewBinder(new MyViewBinder());
        alarmList.setAdapter(adapter);

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        boolean daysArray[] = data.getBooleanArrayExtra("arrayDayOfWeek");
        int hour = data.getIntExtra("hour",0);
        int minute = data.getIntExtra("minute",0);
        String descString = data.getStringExtra("descText");

        AlarmClock.getAlarmList().add(new AlarmClock(daysArray,hour,minute,descString));


        adapter = new SimpleAdapter(getContext(),AlarmClock.getAlarmArrayMap(),R.layout.alarmlist_item,AlarmClock.getFrom(),to);
        adapter.setViewBinder(new MyViewBinder());
        alarmList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

        LocalTime localTime = new LocalTime();

        Intent alarmIntent = new Intent(getContext(), AlarmRecevier.class);
        PendingIntent alarmPendingIntent;
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        switch (v.getId()){
            case R.id.btn_add_alarm:
                Intent alarmAddIntent = new Intent(getContext(), AddAlarmActivity.class);
                startActivityForResult(alarmAddIntent, 1);
                break;
        }
    }


    public void setContext(Context context) {
        this.context = context;
    }

    private class MyViewBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            boolean[] alarmDays;
            switch (view.getId()){
                case R.id.textTime:
                    String time = data.toString();
                    ((TextView) view).setText(time);
                    return true;
                case R.id.textWhen:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    for(int i=0;i<alarmDays.length;i++){
                        boolean day=alarmDays[i];
                        if(day == true) switch (i){
                            case 0:
                                ((TextView) view).setText(((TextView) view).getText()+"Mn ");
                                break;
                            case 1:
                                ((TextView) view).setText(((TextView) view).getText()+"Ts ");
                                break;
                            case 2:
                                ((TextView) view).setText(((TextView) view).getText()+"Wd ");
                                break;
                            case 3:
                                ((TextView) view).setText(((TextView) view).getText()+"Th ");
                                break;
                            case 4:
                                ((TextView) view).setText(((TextView) view).getText()+"Fr ");
                                break;
                            case 5:
                                ((TextView) view).setText(((TextView) view).getText()+"St ");
                                break;
                            case 6:
                                ((TextView) view).setText(((TextView) view).getText()+"Sn ");
                                break;
                        }
                    }
                   return true;
                case R.id.textDescription:
                    String description = data.toString();
                    ((TextView) view).setText(description);
                    return true;
                case R.id.cbEnable:
                    boolean checked = false;
                    alarmDays = (boolean[]) data;
                    for (int i=0; i<alarmDays.length; i++){
                        if (alarmDays[i] == true) checked=true;
                    }
                    ((CheckBox)view).setChecked(checked);
                    return true;
            }
            return false;
        }
    }
    //Example use Intent,PendingIntent,BroadcastRecevier;
    /*            case R.id.btn_start:
                int hour=0;
                int minute=0;

                alarmIntent.putExtra("intentState",1);
                alarmPendingIntent = PendingIntent.getBroadcast(getContext(),0,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);

                long delay = 0;
                String strMinute=String.valueOf(minute);

                if(minute<10) strMinute = "0"+minute;

                setTimeText("Start in: "+hour+":"+strMinute);

                delay = (hour-localTime.getHourOfDay())*3600000+
                        (minute-localTime.getMinuteOfHour())*60000-
                        (localTime.getSecondOfMinute()*1000)-
                        (localTime.getMillisOfSecond());

                if(delay > 0) alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);

                break;  */
}
