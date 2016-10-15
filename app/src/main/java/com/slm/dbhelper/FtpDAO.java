package com.slm.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.slm.method.FTPInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leaves on 2016/6/5.
 */
public class FtpDAO {

    private DBHelper dbHelper = null;

    public FtpDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public Cursor getCursor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("select * from ftp_info", null);
    }

    public synchronized void insertFTP(FTPInfo ftpInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("insert into ftp_info(ftpName, hostName, port, userName, password) " +
                "values(?, ?, ?, ?, ?)",
                new Object[]{ftpInfo.getFtpName(), ftpInfo.getHostName(), ftpInfo.getPort(),
                        ftpInfo.getUserName(), ftpInfo.getPassword()});
        db.close();
    }

    public synchronized void deleteFTP(FTPInfo ftpInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from ftp_info where _id = ?", new Object[]{ftpInfo.getFtp_id()});
        db.close();
    }

    public synchronized void updateFTP(FTPInfo ftpInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update ftp_info set ftpName = ?, hostName = ?, port = ?, " +
                "userName = ?, password = ? where _id = ?",
                new Object[]{ftpInfo.getFtpName(), ftpInfo.getHostName(), ftpInfo.getPort(),
                        ftpInfo.getUserName(), ftpInfo.getPassword(), ftpInfo.getFtp_id()});
        db.close();
    }

    public FTPInfo getFTPInfo(long ftp_id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from ftp_info where _id = ?", new String[]{ftp_id + ""});
        cursor.moveToNext();

        FTPInfo ftpInfo = new FTPInfo();
        ftpInfo.setFtp_id(cursor.getInt(cursor.getColumnIndex("_id")));
        ftpInfo.setFtpName(cursor.getString(cursor.getColumnIndex("ftpName")));
        ftpInfo.setHostName(cursor.getString(cursor.getColumnIndex("hostName")));
        ftpInfo.setPort(cursor.getInt(cursor.getColumnIndex("port")));
        ftpInfo.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
        ftpInfo.setPassword(cursor.getString(cursor.getColumnIndex("password")));

        cursor.close();
        db.close();
        return ftpInfo;
    }

    public List<FTPInfo> getFTPList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from ftp_info", null);

        List<FTPInfo> list = new ArrayList<FTPInfo>();
        while (cursor.moveToNext()) {
            FTPInfo ftpInfo = new FTPInfo();
            ftpInfo.setFtp_id(cursor.getInt(cursor.getColumnIndex("_id")));
            ftpInfo.setFtpName(cursor.getString(cursor.getColumnIndex("ftpName")));
            ftpInfo.setHostName(cursor.getString(cursor.getColumnIndex("hostName")));
            ftpInfo.setPort(cursor.getInt(cursor.getColumnIndex("port")));
            ftpInfo.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
            ftpInfo.setPassword(cursor.getString(cursor.getColumnIndex("password")));
            list.add(ftpInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean isExists(FTPInfo ftpInfo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from ftp_info where _id = ?",
                new String[]{ftpInfo.getFtp_id() + ""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}

/*
db.execSQL(DB_Sql);
ContentValues cv = new ContentValues();
cv.put("ftpName", "Android");
cv.put("hostName", "10.132.252.208");
cv.put("port", 2121);
cv.put("userName", "android");
cv.put("password", "android");
db.insert(T_NAME, null, cv);

cv.put("ftpName", "Mine");
cv.put("hostName", "192.168.3.3");
cv.put("port", 21);
cv.put("userName", "anonymous");
cv.put("password", "");
db.insert(T_NAME, null, cv);
 */