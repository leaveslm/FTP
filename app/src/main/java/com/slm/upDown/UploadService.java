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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Leaves on 2016/6/9.
 */
public class UploadService extends Service {

    public static final String ACTION_START = "ACTION_START"; // 开始上载
    public static final String ACTION_STOP = "ACTION_STOP"; // 暂停上载
    public static final String ACTION_CANCEL = "ACTION_CANCEL"; // 暂停上载
    public static final String ACTION_UPDATE = "ACTION_UPDATE"; // 更新UI
    public static final String ACTION_FINISHED = "ACTION_FINISHED"; // 上载结束

    private FtpDAO ftpDAO = null;
    private ThreadDAO threadDAO = null;

    private FTP ftp = null;
    private FTPInfo ftpInfo = null;
    private FTPClient ftpClient = null;

    private ThreadInfo threadInfo = null;
    private File localFile = null;
    private String remotePath = "/";

    private Map<Integer, UploadTask> tasks = new LinkedHashMap<Integer, UploadTask>(); // 下载任务集合


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            UploadTask uploadTask = new UploadTask(UploadService.this, threadInfo, ftpClient);
            uploadTask.start();
            tasks.put(threadInfo.getThread_id(), uploadTask);
        }
    };

    /**
     * 启动Service自动调用onStartCommand
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START)) {
            threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
            String fileName = threadInfo.getLocal_url().substring(threadInfo.getLocal_url().lastIndexOf("/") + 1);
            InitThread initThread = new InitThread();
            initThread.start();
            Log.i("ACTION_START ---->", fileName + " Start");

        } else if (intent.getAction().equals(ACTION_STOP)) {
            threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
            String fileName = threadInfo.getLocal_url().substring(threadInfo.getLocal_url().lastIndexOf("/") + 1);
            // 取出下载任务
            UploadTask uploadTask = tasks.get(threadInfo.getThread_id());
            if (uploadTask != null) {
                uploadTask.isPause = true;
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
     * 初始化 ftp 连接子线程
     */
    class InitThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                ftpDAO = new FtpDAO(UploadService.this);
                ftpInfo = ftpDAO.getFTPInfo(threadInfo.getFtp_id());
                ftp = new FTP(ftpInfo);
                ftp.openConnect();
                ftpClient = ftp.getFtpClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            Log.i("ftp ---->", "openConnect");
            handler.sendEmptyMessage(0);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
