package org.bogdan.remindme.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.bogdan.remindme.MainActivity;
import org.bogdan.remindme.R;

/**
 * Created by Bodia on 28.10.2016.
 */
public class ExampleFragment extends Fragment{
    private static final int LAYOUT=R.layout.fragment_example_layout;
    private View view;

    public static ExampleFragment getInstance(){
        Bundle args=new Bundle();
        ExampleFragment fragment=new ExampleFragment();
        fragment.setArguments(args);

        return  fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        return view;

    }
}
