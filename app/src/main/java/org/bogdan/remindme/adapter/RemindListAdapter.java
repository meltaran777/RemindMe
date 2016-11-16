package org.bogdan.remindme.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.bogdan.remindme.R;
import org.bogdan.remindme.UserVK;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 30.10.2016.
 */
public class RemindListAdapter extends RecyclerView.Adapter<RemindListAdapter.RemindViewHollder> {

    private static final int IMG_WIDTH = 150;
    private static final int IMG_HIGH = 150;

    private View view;

    private List<UserVK> data=new ArrayList<>();

    public RemindListAdapter(List<UserVK> data) {
        this.data = data;
    }

    @Override
    public RemindViewHollder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item,parent,false);
        return  new RemindViewHollder(view);
    }

    @Override
    public void onBindViewHolder(RemindViewHollder holder, int position) {

        if(!data.isEmpty()) {
            if(data.get(position).getAvatarURL()!=null){
                //new DownloadImageTask(holder.imageView).execute(data.get(position).getAvatarURL());
                Picasso.with(view.getContext()).load(data.get(position).getAvatarURL()).resize(IMG_WIDTH, IMG_HIGH).into(holder.imageView);
            }

            if(data.get(position).getDateFormat()!=null){
                DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(data.get(position).getDateFormat());
                holder.title.setText(data.get(position).getName()+"\n"+"birht date: "+data.get(position).getBirthDate().toString(dateTimeFormat)+
                        "\n"+"next birht: "+UserVK.getTimeToNextBirht(data.get(position)));
            }

        }
    }



    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public static class RemindViewHollder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView title;
        ImageView imageView;
        CheckBox checkBoxVIP;
        CheckBox checkBoxCongratulations;
        public RemindViewHollder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.CardView);
            title = (TextView) itemView.findViewById(R.id.TextViewTitle);
            imageView = (ImageView) itemView.findViewById(R.id.imageViewAvatarVK);
            checkBoxVIP = (CheckBox) itemView.findViewById(R.id.checkboxVIP);
            checkBoxCongratulations = (CheckBox) itemView.findViewById(R.id.checkboxCongratulations);
        }
    }
}
