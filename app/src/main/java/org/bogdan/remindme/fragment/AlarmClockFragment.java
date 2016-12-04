package org.bogdan.remindme.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.database.DBHelper;

/**
 * Created by Bodia on 28.10.2016.
 */
public class AlarmClockFragment extends AbstractTabFragment implements View.OnClickListener,AdapterView.OnItemClickListener {
    private static final int LAYOUT = R.layout.alarm_clock_fragment_layout;
    private static final String DEBUG_TAG = "DebugAlarmFragment";

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
        String ringtoneURI = data.getStringExtra("ringtone");

        if (resultCode == Activity.RESULT_OK) {
            AlarmClock alarmClock = new AlarmClock(daysArray, hour, minute, descString, active, ringtoneURI, AlarmClock.getAlarmList());
            ContentValues contentValues = new ContentValues();
            DBHelper.putAlarmValue(getContext(), contentValues, descString, ringtoneURI , active, daysArray, hour, minute);
            if (alarmId >= 0){
                //update record in DB
                int alarmIdDB = alarmId+1;
                String strAlarmIdDb = String.valueOf(alarmIdDB);
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
            }else{
                //Add record to DB
                DBHelper.getDatabase(getContext()).insert(DBHelper.TABLE_ALARMS, null, contentValues);
            }

            if (active) {
                if (alarmId >= 0) {   //alarmId>0 alarm exist-update,alarmId<0 there is no such alarm-add
                    alarmClock.setAlarmId(alarmId);
                    AlarmClock.getAlarmList().set(alarmId, alarmClock);
                } else AlarmClock.getAlarmList().add(alarmClock);
            }else if(alarmId >= 0) {
                alarmClock.setAlarmId(alarmId);
                AlarmClock.getAlarmList().set(alarmId, alarmClock);
            }
            AlarmClock.createAlarm(getContext(), getAlarmMgr(), AlarmClock.getAlarmList(), false);
        }
        /*
        if (resultCode == Activity.RESULT_CANCELED){
            if (alarmId >= 0) {
                AlarmClock.getAlarmList().get(alarmId).setActive(false);
            }
        }
        */
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

    public void setContext(Context context) {
        this.context = context;
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
        int position = 0;
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (position > AlarmClock.getAlarmList().size() - 1) position = 0;
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
                    ((CheckBox) view).setOnCheckedChangeListener(null);
                    boolean checked = (boolean) data;
                    ((CheckBox) view).setChecked(checked);
                    ((CheckBox) view).setTag(position);
                    ((CheckBox) view).setOnCheckedChangeListener(this);
                    position++;
                    return true;
            }
            return false;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            int position = (int) buttonView.getTag();

            AlarmClock alarmClock = AlarmClock.getAlarmList().get(position);
            ContentValues contentValues = new ContentValues();
            DBHelper.putAlarmValue(getContext(), contentValues, alarmClock.getDescription(), alarmClock.getRingtoneURI(), isChecked, alarmClock.getAlarmDays(), alarmClock.getHour(), alarmClock.getMinute());

            int alarmIdDB = alarmClock.getAlarmId() + 1;
            String strAlarmIdDb = String.valueOf(alarmIdDB);
            DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );

            AlarmClock.getAlarmList().get(position).setActive(isChecked);
            //Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
        }
    }
}
