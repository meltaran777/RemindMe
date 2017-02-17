package org.bogdan.remindme.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.AddAlarmActivity;
import org.bogdan.remindme.content.AlarmClock;
import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bodia on 17.02.2017.
 */

public class AddAlarmFargmentDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private final static int SOUND_PICKER_RESULT_CODE = 1;
    private EditText editText;
    private TextView dayView;
    private TextView soundView;
    private Button btnRepeat;
    private Button btnCancel;
    private Button btnOK;
    private Button btnSound;
    private SwitchCompat switchCompat;
    private TimePicker timePicker;

    // Boolean array for selected items
    boolean[] checkedDay = new boolean[]{
            false, //M
            false, //T
            false, //W
            false, // Th
            false, //F
            false, //S
            false //San
    };

    int alarmId=-1;
    String ringtoneURI;
    String ringtoneTitle;

    static AddAlarmFargmentDialog newInstance(Intent intent) {

        AddAlarmFargmentDialog f = new AddAlarmFargmentDialog();

        if (intent != null) {

            boolean newAlarm = intent.getAction().equalsIgnoreCase(AlarmClockFragment.CREATE_ALARM_ACTION);
            int alarmId = intent.getIntExtra("alarmID", -1);
            boolean active = intent.getBooleanExtra("alarmActive", false);

            Bundle args = new Bundle();
            args.putBoolean("newAlarm", newAlarm);
            args.putBoolean("alarmActive", active);
            args.putInt("alarmID", alarmId);
            f.setArguments(args);
        }
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int style = AppCompatDialogFragment.STYLE_NO_TITLE;
        int theme = android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth;

        setStyle(style, theme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.add_alarm_layout, container, false);

        ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        ringtoneTitle = RingtoneManager.getRingtone(getContext(), Uri.parse(ringtoneURI)).getTitle(getContext());

        editText = (EditText) v.findViewById(R.id.textViewDescription);
        dayView = (TextView)   v.findViewById(R.id.textRepeatDays);
        soundView = (TextView) v.findViewById(R.id.textSound);
        btnRepeat = (Button) v.findViewById(R.id.btn_repeat);
        btnCancel = (Button) v.findViewById(R.id.btn_cancel);
        btnOK = (Button)     v.findViewById(R.id.btn_ok);
        btnSound = (Button) v.findViewById(R.id.btn_sound);
        switchCompat = (SwitchCompat) v.findViewById(R.id.switch_on_off);
        timePicker = (TimePicker) v.findViewById(R.id.timePickerAddAlarm);

        LocalTime localTime = new LocalTime();
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(localTime.getHourOfDay());

        btnRepeat.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        btnSound.setOnClickListener(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {

            if(arguments.getBoolean("newAlarm"))
                switchCompat.setEnabled(false);
            else
                switchCompat.setEnabled(true);

            alarmId = arguments.getInt("alarmID", -1);
            boolean active = arguments.getBoolean("alarmActive",false);
            if (alarmId >= 0) {
                int hour = AlarmClock.getAlarmList().get(alarmId).getHour();
                switchCompat.setChecked(active);
                editText.setText(AlarmClock.getAlarmList().get(alarmId).getDescription());
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(AlarmClock.getAlarmList().get(alarmId).getMinute());
                checkedDay = AlarmClock.getAlarmList().get(alarmId).getAlarmDays();
                ringtoneURI = AlarmClock.getAlarmList().get(alarmId).getRingtoneURI();
            }
            textViewSetText();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){

            case R.id.btn_ok:
                String descText = editText.getText().toString();
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                boolean active = switchCompat.isChecked();

                intent = new Intent();
                intent.putExtra("arrayDayOfWeek",checkedDay);
                intent.putExtra("hour",hour);
                intent.putExtra("minute",minute);
                intent.putExtra("descText",descText);
                intent.putExtra("alarmId",alarmId);
                intent.putExtra("active",active);
                intent.putExtra("ringtone",ringtoneURI);

                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                getDialog().dismiss();
                break;

            case R.id.btn_cancel:
                intent = new Intent();
                intent.putExtra("alarmId",alarmId);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, intent);
                getDialog().dismiss();
                break;

            case R.id.btn_repeat:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setIcon(R.drawable.ic_inf);
                builder.setTitle("Repeat");
                builder.setCancelable(false);
                String[] day = new String[]{
                        "Monday",
                        "Tuesday",
                        "Wednesday",
                        "Thursday",
                        "Friday",
                        "Saturday",
                        "Sunday"
                };
                // Convert the day array to list
                final List<String> daysList = Arrays.asList(day);

                builder.setMultiChoiceItems(day, checkedDay, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // Update the current focused item's checked status
                        checkedDay[which] = isChecked;
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textViewSetText();
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.btn_sound:
                Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM);
                if (ringtoneURI != null) currentTone = Uri.parse(ringtoneURI);

                Intent soundPickerIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                soundPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_RINGTONE);
                soundPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                soundPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                soundPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                soundPickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);

                startActivityForResult(soundPickerIntent, SOUND_PICKER_RESULT_CODE);
                break;
        }
    }

    private void textViewSetText(){
        ringtoneTitle = RingtoneManager.getRingtone(getContext(),Uri.parse(ringtoneURI)).getTitle(getContext());
        int substringSize;
        if (ringtoneTitle.length() <= 16) substringSize = ringtoneTitle.length()-1;
        else substringSize = 16;
        soundView.setHint(ringtoneTitle.substring(0, substringSize));

        dayView.setHint("");
        for(int i=0;i<checkedDay.length;i++){
            boolean day=checkedDay[i];
            if(day == true) switch (i){
                case 0:
                    dayView.setHint(dayView.getHint()+"Mn ");
                    break;
                case 1:
                    dayView.setHint(dayView.getHint()+"Ts ");
                    break;
                case 2:
                    dayView.setHint(dayView.getHint()+"Wd ");
                    break;
                case 3:
                    dayView.setHint(dayView.getHint()+"Th ");
                    break;
                case 4:
                    dayView.setHint(dayView.getHint()+"Fr ");
                    break;
                case 5:
                    dayView.setHint(dayView.getHint()+"St ");
                    break;
                case 6:
                    dayView.setHint(dayView.getHint()+"Sn");
                    break;
            }
        }
    }
}
