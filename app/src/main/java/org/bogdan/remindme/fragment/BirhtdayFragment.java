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
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.adapter.UserListAdapter;

/**
 * Created by Bodia on 28.10.2016.
 */
public class BirhtdayFragment extends AbstractTabFragment{
    private static final int LAYOUT=R.layout.birthday_fragment_layout;

    private static String title;

    public static BirhtdayFragment getInstance(Context context){
        Bundle args=new Bundle();
        BirhtdayFragment fragment=new BirhtdayFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_item_Birthday));

        return  fragment;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);

        RecyclerView recyclerView;
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new UserListAdapter(UserVK.getUsersList()));

        return view;
    }





    public void setContext(Context context) {
        this.context = context;
    }


}
