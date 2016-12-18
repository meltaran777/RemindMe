package org.bogdan.remindme.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.bogdan.remindme.fragment.AbstractTabFragment;
import org.bogdan.remindme.fragment.BirhtdayFragment;
import org.bogdan.remindme.fragment.CalendarFragment;
import org.bogdan.remindme.fragment.AlarmClockFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bodia on 28.10.2016.
 */
public class TabsFragmentAdapter extends FragmentPagerAdapter {
    private Context context;
    private Map<Integer,AbstractTabFragment> tabsMap;

    public TabsFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        initTabsMap();
    }

    @Override
    public Fragment getItem(int position) {
        return tabsMap.get(position);
    }

    @Override
    public int getCount() {
        return tabsMap.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabsMap.get(position).getTitle();
    }


    private void initTabsMap() {
        tabsMap = new HashMap<>();
        tabsMap.put(0, AlarmClockFragment.getInstance(context));
        tabsMap.put(1, BirhtdayFragment.getInstance(context));
        tabsMap.put(2, CalendarFragment.getInstance(context));
    }
}
