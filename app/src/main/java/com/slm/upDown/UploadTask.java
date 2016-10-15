package com.slm.upDown;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.slm.dbhelper.ThreadDAO;
import com.slm.method.FTPInfo;
import com.slm.method.ThreadInfo;
import com.slm.method.UploadStatus;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by Leaves on 2016/6/9.
 */
public class UploadTask {

    private Context context = null;

    private ThreadDAO threadDAO = null;
    private ThreadInfo threadInfo = null;

    private FTPClient ftpClient = null;

    private File localFile = null; // 带文件名路径
    private String remotePath = "/"; // 不带文件名路径

    public boolean isPause = false;
    public static final int BROADCAST_TIME = 1000; // 广播时间间隔

    public UploadTask(Context context, ThreadInfo threadInfo, FTPClient ftpClient) {
        this.context = context;
        this.threadInfo = threadInfo;
        this.ftpClient = ftpClient;
        threadDAO = new ThreadDAO(context);
    }

    public void start() {
        this.localFile = new File(threadInfo.getLocal_url());
        this.remotePath = threadInfo.getRemote_url();
        UploadThread uploadThread = new UploadThread();
        uploadThread.start();
    }

    /**
     * 下载线程类
     */
    private class UploadThread extends Thread {
        @Override
        public void run() {
            super.run();
            upload();
        }
    }

    /**
     * 上传文件到FTP服务器，支持断点续传
     */
    public void upload() {
        try {
            // 设置PassiveMode传输
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制流的方式传输
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);


            // 对远程目录的处理
            if (remotePath.contains("/")) {
                // 创建服务器远程目录结构，创建失败直接返回
                if (CreateDirectory() == UploadStatus.Create_Directory_Fail) {
                    return;
                }
            }

            // 检查远程是否存在文件
            String localFileName = localFile.getName(); // remotePath.substring(remotePath.lastIndexOf("/") + 1);
            FTPFile[] files = ftpClient.listFiles(localFileName); // new String(remoteFileName.getBytes("GBK"), "iso-8859-1")
            if (files.length > 0 && files[files.length - 1].getName().equals(localFileName)) {
                long remoteSize = files[files.length - 1].getSize();
                long localSize = localFile.length();
                if (remoteSize >= localSize) {
                    Log.i("上载 --->", "上载已完成，上载中止");
                    return;
                }
                // 尝试移动文件内读取指针,实现断点续传
                uploadFile(remoteSize);
            } else {
                uploadFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件到服务器,新上传和断点续传
     *
     * @param remoteSize
     */
    public void uploadFile(long remoteSize) {
        RandomAccessFile raf = null;
        OutputStream outputStream = null;

        try {
            // 显示进度的上传
            long process = 0;
            long localReadBytes = 0L;
            raf = new RandomAccessFile(localFile, "r");
            outputStream = ftpClient.appendFileStream(localFile.getName()); // new String(remoteFile.getBytes("GBK"), "iso-8859-1")

            // 断点续传
            if (remoteSize > 0) {
                ftpClient.setRestartOffset(remoteSize);
                raf.seek(remoteSize);
                process = (long) (remoteSize * 1.0 / localFile.length() * 100);
                localReadBytes = remoteSize;
                threadInfo.setFinished((int) remoteSize);
            }

            // 更新下载进度
            Intent intent = new Intent();
            intent.setAction(UploadService.ACTION_UPDATE);

            // 传输开始
            byte[] bytes = new byte[1024];
            int len;
            long time = System.currentTimeMillis();
            while ((len = raf.read(bytes)) != -1) {
                // 写入文件
                outputStream.write(bytes, 0, len);
                // 累加每个线程完成的进度
                threadInfo.setFinished(threadInfo.getFinished() + len);
                localReadBytes += len;
                // 下载进度控制台显示
                long nowProcess = (long) (localReadBytes * 1.0 / localFile.length() * 100);
                if (nowProcess > process) {
                    process = nowProcess;
                    Log.i("上载 --->", localFile.getName() + " " + process);
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
            outputStream.flush();

            threadInfo.setStatus(3);
            threadDAO.updateThread(threadInfo);
            // 发送广播通知UI下载任务结束
            intent.putExtra("threadInfo", threadInfo);
            context.sendBroadcast(intent);
            Log.i("上载 --->", "上载已完成，上载中止");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 递归创建远程服务器目录
     *
     * @return 目录创建是否成功
     * @throws IOException
     */
    public UploadStatus CreateDirectory() throws IOException {
        String directory = remotePath.substring(0, remotePath.lastIndexOf("/") + 1);
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(directory)) { // new String(directory.getBytes("GBK"), "iso-8859-1")
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            while (true) {
                String subDirectory = remotePath.substring(start, end); // new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1")
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        System.out.println("创建目录失败");
                        return UploadStatus.Create_Directory_Fail;
                    }
                }

                start = end + 1;
                end = directory.indexOf("/", start);

                //检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return UploadStatus.Create_Directory_Success;
    }
}
