package org.bogdan.remindme.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
public class AlarmClockFragment extends AbstractTabFragment implements View.OnClickListener,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    private static final int LAYOUT = R.layout.alarm_clock_fragment_layout;
    private static final String DEBUG_TAG = "DebugAlarmFragment";
    public static final String CREATE_ALARM_ACTION = "org.bogdan.remindme.CREATE_ALARM";
    public static final String EDIT_ALARM_ACTION = "org.bogdan.remindme.EDIT_ALARM";

    private static String title;

    private FloatingActionButton btnAdd;
    private ListView alarmList;

    private SimpleAdapter adapter;

    private int[] to = {R.id.textTime,
            R.id.textMn ,R.id.textTs ,R.id.textWd ,R.id.textTh ,R.id.textFr ,R.id.textSt ,R.id.textSn,
            R.id.textDescription, R.id.cbEnable };

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
        registerForContextMenu(alarmList);

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
            if (active) {
                if (alarmId >= 0) {   //alarmId>0 alarm exist-update,alarmId<0 there is no such alarm-add
                    alarmClock.setAlarmId(alarmId);
                    AlarmClock.getAlarmList().set(alarmId, alarmClock);
                } else AlarmClock.getAlarmList().add(alarmClock);
            }else if(alarmId >= 0) {
                alarmClock.setAlarmId(alarmId);
                AlarmClock.getAlarmList().set(alarmId, alarmClock);
            }

            ContentValues contentValues = new ContentValues();
            DBHelper.putAlarmValue(getContext(), contentValues, descString, ringtoneURI , active, daysArray, hour, minute);
            //DBHelper.putAlarmValue(getContext(), contentValues, alarmClock);
            if (alarmId >= 0){
                //update record in DB
                int alarmIdDB = alarmId+1;
                String strAlarmIdDb = String.valueOf(alarmIdDB);
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
                //DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[] {strAlarmIdDb} );
            }else{
                //Add record to DB
                DBHelper.getDatabase(getContext()).insert(DBHelper.TABLE_ALARMS, null, contentValues);
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
                alarmAddIntent.setAction(CREATE_ALARM_ACTION);
                startActivityForResult(alarmAddIntent, 1);
                break;
        }
    }

    public int position = -1;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.position=position;

        Intent alarmEditIntent = new Intent(getContext(), AddAlarmActivity.class);
        alarmEditIntent.setAction(EDIT_ALARM_ACTION);
        alarmEditIntent.putExtra("alarmID", position);
        alarmEditIntent.putExtra("alarmActive", AlarmClock.getAlarmList().get(position).isActive());
        startActivityForResult(alarmEditIntent, 1);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            for (int i=0; i<AlarmClock.getAlarmList().size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_ID_ALARM_UPDATE, i);
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, null, null);
            }

            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int alarmIdDB = acmi.position+1;
            String strAlarmIdDb = String.valueOf(alarmIdDB);
            DBHelper.getDatabase(getContext()).delete(DBHelper.TABLE_ALARMS, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
            //DBHelper.getDatabase(getContext()).delete(DBHelper.TABLE_ALARMS, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[] {strAlarmIdDb} );

            AlarmClock.getAlarmList().remove(acmi.position);
            AlarmClock.recreateAlarmListId();
            AlarmClock.getAlarmArrayMap();
            adapter.notifyDataSetChanged();

            for (int i=0; i<AlarmClock.getAlarmList().size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_ID_ALARM_UPDATE, i);
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, null, null);
            }
            return true;
        }
        return super.onContextItemSelected(item);
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
            switch (view.getId()){
                case R.id.textTime:
                    int[] timeArray = (int[]) data;
                    hour = timeArray[0];
                    minute = timeArray[1];
                    position = timeArray[2];
                    String strHour = String.valueOf(hour);
                    String strMinute = String.valueOf(minute);
                    if (minute<10) strMinute = "0"+minute;
                    if (hour<10)   strHour = "0"+hour;
                    ((TextView) view).setText(strHour+":"+strMinute);
                    return true;
                case R.id.textMn:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[0])((TextView) view).setText("Mn");
                   return true;
                case R.id.textTs:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[1])((TextView) view).setText("Ts");
                    return true;
                case R.id.textWd:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[2])((TextView) view).setText("Wd");
                    return true;
                case R.id.textTh:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[3])((TextView) view).setText("Th");
                    return true;
                case R.id.textFr:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[4])((TextView) view).setText("Fr");
                    return true;
                case R.id.textSt:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[5])((TextView) view).setText("St");
                    return true;
                case R.id.textSn:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if(alarmDays[6])((TextView) view).setText("Sn");
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
            //DBHelper.putAlarmValue(getContext(), contentValues, alarmClock);
            int alarmIdDB = alarmClock.getAlarmId() + 1;
            String strAlarmIdDb = String.valueOf(alarmIdDB);
            DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
            //DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[] {strAlarmIdDb} );

            AlarmClock.getAlarmList().get(position).setActive(isChecked);
            AlarmClock.getAlarmArrayMap(); //update data that set to adapter
            //Toast.makeText(getContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
        }
    }
}
