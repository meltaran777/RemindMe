package org.bogdan.remindme.adapter;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.HappyBirthdayDialogActivity;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.util.NotificationPublisher;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bodia on 30.10.2016.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.RemindViewHollder>{

    private static final int IMG_WIDTH = 150;
    private static final int IMG_HIGH = 150;

    private View view;

    private List<UserVK> data = new ArrayList<>();

    public UserListAdapter(List<UserVK> data) {
        this.data = data;
    }

    @Override
    public RemindViewHollder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_item,parent,false);

        return  new RemindViewHollder(view);
    }

    @Override
    public void onBindViewHolder(RemindViewHollder holder, final int position) {

        if(!data.isEmpty()) {

            if(data.get(position).getAvatarURL()!=null){

                Picasso.with(view.getContext()).load(data.get(position).getAvatarURL()).resize(IMG_WIDTH, IMG_HIGH).into(holder.imageBtnAvatar);
            }

            if(data.get(position).getDateFormat()!=null){

                DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(data.get(position).getDateFormat());
                holder.tvName.setText(data.get(position).getName());
                holder.tvBdate.setText(view.getContext().getString(R.string.str_birthday_text)+data.get(position).getBirthDate().toString(dateTimeFormat));
                holder.tvTimeToBdate.setText(view.getContext().getString(R.string.str_next_birth)+data.get(position).getTimeToNextBirht());
            }

            holder.imageBtnAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String userName = data.get(position).getName();
                    String userAvatarURL = data.get(position).getAvatarURL();
                    int userId = data.get(position).getId();

                    Intent happyBirthdayDialogIntent = new Intent(view.getContext(), HappyBirthdayDialogActivity.class);
                    happyBirthdayDialogIntent.putExtra("userId", userId);
                    happyBirthdayDialogIntent.putExtra("userName", userName);
                    happyBirthdayDialogIntent.putExtra("userAvatarURL", userAvatarURL);

                    view.getContext().startActivity(happyBirthdayDialogIntent);
                }
            });

            holder.checkBoxVIP.setOnCheckedChangeListener(null);
            holder.checkBoxVIP.setChecked(data.get(position).isNotify());
            holder.checkBoxVIP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    UserVK userVK = data.get(position);
                    userVK.setNotify(isChecked);

                    ContentValues contentValues = new ContentValues();
                    DBHelper.putUserValue(view.getContext(), userVK, contentValues);
                    DBHelper.getDatabase(view.getContext()).update(DBHelper.TABLE_USERS, contentValues, DBHelper.KEY_NAME+ "=?", new String[] {userVK.getName()} );

                    data.get(position).setNotify(isChecked);

                    if ((data.get(position).getDayToNextBirht() -  data.get(0).getDayToNextBirht()) < 3){

                        List<UserVK> userVKListFull = UserVK.getUserVKListFull(data);
                        Collections.sort(userVKListFull);
                        UserVK user = userVKListFull.get(0);

                        NotificationPublisher.scheduleNotification(view.getContext(), user);
                    }
                    //Toast.makeText(view.getContext(), String.valueOf(data.get(position).getId()), Toast.LENGTH_SHORT).show();
                    //Test Notification
                    //NotificationPublisher.testScheduleNotification(view.getContext(), userVK);
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public static class RemindViewHollder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView tvName;
        TextView tvBdate;
        TextView tvTimeToBdate;
        ImageButton imageBtnAvatar;
        CheckBox checkBoxVIP;

        public RemindViewHollder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.CardView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvBdate = (TextView) itemView.findViewById(R.id.tvBdate);
            tvTimeToBdate = (TextView) itemView.findViewById(R.id.tvTimeToBdate);
            imageBtnAvatar = (ImageButton) itemView.findViewById(R.id.imageViewAvatarVK);
            checkBoxVIP = (CheckBox) itemView.findViewById(R.id.checkboxVIP);

        }
    }
}
