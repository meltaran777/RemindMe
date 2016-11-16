package org.bogdan.remindme.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Bodia on 15.11.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "usersVK";
    public static final String TABLE_USERS = "users";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_BDATE = "bdate";
    public static final String KEY_DATE_FORMAT = "date_format";
    public static final String KEY_AVATAR_URL = "avatar_url";




    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TABLE_USERS
                +"(" + KEY_ID +" integer primary key,"
                + KEY_NAME + " text,"
                + KEY_DATE_FORMAT + " text,"
                + KEY_AVATAR_URL + " text,"
                + KEY_BDATE +" text"+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exist "+ TABLE_USERS);
        onCreate(db);
    }
}
