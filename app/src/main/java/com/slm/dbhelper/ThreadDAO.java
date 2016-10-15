package com.slm.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.slm.method.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leaves on 2016/6/5.
 */
public class ThreadDAO {

    private DBHelper dbHelper = null;
    private static String TABLE_NAME = "thread_info";

    public ThreadDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public synchronized boolean insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db.execSQL("insert into thread_info(ftp_id, remote_url, local_url, length, start, end, finished, status) " +
//                "values(?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{threadInfo.getFtp_id(), threadInfo.getRemote_url(),
//                threadInfo.getLocal_url(), threadInfo.getLength(), threadInfo.getStart(), threadInfo.getEnd(),
//                threadInfo.getFinished(), threadInfo.getStatus()});
        ContentValues cv = new ContentValues();
        cv.put("ftp_id", threadInfo.getFtp_id());
        cv.put("remote_url", threadInfo.getRemote_url());
        cv.put("local_url", threadInfo.getLocal_url());
        cv.put("length", threadInfo.getLength());
        cv.put("start", threadInfo.getStart());
        cv.put("end", threadInfo.getEnd());
        cv.put("finished", threadInfo.getFinished());
        cv.put("status", threadInfo.getStatus());
        long result = db.insert(TABLE_NAME, null, cv);
        db.close();
        if (result > -1) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void deleteThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(threadInfo.getThread_id())});
//        db.execSQL("delete from thread_info where _id = ?", new Object[]{threadInfo.getThread_id()});
        db.close();
    }

    public synchronized void updateThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ftp_id", threadInfo.getFtp_id());
        cv.put("remote_url", threadInfo.getRemote_url());
        cv.put("local_url", threadInfo.getLocal_url());
        cv.put("length", threadInfo.getLength());
        cv.put("start", threadInfo.getStart());
        cv.put("end", threadInfo.getEnd());
        cv.put("finished", threadInfo.getFinished());
        cv.put("status", threadInfo.getStatus());
        db.update(TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(threadInfo.getThread_id())});
//        db.execSQL("update thread_info set finished = ?, status = ? where _id = ?",
//                new Object[]{threadInfo.getFinished(), threadInfo.getStatus(), threadInfo.getThread_id()});
        db.close();
    }

    public List<ThreadInfo> getThreadList(int where) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where status = ?", new String[]{where + ""});

        List<ThreadInfo> list = new ArrayList<ThreadInfo>();
        while (cursor.moveToNext()) {
            ThreadInfo threadInfo = new ThreadInfo();
            threadInfo.setThread_id(cursor.getInt(cursor.getColumnIndex("_id")));
            threadInfo.setFtp_id(cursor.getInt(cursor.getColumnIndex("ftp_id")));
            threadInfo.setRemote_url(cursor.getString(cursor.getColumnIndex("remote_url")));
            threadInfo.setLocal_url(cursor.getString(cursor.getColumnIndex("local_url")));
            threadInfo.setLength(cursor.getInt(cursor.getColumnIndex("length")));
            threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            threadInfo.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            list.add(threadInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean isExists(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where _id = ?",
                new String[]{threadInfo.getThread_id() + ""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
