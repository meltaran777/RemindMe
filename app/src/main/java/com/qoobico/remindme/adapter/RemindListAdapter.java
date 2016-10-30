package com.qoobico.remindme.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qoobico.remindme.R;
import com.qoobico.remindme.dto.RemindDTO;

import java.util.List;

/**
 * Created by Bodia on 30.10.2016.
 */
public class RemindListAdapter extends RecyclerView.Adapter<RemindListAdapter.RemindViewHollder> {

    private List<RemindDTO> data;

    public RemindListAdapter(List<RemindDTO> data) {
        this.data = data;
    }

    @Override
    public RemindViewHollder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item,parent,false);
        return  new RemindViewHollder(view);
    }

    @Override
    public void onBindViewHolder(RemindViewHollder holder, int position) {
        holder.title.setText(data.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class RemindViewHollder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView title;
        public RemindViewHollder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.CardView);
            title = (TextView) itemView.findViewById(R.id.TextViewTitle);
        }
    }
}
