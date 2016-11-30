package org.bogdan.remindme.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Toast;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.AddAlarmActivity;
import org.bogdan.remindme.activities.AlarmDialogActivity;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.util.AlarmReceiver;
import org.joda.time.Hours;
import org.joda.time.LocalTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bodia on 28.10.2016.
 */
public class AlarmClockFragment extends AbstractTabFragment implements View.OnClickListener,AdapterView.OnItemClickListener {
    private static final int LAYOUT = R.layout.alarm_clock_fragment_layout;
    private static final String DEBUG_TAG = "DebugAlarm";

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

        if (AlarmClock.getAlarmList().isEmpty()) {
            //readDB();
            DBHelper.readTableAlarms(getContext(), AlarmClock.getAlarmList());
            AlarmClock.getAlarmArrayMap();
        }

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
    public void onDestroyView() {
        DBHelper.closeDB();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        boolean daysArray[] = data.getBooleanArrayExtra("arrayDayOfWeek");
        int hour = data.getIntExtra("hour", 0);
        int minute = data.getIntExtra("minute", 0);
        int alarmId = data.getIntExtra("alarmId", -1);
        boolean active = data.getBooleanExtra("active",false);
        String descString = data.getStringExtra("descText");

        if (resultCode == Activity.RESULT_OK) {
            AlarmClock alarmClock = new AlarmClock(daysArray, hour, minute, descString, active);

            //start DB logic

            ContentValues contentValues = new ContentValues();
            putValue(contentValues,descString,active,daysArray,hour,minute);
            if (alarmId >= 0){
                //update record in DB
                int alarmIdDB = alarmId+1;
                String strAlarmIdDb = String.valueOf(alarmIdDB);
                //Log.d("DebugDB","Alarm ID = "+strAlarmIdDb);
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
                //DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, "_id=5", null );
            }else{
                //Add record to DB
                DBHelper.getDatabase(getContext()).insert(DBHelper.TABLE_ALARMS, null, contentValues);
            }

            //end DB logic

            //Start mod alarmList
            if (active) {
                if (alarmId >= 0) {   //alarmId>0 alarm exist-replace,alarmId<0 there is no alarm-add
                    alarmClock.setAlarmId(alarmId);
                    AlarmClock.getAlarmList().set(alarmId, alarmClock);
                    // AlarmClock.setArrayMapElem(alarmId);
                    //cancelAlarm(AlarmClock.getAlarmList().get(alarmId).getAlarmPendingIntent()); //Last change not test
                    //AlarmClock.createAlarm(getContext(), getAlarmMgr(), AlarmClock.calcDelay(hour, minute), alarmId, descString);
                } else {
                    AlarmClock.getAlarmList().add(alarmClock);
                    //AlarmClock.addArrayMapElem(AlarmClock.getAlarmList().indexOf(alarmClock));
                    //AlarmClock.createAlarm(getContext(), getAlarmMgr(), AlarmClock.calcDelay(hour, minute), alarmClock.getAlarmId(), descString);
                }
            }else if(alarmId >= 0) {
                //AlarmClock.cancelAlarm(alarmMgr,AlarmClock.getAlarmList().get(alarmId).getAlarmPendingIntent());
                alarmClock.setAlarmId(alarmId);
                AlarmClock.getAlarmList().set(alarmId, alarmClock);
            }
            //End mod alarmList

            /*
            for(AlarmClock ac : AlarmClock.getAlarmList()) {
                Log.d(DEBUG_TAG, "Fragment:AlarmList");
                for (boolean day : ac.getAlarmDays()) {
                    Log.d(DEBUG_TAG, "Day =  " + day);
                }
            }
            */

            AlarmClock.createAlarm(getContext(), getAlarmMgr(), AlarmClock.getAlarmList());


        }
        /*
        if (resultCode == Activity.RESULT_CANCELED){
            if (alarmId >= 0) {
                AlarmClock.getAlarmList().get(alarmId).setActive(false);
            }
        }
        */
        //for (AlarmClock alarmClock : AlarmClock.getAlarmList()) Log.d(DEBUG_TAG, "Active "+alarmClock.isActive());
        AlarmClock.getAlarmArrayMap(); //update data that set to adapter
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_alarm:
                Intent alarmAddIntent = new Intent(getContext(), AddAlarmActivity.class);
                startActivityForResult(alarmAddIntent, 1);
                break;
        }
    }

    public int position = -1;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.position=position;

        Intent alarmEditIntent = new Intent(getContext(), AddAlarmActivity.class);
        alarmEditIntent.putExtra("alarmID", position);
        alarmEditIntent.putExtra("alarmActive", AlarmClock.getAlarmList().get(position).isActive());
        startActivityForResult(alarmEditIntent, 1);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private void readDB(){

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
                AlarmClock.getAlarmList().add(alarmClock);
            }while (cursor.moveToNext());
        }else Log.d("DebugDB","0 rows");

        cursor.close();

    }

    private void putValue(ContentValues contentValues, String descString, boolean active, boolean [] daysArray, int hour, int minute) {

        contentValues.put(DBHelper.getDbHelper(getContext()).KEY_DESC_ALARM, descString);

        contentValues.put(DBHelper.getDbHelper(getContext()).KEY_TIME_TEXT_ALARM, hour+":"+minute);

        contentValues.put(DBHelper.getDbHelper(getContext()).KEY_HOUR_ALARM, hour);
        contentValues.put(DBHelper.getDbHelper(getContext()).KEY_MINUTE_ALARM, minute);

        if (active) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_ACTIVE_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_ACTIVE_ALARM, 0);

        if (daysArray[0]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_MONDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_MONDAY_ALARM, 0);

        if (daysArray[1]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_TUESDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_TUESDAY_ALARM, 0);

        if (daysArray[2]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_WEDNESDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_WEDNESDAY_ALARM, 0);

        if (daysArray[3]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_THURSDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_THURSDAY_ALARM, 0);

        if (daysArray[4]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_FRIDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_FRIDAY_ALARM, 0);

        if (daysArray[5]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_SATURDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_SATURDAY_ALARM, 0);

        if (daysArray[6]) {
            contentValues.put(DBHelper.getDbHelper(getContext()).KEY_SUNDAY_ALARM, 1);
        }else contentValues.put(DBHelper.getDbHelper(getContext()).KEY_SUNDAY_ALARM, 0);
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

    public AlarmManager getAlarmMgr() {
        return alarmMgr;
    }


    private class MyViewBinder implements SimpleAdapter.ViewBinder,CompoundButton.OnCheckedChangeListener {
        boolean[] alarmDays;
        boolean active;
        String description="";
        int hour;
        int minute;
        Intent alarmIntent;
        PendingIntent alarmPendingIntent;
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            //Log.d(DEBUG_TAG,"Start setViewValue");
            position++;
            if (position > AlarmClock.getAlarmList().size()-1) position=0;
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
                    boolean checked = (boolean) data;
                    ((CheckBox) view).setChecked(checked);
                    //Log.d(DEBUG_TAG, "setViewValue cbEnable- "+checked);
                    //Log.d(DEBUG_TAG, "setViewValue AlarmPosition- "+position);
                    //Log.d(DEBUG_TAG, "setViewValue AlarmListElemActive- "+AlarmClock.getAlarmList().get(position).isActive());
                    //((CheckBox) view).setChecked(true);
                    //((CheckBox) view).setOnCheckedChangeListener(this);
                    /*
                    alarmDays = (boolean[]) data;
                    for (int i=0; i<alarmDays.length; i++){
                        if (alarmDays[i] == true) checked=true;
                    }
                    */
                    //Toast.makeText(getContext(),"position="+position+" id="+id,Toast.LENGTH_SHORT).show();
                    //if (position == id)((CheckBox) view).setChecked(false);
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
                alarmPendingIntent = PendingIntent.getBroadcast(getContext(),position,alarmIntent,0);

                LocalTime localTime = new LocalTime();
                if (hour < 12) hour+=12;

                Log.d("AlarmDebud",hour +"-->Local"+ localTime.getHourOfDay());
                Log.d("AlarmDebud",minute +"-->Local"+ localTime.getMinuteOfHour());
                long delay = (hour-localTime.getHourOfDay())*3600000+
                        (minute-localTime.getMinuteOfHour())*60000-
                        (localTime.getSecondOfMinute()*1000)-
                        (localTime.getMillisOfSecond());

                Toast.makeText(getContext(),"Create alarm",Toast.LENGTH_SHORT).show();
                Log.d("AlarmDebud","Delay"+String.valueOf(delay));

                if(delay > 0) alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);
                //if(true) alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, alarmPendingIntent);
            }else if (alarmPendingIntent != null) {
                alarmMgr.cancel(alarmPendingIntent);
                Toast.makeText(getContext(),"Destroy alarm",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
