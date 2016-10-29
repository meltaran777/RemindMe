package com.qoobico.remindme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qoobico.remindme.R;

/**
 * Created by Bodia on 28.10.2016.
 */
public class IdeasFragment extends AbstractTabFragment{
    private static final int LAYOUT=R.layout.fragment_example_layout;

    private static String title;

    public static IdeasFragment getInstance(Context context){
        Bundle args=new Bundle();
        IdeasFragment fragment=new IdeasFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_item_Ideas));


        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        return view;

    }

    public void setContext(Context context) {
        this.context = context;
    }

}
