package org.bogdan.remindme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.bogdan.remindme.R;
import org.bogdan.remindme.adapter.RemindListAdapter;
import org.bogdan.remindme.dto.RemindDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 28.10.2016.
 */
public class HistoryFragment extends AbstractTabFragment{
    private static final int LAYOUT=R.layout.fragment_history;

    private static String title;

    public static HistoryFragment getInstance(Context context){
        Bundle args=new Bundle();
        HistoryFragment fragment=new HistoryFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_item_History));


        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new RemindListAdapter(createMockRemindListData()));
        return view;
    }

    private List<RemindDTO> createMockRemindListData() {
        List<RemindDTO> data=new ArrayList<>();
        data.add(new RemindDTO("Item 1"));
        data.add(new RemindDTO("Item 2"));
        data.add(new RemindDTO("Item 3"));
        data.add(new RemindDTO("Item 4"));
        data.add(new RemindDTO("Item 5"));
        data.add(new RemindDTO("Item 6"));
        data.add(new RemindDTO("Item 7"));

        return data;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
