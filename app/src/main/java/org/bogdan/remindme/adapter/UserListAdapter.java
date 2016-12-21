package org.bogdan.remindme.adapter;

import android.content.ContentValues;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.AlarmClock;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.util.NotificationPublisher;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 30.10.2016.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.RemindViewHollder> {

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
                Picasso.with(view.getContext()).load(data.get(position).getAvatarURL()).resize(IMG_WIDTH, IMG_HIGH).into(holder.imageView);
            }

            if(data.get(position).getDateFormat()!=null){
                DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(data.get(position).getDateFormat());
                holder.tvName.setText(data.get(position).getName());
                holder.tvBdate.setText(view.getContext().getString(R.string.str_birthday_text)+data.get(position).getBirthDate().toString(dateTimeFormat));
                holder.tvTimeToBdate.setText(view.getContext().getString(R.string.str_next_birth)+UserVK.getTimeToNextBirht(data.get(position)));
            }

            holder.checkBoxVIP.setOnCheckedChangeListener(null);
            holder.checkBoxVIP.setChecked(data.get(position).isNotify());
            holder.checkBoxVIP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if ((data.get(position).getDayToNextBirht() -  data.get(0).getDayToNextBirht()) < 3){
                        NotificationPublisher.scheduleNotification(view.getContext(),0,data.get(position));
                    }
                    UserVK userVK = data.get(position);
                    userVK.setNotify(isChecked);
                    ContentValues contentValues = new ContentValues();
                    DBHelper.putUserValue(view.getContext(), userVK, contentValues);

                    int userVKIdDB = position + 1;
                    String strUserVKIdDB = String.valueOf(userVKIdDB);
                    DBHelper.getDatabase(view.getContext()).update(DBHelper.TABLE_USERS, contentValues, DBHelper.KEY_ID+ "=?", new String[] {strUserVKIdDB} );

                    data.get(position).setNotify(isChecked);

                    Toast.makeText(view.getContext(), data.get(position).getName()+" "+position, Toast.LENGTH_SHORT).show();
                    //Test Notification
                   // NotificationPublisher.scheduleNotification(view.getContext(), 0, position, data.get(position),
                   //         data.get(position).getAvatarURL(), data.get(position).isNotify());
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
        ImageView imageView;
        CheckBox checkBoxVIP;

        public RemindViewHollder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.CardView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvBdate = (TextView) itemView.findViewById(R.id.tvBdate);
            tvTimeToBdate = (TextView) itemView.findViewById(R.id.tvTimeToBdate);
            imageView = (ImageView) itemView.findViewById(R.id.imageViewAvatarVK);
            checkBoxVIP = (CheckBox) itemView.findViewById(R.id.checkboxVIP);
        }
    }
}
