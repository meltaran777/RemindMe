package org.bogdan.remindme.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.bogdan.remindme.R;
import org.bogdan.remindme.UserVK;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 30.10.2016.
 */
public class RemindListAdapter extends RecyclerView.Adapter<RemindListAdapter.RemindViewHollder> {

    private List<UserVK> data=new ArrayList<>();


    public RemindListAdapter(List<UserVK> data) {
        this.data = data;
    }

    @Override
    public RemindViewHollder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item,parent,false);
        return  new RemindViewHollder(view);
    }

    @Override
    public void onBindViewHolder(RemindViewHollder holder, int position) {

        if(data !=null) {
            if(data.get(position).getAvatarURL()!=null){
                new DownloadImageTask(holder.imageView).execute(data.get(position).getAvatarURL());
            }

            if(data.get(position).getDateFormat()!=null){
                DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(data.get(position).getDateFormat());
                holder.title.setText(data.get(position).getName()+" "+data.get(position).getBirthDate().toString(dateTimeFormat)+
                        "\n"+"next birht: "+UserVK.getTimeToNextBirht(data.get(position))+
                        "\n"+"in day:"+UserVK.getDayToNextBirht(data.get(position).getBirthDate()));
            }else holder.title.setText(data.get(position).getName()+
                    "\n"+"next birht: "+UserVK.getTimeToNextBirht(data.get(position)));
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
