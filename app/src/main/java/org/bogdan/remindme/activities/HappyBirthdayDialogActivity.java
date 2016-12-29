package org.bogdan.remindme.activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.bogdan.remindme.R;

public class HappyBirthdayDialogActivity extends AppCompatActivity implements View.OnClickListener, TextSwitcher.ViewFactory, View.OnTouchListener {
    private Button btnClose;
    private  Button btnHappyBirthday;
    private  ImageButton btnPrevious;
    private  ImageButton btnNext;
    private  ImageView imageUserAvatar;
    private  TextView textUserName;
    private  TextSwitcher textSwitcher;
    private  Spinner messageCategorySpinner;

    private int messageCategory = 0;
    private int mCounter = 0;

    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 0;
    private float y2 = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_happy_birthday_dialog_layout);

        btnClose = (Button) findViewById(R.id.btn_close);
        btnHappyBirthday = (Button) findViewById(R.id.btn_happy_birthday);
        btnPrevious = (ImageButton) findViewById(R.id.imageButtonPrevious);
        btnNext = (ImageButton) findViewById(R.id.imageButtonNext);
        imageUserAvatar = (ImageView) findViewById(R.id.imageViewAvatar);
        textUserName = (TextView) findViewById(R.id.textViewName);
        textSwitcher = (TextSwitcher) findViewById(R.id.textHappyBirthdaySwitcher);
        messageCategorySpinner = (Spinner) findViewById(R.id.spinnerMessageCategory);

        String avatarUrl = getIntent().getStringExtra("userAvatarURL");
        Picasso.with(getApplicationContext()).load(avatarUrl).into(imageUserAvatar);

        String userName = getIntent().getStringExtra("userName");
        textUserName.setText(textUserName.getText()+" "+userName);

        spinnerSetAdapter();
        textSwitcherSetAnimation();
        
        btnClose.setOnClickListener(this);
        btnHappyBirthday.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        updateCounter();
    }

    private void textSwitcherSetAnimation() {
        textSwitcher.setFactory(this);

        Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        textSwitcher.setInAnimation(inAnimation);
        textSwitcher.setOutAnimation(outAnimation);

        textSwitcher.setOnTouchListener(this);
    }

    private void spinnerSetAdapter() {
        String[] messageCategoryName = getResources().getStringArray(R.array.messageCategoryArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, messageCategoryName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        messageCategorySpinner.setAdapter(adapter);
        messageCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                messageCategory = position;
                //Toast.makeText(getApplicationContext(), messageCategorySpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_happy_birthday:

                finish();
                break;
            case R.id.imageButtonPrevious:
                mCounter--;
                updateCounter();
                break;
            case R.id.imageButtonNext:
                mCounter++;
                updateCounter();
                break;
        }
    }

    @Override
    public View makeView() {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(70);
        textView.setTextColor(Color.RED);
        return textView;
    }
    private void updateCounter() {
        textSwitcher.setText(String.valueOf(mCounter));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction())
        {// when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN:
            {
                x1 = event.getX();
                y1 = event.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = event.getX();
                y2 = event.getY();
                //if left to right sweep event on screen(back)
                if (x1 < x2){// Toast.makeText(getApplicationContext(), "Left to Right Swap Performed", Toast.LENGTH_LONG).show();
                    mCounter--;
                    updateCounter();
                }
                // if right to left sweep event on screen(next)
                if (x1 > x2){//   Toast.makeText(getApplicationContext(), "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
                    mCounter++;
                    updateCounter();
                }
                // if UP to Down sweep event on screen
                if (y1 < y2){// Toast.makeText(getApplicationContext(), "UP to Down Swap Performed", Toast.LENGTH_LONG).show();
                }
                //if Down to UP sweep event on screen
                if (y1 > y2){// Toast.makeText(getApplicationContext(), "Down to UP Swap Performed", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
        return true;
    }
}
