package org.bogdan.remindme.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.AddAlarmActivity;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.database.DBHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
            R.id.textMn, R.id.textTs, R.id.textWd, R.id.textTh, R.id.textFr, R.id.textSt, R.id.textSn,
            R.id.textDescription, R.id.cbEnable};

    AlarmManager alarmMgr;

    public static AlarmClockFragment getInstance(Context context) {
        Bundle args = new Bundle();
        AlarmClockFragment fragment = new AlarmClockFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_item_Alarm_Clock));

        return fragment;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (AlarmClock.getAlarmList().isEmpty()) {
            DBHelper.readTableAlarms(getContext(), AlarmClock.getAlarmList());
            AlarmClock.getAlarmArrayMap();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        boolean daysArray[] = data.getBooleanArrayExtra("arrayDayOfWeek");
        int hour = data.getIntExtra("hour", 0);
        int minute = data.getIntExtra("minute", 0);
        int alarmId = data.getIntExtra("alarmId", -1);
        boolean active = data.getBooleanExtra("active", false);
        String descString = data.getStringExtra("descText");
        String ringtoneURI = data.getStringExtra("ringtone");

        if (resultCode == Activity.RESULT_OK) {
            AlarmClock alarmClock = new AlarmClock(daysArray, hour, minute, descString, active, ringtoneURI, AlarmClock.getAlarmList());
            if (active) {
                if (alarmId >= 0) { //update
                    alarmClock.setAlarmId(alarmId);
                    AlarmClock.getAlarmList().set(alarmId, alarmClock);
                } else AlarmClock.getAlarmList().add(alarmClock);
            } else if (alarmId >= 0) { //add
                alarmClock.setAlarmId(alarmId);
                AlarmClock.getAlarmList().set(alarmId, alarmClock);
            }

            ContentValues contentValues = new ContentValues();
            //DBHelper.putAlarmValue(getContext(), contentValues, descString, ringtoneURI , active, daysArray, hour, minute);
            DBHelper.putAlarmValue(getContext(), contentValues, alarmClock);
            if (alarmId >= 0) {
                //update record in DB
                int alarmIdDB = alarmId + 1;
                String strAlarmIdDb = String.valueOf(alarmIdDB);
                //DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[]{strAlarmIdDb});
            } else {
                //Add record to DB
                DBHelper.getDatabase(getContext()).insert(DBHelper.TABLE_ALARMS, null, contentValues);
            }
            new CreateAlarmTask().execute();
            //AlarmClock.createAlarm(getContext(), getAlarmMgr(), AlarmClock.getAlarmList(), false);
            if (alarmClock.isActive()) showAlarmTimeToast(alarmClock);
        }
        AlarmClock.getAlarmArrayMap(); //update data that set to adapter
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
        this.position = position;

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
        menu.add(0, 0, 0, getContext().getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int alarmIdDB = acmi.position + 1;
            String strAlarmIdDb = String.valueOf(alarmIdDB);
            //DBHelper.getDatabase(getContext()).delete(DBHelper.TABLE_ALARMS, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
            DBHelper.getDatabase(getContext()).delete(DBHelper.TABLE_ALARMS, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[]{strAlarmIdDb});

            AlarmClock.getAlarmList().remove(acmi.position);
            AlarmClock.recreateAlarmListId();
            AlarmClock.getAlarmArrayMap();
            adapter.notifyDataSetChanged();

            for (int i = alarmIdDB; i <= AlarmClock.getAlarmList().size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_ID_ALARM_UPDATE, i);
                String idDb = String.valueOf(i + 1);
                DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[]{idDb});
            }
            //AlarmClock.createAlarm(getContext(), getAlarmMgr(),AlarmClock.getAlarmList(), false);
            new CreateAlarmTask().execute();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showAlarmTimeToast(AlarmClock clock) {
        long diff = clock.getAlarmDiffInMillis();
        LocalDateTime localAlarmTime = new LocalDateTime(diff, DateTimeZone.UTC);

        LocalDateTime alarmTime = new LocalDateTime(clock.getAlarmTimeInMillis(), DateTimeZone.UTC);
        LocalDateTime now = new LocalDateTime(DateTimeZone.UTC);
        Duration duration = new Duration(now.toDateTime(DateTimeZone.UTC), alarmTime.toDateTime(DateTimeZone.UTC));

        String toastString = getContext().getString(R.string.toast_text1);
        boolean day = false;
        if (duration.getStandardDays() > 0) {
            toastString += " " + duration.getStandardDays();
            toastString += " " + getContext().getString(R.string.toast_text_day) + " ";
            day = true;
        }
        if (localAlarmTime.getHourOfDay() > 0) {
            if (!day) toastString += " ";
            toastString += +localAlarmTime.getHourOfDay();
            toastString += " " + getContext().getString(R.string.toast_text_hour) + " ";
        }
        if (localAlarmTime.getMinuteOfHour() > 0) {
            toastString += localAlarmTime.getMinuteOfHour();
            toastString += " " + getContext().getString(R.string.toast_text_minute) + " ";
        }
        if (localAlarmTime.getHourOfDay() <= 0 && localAlarmTime.getMinuteOfHour() <= 0)
            toastString += getContext().getString(R.string.toast_text_less_minute);
        toastString += " " + getContext().getString(R.string.toast_text2) + " ";

        Toast.makeText(getContext(), toastString, Toast.LENGTH_LONG).show();
    }

    private void alarmListSetAdapter() {
        adapter = new SimpleAdapter(getContext(), AlarmClock.getAlarmArrayMap(), R.layout.alarmlist_item, AlarmClock.getFrom(), to) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
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


    private class MyViewBinder implements SimpleAdapter.ViewBinder, CompoundButton.OnCheckedChangeListener {
        boolean[] alarmDays;
        boolean active;
        String description = "";
        int hour;
        int minute;
        int position = 0;

        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            switch (view.getId()) {
                case R.id.textTime:
                    int[] timeArray = (int[]) data;
                    hour = timeArray[0];
                    minute = timeArray[1];
                    position = timeArray[2];
                    String strHour = String.valueOf(hour);
                    String strMinute = String.valueOf(minute);
                    if (minute < 10) strMinute = "0" + minute;
                    if (hour < 10) strHour = "0" + hour;
                    ((TextView) view).setText(strHour + ":" + strMinute);
                    return true;
                case R.id.textMn:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[0])
                        ((TextView) view).setText(getContext().getString(R.string.mn));
                    return true;
                case R.id.textTs:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[1])
                        ((TextView) view).setText(getContext().getString(R.string.ts));
                    return true;
                case R.id.textWd:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[2])
                        ((TextView) view).setText(getContext().getString(R.string.wd));
                    return true;
                case R.id.textTh:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[3])
                        ((TextView) view).setText(getContext().getString(R.string.th));
                    return true;
                case R.id.textFr:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[4])
                        ((TextView) view).setText(getContext().getString(R.string.fr));
                    return true;
                case R.id.textSt:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[5])
                        ((TextView) view).setText(getContext().getString(R.string.st));
                    return true;
                case R.id.textSn:
                    alarmDays = (boolean[]) data;
                    ((TextView) view).setText("");
                    if (alarmDays[6])
                        ((TextView) view).setText(getContext().getString(R.string.sn));
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
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int position = (int) buttonView.getTag();

            AlarmClock alarmClock = AlarmClock.getAlarmList().get(position);
            alarmClock.setActive(isChecked);//Update id

            ContentValues contentValues = new ContentValues();
            //DBHelper.putAlarmValue(getContext(), contentValues, alarmClock.getDescription(), alarmClock.getRingtoneURI(), isChecked, alarmClock.getAlarmDays(), alarmClock.getHour(), alarmClock.getMinute());
            DBHelper.putAlarmValue(getContext(), contentValues, alarmClock);
            int alarmIdDB = alarmClock.getAlarmId() + 1;
            String strAlarmIdDb = String.valueOf(alarmIdDB);
            //DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM + "=?", new String[] {strAlarmIdDb} );
            DBHelper.getDatabase(getContext()).update(DBHelper.TABLE_ALARMS, contentValues, DBHelper.KEY_ID_ALARM_UPDATE + "=?", new String[]{strAlarmIdDb});

            AlarmClock.getAlarmList().get(position).setActive(isChecked);
            AlarmClock.getAlarmArrayMap(); //update data that set to adapter

            new CreateAlarmTask().execute();

            if (alarmClock.isActive()) showAlarmTimeToast(alarmClock);
        }
    }
    private class CreateAlarmTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return AlarmClock.createAlarm(getContext(), getAlarmMgr(), AlarmClock.getAlarmList(), false);
        }
    }
}
