package org.bogdan.remindme.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.bogdan.remindme.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText;
    private Button btnRepeat;
    private Button btnCancel;
    private Button btnOK;
    private TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        editText = (EditText) findViewById(R.id.textViewDescription);
        btnRepeat = (Button) findViewById(R.id.btn_repeat);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnOK = (Button) findViewById(R.id.btn_ok);
        timePicker = (TimePicker) findViewById(R.id.timePickerAddAlarm);

        btnRepeat.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);
    }



    // Boolean array for selected items
    final boolean[] checkedDay = new boolean[]{
            false, //M
            false, //T
            false, //W
            false, // Th
            false, //F
            false, //S
            false //San
    };
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
                setResult(Activity.RESULT_OK,intent);

                finish();
                break;
            case R.id.btn_cancel:

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

                // Convert the color array to list
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
                        btnRepeat.setHint("");
                        for(int i=0;i<checkedDay.length;i++){
                            boolean day=checkedDay[i];
                            if(day == true) switch (i){
                                case 0:
                                    btnRepeat.setHint(btnRepeat.getHint()+"Mn ");
                                    break;
                                case 1:
                                    btnRepeat.setHint(btnRepeat.getHint()+"Ts ");
                                    break;
                                case 2:
                                    btnRepeat.setHint(btnRepeat.getHint()+"Wd ");
                                    break;
                                case 3:
                                    btnRepeat.setHint(btnRepeat.getHint()+"Th ");
                                    break;
                                case 4:
                                    btnRepeat.setHint(btnRepeat.getHint()+"Fr ");
                                    break;
                                case 5:
                                    btnRepeat.setHint(btnRepeat.getHint()+"St ");
                                    break;
                                case 6:
                                    btnRepeat.setHint(btnRepeat.getHint()+"Sn");
                                    break;
                            }
                        }
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

    }
}
