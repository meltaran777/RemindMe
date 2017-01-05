package org.bogdan.remindme.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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

import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.bogdan.remindme.R;

public class HappyBirthdayDialogActivity extends AppCompatActivity implements View.OnClickListener, TextSwitcher.ViewFactory, View.OnTouchListener {
    private static final float MIN_DISTANCE = 150;
    private static final int OPERATION_TAKE_NEXT = 1;
    private static final int OPERATION_TAKE_PREVIOUS = 0;
    private static final int OPERATION_TAKE_FIRST = 3;

    private Button btnClose;
    private  Button btnHappyBirthday;
    private  ImageButton btnPrevious;
    private  ImageButton btnNext;
    private  ImageView imageUserAvatar;
    private  TextView textUserName;
    private  TextSwitcher textSwitcher;
    private  Spinner messageCategorySpinner;

    private int messageCategoryIndex = 0;
    private int arrayCurrElemInd = 0;
    int userId;

    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 0;
    private float y2 = 0;

    String currentMessage = "";
    String[] friendsText;
    String[] darlingText;
    String[] favouriteText;
    String[] manText;
    String[] girlText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

        userId = getIntent().getIntExtra("userId",0);

        spinnerSetAdapter();
        textSwitcherSetAnimation();
        
        btnClose.setOnClickListener(this);
        btnHappyBirthday.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        friendsText = getResources().getStringArray(R.array.happyBirthdayFriendArray);
        darlingText = getResources().getStringArray(R.array.happyBirthdayDarlingArray);
        favouriteText = getResources().getStringArray(R.array.happyBirthdayFavouriteArray);
        girlText = getResources().getStringArray(R.array.happyBirthdayGirlArray);
        manText = getResources().getStringArray(R.array.happyBirthdayManArray);
        updateTextView(OPERATION_TAKE_FIRST);
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
                messageCategoryIndex = position;
                updateTextView(OPERATION_TAKE_FIRST);
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
                sendHappyBirthdayMessage();
                finish();
                break;
            case R.id.imageButtonPrevious:
                updateTextView(OPERATION_TAKE_PREVIOUS);
                break;
            case R.id.imageButtonNext:
                updateTextView(OPERATION_TAKE_NEXT);
                break;
        }
    }

    private void sendHappyBirthdayMessage() {
        VKRequest sendMassage = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, currentMessage));
        sendMassage.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.i("VKInfo", "onComplete: MessageSend");
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.e("VKError", "onError: "+error.toString());
            }
        });
    }

    @Override
    public View makeView() {
        TextView textView = new TextView(this);
        textView.setVerticalScrollBarEnabled(true);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER);
        textView.setTextSize(20);
        textView.setTextColor(Color.BLACK);
        return textView;
    }
    private void updateTextView(int operationType) {
        String selectedArray[];
        switch (messageCategoryIndex){
            case 0:
                selectedArray = friendsText;
                break;
            case 1:
                selectedArray = darlingText;
                break;
            case 2:
                selectedArray = favouriteText;
                break;
            case 3:
                selectedArray = manText;
                break;
            case 4:
                selectedArray = girlText;
                break;
            default:
                selectedArray = friendsText;
        }

        if (operationType == 0) arrayCurrElemInd--;
        if (operationType == 1) arrayCurrElemInd++;
        if (operationType == 3) arrayCurrElemInd = 0;

        if (arrayCurrElemInd > 4) arrayCurrElemInd--;
        if (arrayCurrElemInd < 0) arrayCurrElemInd++;

        currentMessage = selectedArray[arrayCurrElemInd];
        textSwitcher.setText(currentMessage);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
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
                if (x1 + MIN_DISTANCE < x2){// Toast.makeText(getApplicationContext(), "Left to Right Swap Performed", Toast.LENGTH_LONG).show();
                    updateTextView(OPERATION_TAKE_PREVIOUS);
                }
                // if right to left sweep event on screen(next)
                if (x1 > x2 + MIN_DISTANCE){// Toast.makeText(getApplicationContext(), "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
                    updateTextView(OPERATION_TAKE_NEXT);
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
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
