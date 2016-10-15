package com.slm.upDown;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.slm.dbhelper.ThreadDAO;
import com.slm.method.ThreadInfo;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Leaves on 2016/6/4.
 */
public class DownloadTask {

    private Context context = null;

    private ThreadDAO threadDAO = null;
    private ThreadInfo threadInfo = null;

    private FTPFile ftpFile = null;
    private FTPClient ftpClient = null;

    private String remotePath = null; // 带文件名路径
    private String localPath = null; // 带文件名路径

    public boolean isPause = false;
    public static final int BROADCAST_TIME = 1000; // 广播时间间隔

    public DownloadTask(Context context, ThreadInfo threadInfo, FTPClient ftpClient, FTPFile ftpFile) {
        this.context = context;
        this.threadInfo = threadInfo;
        this.ftpClient = ftpClient;
        this.ftpFile = ftpFile;
        threadDAO = new ThreadDAO(context);
    }

    public void start() {
        this.localPath = threadInfo.getLocal_url() + ftpFile.getName();
        this.remotePath = threadInfo.getRemote_url();
        DownloadThread downloadThread = new DownloadThread();
        downloadThread.start();
    }

    /**
     * 下载线程类
     */
    private class DownloadThread extends Thread {
        @Override
        public void run() {
            super.run();
            download();
        }
    }

    /**
     * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报
     *
     * @return 上传的状态
     * @throws IOException
     */
    private void download() {
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;

        try {
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制方式传输
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);

            long ftpFileSize = ftpFile.getSize();
            Log.i("ftpFileSize --->", ftpFile.getSize() + "");
            File localFile = new File(localPath);
            long localSize = 0L;

            if (localFile.exists()) {
                localSize = localFile.length();
                //判断本地文件大小是否大于远程文件大小
                if (localSize >= ftpFileSize) {
                    Log.i("下载进度 --->", "已下载完成，下载中止");
                }
                // 进行断点续传，并记录状态
                fileOutputStream = new FileOutputStream(localFile, true); // 文件追加模式
                ftpClient.setRestartOffset(localSize);
                threadInfo.setFinished((int) localSize);
            } else {
                fileOutputStream = new FileOutputStream(localFile); // 新建文件
            }

            // 更新下载进度
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_UPDATE);

            // 传输开始
            inputStream = ftpClient.retrieveFileStream(remotePath); // new String(remotePath.getBytes("GBK"), "iso-8859-1") 会报错
            long process = (long) (localSize * 1.0 / ftpFileSize * 100);
            byte[] bytes = new byte[1024];
            int len = -1;
            long time = System.currentTimeMillis();
            while ((len = inputStream.read(bytes)) != -1) {
                // 写入文件
                fileOutputStream.write(bytes, 0, len);
                // 累加每个线程完成的进度
                threadInfo.setFinished(threadInfo.getFinished() + len);
                localSize += len;
                // 下载进度控制台显示
                long nowProcess = (long) (localSize * 1.0 / ftpFileSize * 100);
                if (nowProcess > process) {
                    process = nowProcess;
                    Log.i("下载进度 --->", ftpFile.getName() + " " + process);
                }

                // 每BROADCAST_TIMEms发送一次广播
                if (System.currentTimeMillis() - time > BROADCAST_TIME) {
                    time = System.currentTimeMillis();
                    // 把下载进度通过广播发送给 Activity
                    intent.putExtra("threadInfo", threadInfo);
                    context.sendBroadcast(intent);
                }

                if (isPause) {
                    // 存储下载进度
                    threadDAO.updateThread(threadInfo);
                    return;
                }
            }

            threadInfo.setStatus(1);
            threadDAO.updateThread(threadInfo);
            // 发送广播通知UI下载任务结束
            intent.putExtra("threadInfo", threadInfo);
            context.sendBroadcast(intent);
            Log.i("下载完成 --->", "");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
