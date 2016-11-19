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
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.util.AlarmRecevier;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Calendar;

/**
 * Created by Bodia on 28.10.2016.
 */
public class AlarmClockFragment extends AbstractTabFragment implements View.OnClickListener {
    private static final int LAYOUT=R.layout.alarm_clock_fragment_layout;

    private static String title;

    private TimePicker timePicker;
    private Button btnStop;
    private Button btnStart;
    private TextView textTime;

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

        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        textTime = (TextView) view.findViewById(R.id.textView_time);
        btnStart = (Button) view.findViewById(R.id.btn_start);
        btnStop = (Button) view.findViewById(R.id.btn_stop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        return view;
    }

    private void setTimeText(String timeText){
        textTime.setText(timeText);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {

        LocalTime localTime = new LocalTime();

        Intent alarmIntent = new Intent(getContext(), AlarmRecevier.class);
        PendingIntent alarmPendingIntent;
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        switch (v.getId()){
            case R.id.btn_start:

                alarmIntent.putExtra("intentState",1);
                alarmPendingIntent = PendingIntent.getBroadcast(getContext(),0,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);

                long delay = 0;
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String strMinute=String.valueOf(minute);

                if(minute<10) strMinute = "0"+minute;

                setTimeText("Start in: "+hour+":"+strMinute);

                delay = (timePicker.getCurrentHour()-localTime.getHourOfDay())*3600000+
                        (timePicker.getCurrentMinute()-localTime.getMinuteOfHour())*60000-
                        (localTime.getSecondOfMinute()*1000)-
                        (localTime.getMillisOfSecond());

                if(delay > 0) alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);

                break;
            case R.id.btn_stop:

                alarmIntent = new Intent(getContext(), AlarmRecevier.class);
                alarmIntent.putExtra("intentState",0);

                alarmPendingIntent = PendingIntent.getBroadcast(getContext(),0,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);

                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), alarmPendingIntent);

                setTimeText("Stop");
                break;
        }
    }
}
