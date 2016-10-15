package com.slm.upDown;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.slm.dbhelper.FtpDAO;
import com.slm.dbhelper.ThreadDAO;
import com.slm.method.FTPInfo;
import com.slm.method.ThreadInfo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Leaves on 2016/6/4.
 */
public class DownloadService extends Service {

    public static final String ACTION_START = "ACTION_START"; // 开始下载
    public static final String ACTION_STOP = "ACTION_STOP"; // 暂停下载
    public static final String ACTION_CANCEL = "ACTION_CANCEL"; // 暂停下载
    public static final String ACTION_UPDATE = "ACTION_UPDATE"; // 更新UI
    public static final String ACTION_FINISHED = "ACTION_FINISHED"; // 下载结束

    private FtpDAO ftpDAO = null;
    private ThreadDAO threadDAO = null;

    private FTP ftp = null;
    private FTPClient ftpClient = null;
    private ThreadInfo threadInfo = null;
    private FTPInfo ftpInfo = null;
    private FTPFile ftpFile = null;

    private Map<Integer, DownloadTask> tasks = new  LinkedHashMap<Integer, DownloadTask>(); // 下载任务集合

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadTask downloadTask = new DownloadTask(DownloadService.this, threadInfo, ftpClient, ftpFile);
            downloadTask.start();
            tasks.put(threadInfo.getThread_id(), downloadTask);
        }
    };

    /**
     * 启动Service自动调用onStartCommand
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START)) {
            threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
            String fileName = threadInfo.getRemote_url().substring(threadInfo.getRemote_url().lastIndexOf("/") + 1);
            // 下载任务初始化
            InitThread initThread = new InitThread();
            initThread.start();
            Log.i("ACTION_START ---->", fileName + " Start");

        } else if (intent.getAction().equals(ACTION_STOP)) {
            threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
            String fileName = threadInfo.getRemote_url().substring(threadInfo.getRemote_url().lastIndexOf("/") + 1);
            // 取出下载任务
            DownloadTask downloadTask = tasks.get(threadInfo.getThread_id());
            if (downloadTask != null) {
                downloadTask.isPause = true;
            }
            Log.i("ACTION_STOP ---->", fileName + " Stop");

        } else if (intent.getAction().equals(ACTION_CANCEL)) {
            threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
            threadDAO = new ThreadDAO(this);
            threadDAO.deleteThread(threadInfo);
            // 广播通知下载任务取消
            Intent intent1 = new Intent();
            intent1.setAction(ACTION_CANCEL);
            intent1.putExtra("threadInfo", threadInfo);
            this.sendBroadcast(intent1);
            Log.i("ACTION_CANCEL ---->", "Cancel");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化下载任务
     */
    class InitThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                ftpDAO = new FtpDAO(DownloadService.this);
                ftpInfo = ftpDAO.getFTPInfo(threadInfo.getFtp_id());
                ftp = new FTP(ftpInfo);
                ftp.openConnect();
                ftpClient = ftp.getFtpClient();
                ftpFile = ftp.getFTPFile(threadInfo.getRemote_url());
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
