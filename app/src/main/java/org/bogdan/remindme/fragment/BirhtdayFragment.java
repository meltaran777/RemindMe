package org.bogdan.remindme.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;
import com.vk.sdk.util.VKUtil;

import org.bogdan.remindme.R;
import org.bogdan.remindme.activities.HappyBirthdayDialogActivity;
import org.bogdan.remindme.activities.MainActivity;
import org.bogdan.remindme.content.UserVK;
import org.bogdan.remindme.adapter.UserListAdapter;
import org.bogdan.remindme.database.DBHelper;
import org.bogdan.remindme.util.NotificationPublisher;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by Bodia on 28.10.2016.
 */
public class BirhtdayFragment extends AbstractTabFragment{
    private static final int LAYOUT=R.layout.birthday_fragment_layout;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
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
        tvError = (TextView) view.findViewById(R.id.tvError);

        adapter = new UserListAdapter(UserVK.getUsersList());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(UserVK.getUsersList().isEmpty())
            tvError.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        DBHelper.closeDB();
        super.onDestroyView();
    }

    public void setContext(Context context) {
        this.context = context;

    }
    public UserListAdapter getAdapter() {
        return adapter;
    }
    public TextView getTvError() {
        return tvError;
    }
}
