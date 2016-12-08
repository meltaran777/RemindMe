package org.bogdan.remindme.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.fragment.AlarmClockFragment;
import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.List;

public class AddAlarmActivity extends AppCompatActivity implements View.OnClickListener {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_add_alarm);

        ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        ringtoneTitle = RingtoneManager.getRingtone(this,Uri.parse(ringtoneURI)).getTitle(this);

        editText = (EditText) findViewById(R.id.textViewDescription);
        dayView = (TextView) findViewById(R.id.textRepeatDays);
        soundView = (TextView) findViewById(R.id.textSound);
        btnRepeat = (Button) findViewById(R.id.btn_repeat);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnOK = (Button) findViewById(R.id.btn_ok);
        btnSound = (Button) findViewById(R.id.btn_sound);
        switchCompat = (SwitchCompat) findViewById(R.id.switch_on_off);
        timePicker = (TimePicker) findViewById(R.id.timePickerAddAlarm);

        LocalTime localTime = new LocalTime();
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(localTime.getHourOfDay());



        btnRepeat.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        btnSound.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null) {
            if(intent.getAction().equalsIgnoreCase(AlarmClockFragment.CREATE_ALARM_ACTION)) switchCompat.setEnabled(false);
            else switchCompat.setEnabled(true);

            alarmId = intent.getIntExtra("alarmID", -1);
            boolean active = intent.getBooleanExtra("alarmActive",false);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null){
                ringtoneURI = uri.toString();
            }
            else ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        }
        textViewSetText();
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
                setResult(Activity.RESULT_OK,intent);

                finish();
                break;
            case R.id.btn_cancel:
                intent = new Intent();
                intent.putExtra("alarmId",alarmId);
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_repeat:
                AlertDialog.Builder builder = new AlertDialog.Builder(AddAlarmActivity.this);
                builder.setIcon(R.drawable.ic_inf);
                builder.setTitle("Select Day of Week");
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
                Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
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
        ringtoneTitle = RingtoneManager.getRingtone(this,Uri.parse(ringtoneURI)).getTitle(this);
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
