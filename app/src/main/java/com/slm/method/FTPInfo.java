package com.slm.method;

import android.os.Environment;

import java.io.Serializable;

/**
 * Created by Leaves on 2016/6/8.
 */
public class FTPInfo implements Serializable {

    /**
     * ftp id
     */
    private int ftp_id = -1;

    /**
     * ftp 别名
     */
    private String ftpName = "";

    /**
     * 服务器名
     */
    private String hostName = "";

    /**
     * 端口号
     */
    private int port = 21;

    /**
     * 用户名
     */
    private String userName = "";

    /**
     * 密码
     */
    private String password = "";

    /**
     * 默认下载位置
     */
    public String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";

    public FTPInfo() { }

    public FTPInfo(int ftp_id, String ftpName, String hostName, int port, String userName, String password, String downloadPath) {

        this.ftp_id = ftp_id;
        this.ftpName = ftpName;
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.downloadPath = downloadPath;
    }

    public int getFtp_id() {
        return ftp_id;
    }

    public void setFtp_id(int ftp_id) {
        this.ftp_id = ftp_id;
    }

    public String getFtpName() {
        return ftpName;
    }

    public void setFtpName(String ftpName) {
        this.ftpName = ftpName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }
}
