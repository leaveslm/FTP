package com.slm.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Leaves on 2016/6/8.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ftp.db";
    private static final int VERSION = 1;

    private static final String SQL_CREATE_FTP_TABLE =
            "create table if not exists ftp_info(_id integer primary key autoincrement, " +
            "ftpName text, hostName text, port integer, userName text, password text)";

    private static final String SQL_CREATE_THREAD_TABLE =
            "create table if not exists thread_info(_id integer primary key autoincrement, " +
            "ftp_id integer, remote_url text, local_url text, length integer, " +
            "start integer, end integer, finished integer, status integer)";

    private static final String SQL_DROP_FTP_TABLE = "drop table if exists ftp_info";

    private static final String SQL_DROP_THREAD_TABLE = "drop table if exists thread_info";

    private static DBHelper dbHelper = null;

    // 单例模式
    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (null == dbHelper) {
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FTP_TABLE);
        db.execSQL(SQL_CREATE_THREAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_FTP_TABLE);
        db.execSQL(SQL_DROP_THREAD_TABLE);
        db.execSQL(SQL_CREATE_FTP_TABLE);
        db.execSQL(SQL_CREATE_THREAD_TABLE);
    }
}
