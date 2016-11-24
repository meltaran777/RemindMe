package org.bogdan.remindme.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.AddAlarmActivity;
import org.bogdan.remindme.activities.AlarmDialogActivity;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.util.AlarmReceiver;
import org.joda.time.LocalTime;

import java.util.List;

/**
 * Created by Bodia on 28.10.2016.
 */
public class AlarmClockFragment extends AbstractTabFragment implements View.OnClickListener,AdapterView.OnItemClickListener {
    private static final int LAYOUT=R.layout.alarm_clock_fragment_layout;

    private static String title;

    private FloatingActionButton btnAdd;
    private ListView alarmList;

    private SimpleAdapter adapter;
    private int[] to = {R.id.textTime, R.id.textWhen, R.id.textDescription, R.id.cbEnable };

    AlarmManager alarmMgr;

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

        btnAdd = (FloatingActionButton) view.findViewById(R.id.btn_add_alarm);
        alarmList = (ListView) view.findViewById(R.id.listView_alarmList);

        btnAdd.setOnClickListener(this);

        alarmListSetAdapter();
        alarmList.setOnItemClickListener(this);
        alarmList.setItemsCanFocus(true);

        alarmMgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        boolean daysArray[] = data.getBooleanArrayExtra("arrayDayOfWeek");
        int hour = data.getIntExtra("hour",0);
        int minute = data.getIntExtra("minute",0);
        int id = data.getIntExtra("alarmId",-1);
        String descString = data.getStringExtra("descText");

        if (id >=0){
            AlarmClock.getAlarmList().set(id,new AlarmClock(daysArray,hour,minute,descString));
        }else AlarmClock.getAlarmList().add(new AlarmClock(daysArray,hour,minute,descString));

        alarmListSetAdapter();
    }

    private void alarmListSetAdapter(){
        adapter = new SimpleAdapter(getContext(),AlarmClock.getAlarmArrayMap(),R.layout.alarmlist_item,AlarmClock.getFrom(),to){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView,parent);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 65;
                view.setLayoutParams(params);
                return view;
            }
        };
        adapter.setViewBinder(new MyViewBinder());
        alarmList.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {

        LocalTime localTime = new LocalTime();

        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent alarmPendingIntent;
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        switch (v.getId()){
            case R.id.btn_add_alarm:
                Intent alarmAddIntent = new Intent(getContext(), AddAlarmActivity.class);
                startActivityForResult(alarmAddIntent, 1);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent alarmEditIntent = new Intent(getContext(), AddAlarmActivity.class);
        alarmEditIntent.putExtra("alarmID", position);
        startActivityForResult(alarmEditIntent, 1);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private class MyViewBinder implements SimpleAdapter.ViewBinder,CompoundButton.OnCheckedChangeListener {
        int hour = 0;
        int minute = 0;
        boolean[] alarmDays;
        String description="";
        Intent alarmIntent;
        PendingIntent alarmPendingIntent;
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            switch (view.getId()){
                case R.id.textTime:
                    int[] timeArray = (int[]) data;
                    hour = timeArray[0];
                    minute = timeArray[1];
                    String strHour = String.valueOf(hour);
                    String strMinute = String.valueOf(minute);
                    if (minute<10) strMinute = "0"+minute;
                    if (hour<10)   strHour = "0"+hour;
                    ((TextView) view).setText(strHour+":"+strMinute);
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
                    description = data.toString();
                    ((TextView) view).setText(description);
                    return true;
                case R.id.cbEnable:
                    ((CheckBox) view).setOnCheckedChangeListener(this);
                    boolean checked = false;
                    alarmDays = (boolean[]) data;
                    for (int i=0; i<alarmDays.length; i++){
                        if (alarmDays[i] == true) checked=true;
                    }
                    ((CheckBox) view).setChecked(checked);
                    return true;
            }
            return false;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            if(isChecked){
                Log.d("AlarmDebud","CB Active!");

                alarmIntent = new Intent(getContext(), AlarmReceiver.class);
                alarmIntent.putExtra("description",description);
                alarmPendingIntent = PendingIntent.getBroadcast(getContext(),0,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);

                LocalTime localTime = new LocalTime();
                if (hour <= 12) hour+=12;

                Log.d("AlarmDebud",hour +"-->"+ localTime.getHourOfDay());
                Log.d("AlarmDebud",minute +"-->"+ localTime.getMinuteOfHour());
                long delay = (hour-localTime.getHourOfDay())*3600000+
                        (minute-localTime.getMinuteOfHour())*60000-
                        (localTime.getSecondOfMinute()*1000)-
                        (localTime.getMillisOfSecond());

                Log.d("AlarmDebud","Delay"+String.valueOf(delay));

                if(delay > 0) alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);
                //if(true) alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);
            }else if (alarmPendingIntent != null) alarmMgr.cancel(alarmPendingIntent);
        }
    }

}
