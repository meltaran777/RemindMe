package org.bogdan.remindme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKSdk;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.adapter.UserListAdapter;
import org.bogdan.remindme.content.UserVK;

/**
 * Created by Bodia on 28.10.2016.
 */
public class BirhtdayFragment extends AbstractTabFragment implements View.OnClickListener{

    private static final int LAYOUT=R.layout.birthday_fragment_layout;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button vkLoginBtn;
    private TextView textView;

    private UserListAdapter adapter;

    private TextView tvError;
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

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        textView = (TextView) view.findViewById(R.id.textVkLogin);
        vkLoginBtn = (Button) view.findViewById(R.id.vkLoginBtn);

        vkLoginBtn.setOnClickListener(this);

        adapter = new UserListAdapter(UserVK.getUsersList());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (VKSdk.isLoggedIn()){
            hideVkLogin();
            if (!((MainActivity)getActivity()).isInternetAvailable())
                progressBar.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public UserListAdapter getAdapter() {
        return adapter;
    }

    public void updateBirthdayFragmentUI() {
        getAdapter().notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    public void hideVkLogin(){
        vkLoginBtn.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }

    public void showVkLogin(){
        vkLoginBtn.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        ((MainActivity)getActivity()).vkLogin();
        hideVkLogin();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
