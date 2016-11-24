package org.bogdan.remindme.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.AlarmClock;

import java.util.Arrays;
import java.util.List;

public class AddAlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText;
    private Button btnRepeat;
    private Button btnCancel;
    private Button btnOK;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        editText = (EditText) findViewById(R.id.textViewDescription);
        btnRepeat = (Button) findViewById(R.id.btn_repeat);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnOK = (Button) findViewById(R.id.btn_ok);
        timePicker = (TimePicker) findViewById(R.id.timePickerAddAlarm);

        timePicker.setIs24HourView(true);

        btnRepeat.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent != null) {
            alarmId = intent.getIntExtra("alarmID", -1);
            if (alarmId >= 0) {
                editText.setText(AlarmClock.getAlarmList().get(alarmId).getDescription());
                timePicker.setCurrentHour(AlarmClock.getAlarmList().get(alarmId).getHour());
                timePicker.setCurrentMinute(AlarmClock.getAlarmList().get(alarmId).getMinute());
                checkedDay = AlarmClock.getAlarmList().get(alarmId).getAlarmDays();
                btnRepeatSetText();
            }
        }
    }




    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_ok:
                String descText = editText.getText().toString();
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                Intent intent = new Intent();
                intent.putExtra("arrayDayOfWeek",checkedDay);
                intent.putExtra("hour",hour);
                intent.putExtra("minute",minute);
                intent.putExtra("descText",descText);
                intent.putExtra("alarmId",alarmId);
                setResult(Activity.RESULT_OK,intent);

                finish();
                break;
            case R.id.btn_cancel:
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
                        btnRepeatSetText();
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

    }

    private void btnRepeatSetText(){
        btnRepeat.setText("");
        for(int i=0;i<checkedDay.length;i++){
            boolean day=checkedDay[i];
            if(day == true) switch (i){
                case 0:
                    btnRepeat.setText(btnRepeat.getText()+"Mn ");
                    break;
                case 1:
                    btnRepeat.setText(btnRepeat.getText()+"Ts ");
                    break;
                case 2:
                    btnRepeat.setText(btnRepeat.getText()+"Wd ");
                    break;
                case 3:
                    btnRepeat.setText(btnRepeat.getText()+"Th ");
                    break;
                case 4:
                    btnRepeat.setText(btnRepeat.getText()+"Fr ");
                    break;
                case 5:
                    btnRepeat.setText(btnRepeat.getText()+"St ");
                    break;
                case 6:
                    btnRepeat.setText(btnRepeat.getText()+"Sn");
                    break;
            }
        }
    }
}
