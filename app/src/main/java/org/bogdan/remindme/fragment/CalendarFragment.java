package org.bogdan.remindme.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.squareup.picasso.Picasso;

import org.bogdan.remindme.R;
import org.bogdan.remindme.content.UserVK;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Bodia on 28.10.2016.
 */
public class CalendarFragment extends AbstractTabFragment{
    private static final int LAYOUT=R.layout.calendar_fragment_layout;

    private static final int IMG_WIDTH = 150;
    private static final int IMG_HIGHT = 150;
    private static final int DOT_RADIUS = 13;

    private static String title;

    public static CalendarFragment getInstance(Context context){
        Bundle args=new Bundle();
        CalendarFragment fragment=new CalendarFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.tab_item_Calendar));

        return  fragment;
    }

    private MaterialCalendarView materialCalendarView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);

        materialCalendarView = (MaterialCalendarView) view.findViewById(R.id.materialCalendarView);

        addDecorator();
        addOnDateChangedListener();

        return view;
    }

    private void addOnDateChangedListener() {
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                int year = date.getYear();
                int month = date.getMonth() +1;
                int day = date.getDay();

                LocalDate selectedDay = new LocalDate().withDayOfMonth(day).withMonthOfYear(month).withYear(year);

                if (selectedDay.equals(new LocalDate()))
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.today), Toast.LENGTH_SHORT).show();

                for(UserVK userVK : UserVK.getUsersList()){

                    LocalDate bdate = UserVK.getNextBirthDate(userVK.getBirthDate());

                    if(bdate.equals(selectedDay)){

                        ImageView image = new ImageView(view.getContext());
                        Picasso.with(view.getContext()).load(userVK.getAvatarURL()).resize(IMG_WIDTH,IMG_HIGHT).into(image);

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(view.getContext())
                                        .setMessage(getContext().getString(R.string.birthday_big)+userVK.getName())
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setView(image);
                        builder.create().show();
                    }
                }
            }
        });
    }

    private void addDecorator() {
        List<CalendarDay> calendarDays = new ArrayList<CalendarDay>();
        Calendar calendar = Calendar.getInstance();

        List<CalendarDay> today = new ArrayList<CalendarDay>();
        CalendarDay todayDay = CalendarDay.from(calendar);
        today.add(todayDay);

        for (UserVK userVK : UserVK.getUsersList()) {
            LocalDate date = UserVK.getNextBirthDate(userVK.getBirthDate());
            // might be a more elegant way to do this part, but this is very explicit
            int year = date.getYear();
            int month = date.getMonthOfYear() - 1; // months are 0-based in Calendar
            int day = date.getDayOfMonth();

            calendar.set(year, month, day);
            CalendarDay calendarDay = CalendarDay.from(calendar);
            calendarDays.add(calendarDay);
        }
        materialCalendarView.addDecorators(new EventDecorator(Color.RED, calendarDays));
        materialCalendarView.addDecorators(new EventDecorator(Color.GREEN, today));
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private class EventDecorator implements DayViewDecorator {

        private int color;
        private HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(DOT_RADIUS, color));
        }
    }
}
