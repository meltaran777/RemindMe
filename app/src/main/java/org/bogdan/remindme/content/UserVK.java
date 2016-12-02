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

    private DateTime birthDate;

    private final String dateFormat;
    private final String avatarURL;
    private boolean notify;

    static private ArrayList<UserVK> users=new ArrayList<>();

    public UserVK(String name, DateTime birthDate, String dateFormat, String avatarURL, boolean notify) {
        this.name = name;
        this.birthDate = birthDate;
        this.dateFormat = dateFormat;
        this.avatarURL =avatarURL;
        this.notify = notify;
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

    @Override
    public int compareTo(UserVK another) {
        Integer dayThis = getDayToNextBirht();
        Integer dayAnother = another.getDayToNextBirht();

        return dayThis.compareTo(dayAnother);
    }

    public static String getTimeToNextBirht(UserVK userVK){

        LocalDate dateOfBirth =userVK.getBirthDate().toLocalDate();
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


    public static LocalDate getNextBirthDate(DateTime birthDate) {

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
            userVKListFull.add(userVK);
            if (userVK.isNotify()) {
                userVK.setBirthDate(userVK.getBirthDate().minusDays(3));
                userVKListFull.add(userVK);
            }
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

}
