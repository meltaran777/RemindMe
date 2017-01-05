package org.bogdan.remindme.content;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bodia on 05.11.2016.
 */

public class UserVK implements Comparable<UserVK> {

    private final String name;
    private int id;
    private DateTime birthDate;
    private final String dateFormat;
    private final String avatarURL;
    private boolean notify;

    static private ArrayList<UserVK> users=new ArrayList<>();

    public UserVK (int id, String name, DateTime birthDate, String dateFormat, String avatarURL, boolean notify) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.dateFormat = dateFormat;
        this.avatarURL =avatarURL;
        this.notify = notify;
    }

    public UserVK (UserVK userVK) {
        this.id = userVK.getId();
        this.name = userVK.getName();
        this.birthDate = userVK.getBirthDate();
        this.dateFormat = userVK.getDateFormat();
        this.avatarURL =userVK.getAvatarURL();
        this.notify = userVK.isNotify();
    }

    @Override
    public int compareTo(UserVK another) {
        Integer dayThis = getDayToNextBirht();
        Integer dayAnother = another.getDayToNextBirht();

        return dayThis.compareTo(dayAnother);
    }

    public String getTimeToNextBirht(){
        LocalDate dateOfBirth = getBirthDate().toLocalDate();
        LocalDate currentDate = new LocalDate();
        // Take birthDay  and birthMonth  from dateOfBirth
        int birthDay = dateOfBirth.getDayOfMonth();
        int birthMonth = dateOfBirth.getMonthOfYear();
        // Current year's birthday
        LocalDate currentYearBirthDay = new LocalDate().withDayOfMonth(birthDay)
                .withMonthOfYear(birthMonth);
        PeriodType monthDay = PeriodType.yearMonthDayTime().withYearsRemoved();
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                .appendMonths().appendSuffix(" Months ").appendDays()
                .appendSuffix(" Days ").printZeroNever().toFormatter();
        if (currentYearBirthDay.isAfter(currentDate)) {
            Period period = new Period(currentDate, currentYearBirthDay,monthDay );
            String currentBirthday = periodFormatter.print(period);
            return currentBirthday;
        } else {
            LocalDate nextYearBirthDay =currentYearBirthDay.plusYears(1);
            Period period = new Period(currentDate, nextYearBirthDay ,monthDay );
            String nextBirthday = periodFormatter.print(period);
            return nextBirthday;
        }
    }


    public LocalDate getNextBirthDate() {
        LocalDate dateOfBirth =birthDate.toLocalDate();
        LocalDate currentDate = new LocalDate();
        // Take birthDay  and birthMonth  from dateOfBirth
        int birthDay = dateOfBirth.getDayOfMonth();
        int birthMonth = dateOfBirth.getMonthOfYear();
        // Current year's birthday
        LocalDate currentYearBirthDay = new LocalDate().withDayOfMonth(birthDay)
                .withMonthOfYear(birthMonth);

        if (currentYearBirthDay.isAfter(currentDate)) {
            return currentYearBirthDay;
        }else{
            LocalDate nextYearBirthDay =currentYearBirthDay.plusYears(1);
            return nextYearBirthDay;
        }
    }

    public int getDayToNextBirht(){
        LocalDate dateOfBirth =getBirthDate().toLocalDate();
        LocalDate currentDate = new LocalDate();
        // Take birthDay  and birthMonth  from dateOfBirth
        int birthDay = dateOfBirth.getDayOfMonth();
        int birthMonth = dateOfBirth.getMonthOfYear();
        // Current year's birthday
        LocalDate currentYearBirthDay = new LocalDate().withDayOfMonth(birthDay)
                .withMonthOfYear(birthMonth);

        PeriodType periodTypeDay = PeriodType.yearDayTime().withYearsRemoved();

        if (currentYearBirthDay.isAfter(currentDate)) {
            Period periodDay = new Period(currentDate, currentYearBirthDay,periodTypeDay );
            return periodDay.getDays();
        } else {
            LocalDate nextYearBirthDay =currentYearBirthDay.plusYears(1);
            Period periodDay = new Period(currentDate, nextYearBirthDay,periodTypeDay );
            return periodDay.getDays();
        }
    }

    public static List<UserVK> getUserVKListFull(List<UserVK> userVKList){
        List<UserVK> userVKListFull = new ArrayList<>();
        for (UserVK userVK : userVKList){
            if (userVK.isNotify()) {
                UserVK user = new UserVK(userVK);
                user.setBirthDate(userVK.getBirthDate().minusDays(3));
                user.setNotify(false);
                userVKListFull.add(user);
            }
            UserVK user = new UserVK(userVK);
            user.setNotify(true);
            userVKListFull.add(user);
        }
        return userVKListFull;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public void setBirthDate(DateTime birthDate) {
        this.birthDate = birthDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getBirthDate() {
        return birthDate;
    }

    public String getName() {
        return name;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getAvatarURL() {return avatarURL; }

    public static List<UserVK> getUsersList(){
        return users;
    }

}
