package com.slm.method;

import android.app.Application;

/**
 * Created by Leaves on 2016/5/19.
 */
public class Constant /*extends Application*/ {

    /**
     * ftp 相关
     */
    public static final String FTP_CONNECT_SUCCESSS = "ftp连接成功";
    public static final String FTP_CONNECT_FAIL = "ftp连接失败";
    public static final String FTP_DISCONNECT_SUCCESS = "ftp断开连接";
    public static final String FTP_FILE_NOTEXISTS = "ftp上文件不存在";

    public static final String FTP_UPLOAD_SUCCESS = "ftp文件上传成功";
    public static final String FTP_UPLOAD_FAIL = "ftp文件上传失败";
    public static final String FTP_UPLOAD_LOADING = "ftp文件正在上传";

    public static final String FTP_DOWN_LOADING = "ftp文件正在下载";
    public static final String FTP_DOWN_SUCCESS = "ftp文件下载成功";
    public static final String FTP_DOWN_FAIL = "ftp文件下载失败";

    public static final String FTP_DELETEFILE_SUCCESS = "ftp文件删除成功";
    public static final String FTP_DELETEFILE_FAIL = "ftp文件删除失败";

    /**
     * 数据库相关
     */
    public static final int FTP_CANCEL = 0; // 添加
    public static final int ADD_REQUEST_CODE = 1; // 添加
    public static final int UPDATE_REQUEST_CODE = 2; // 更新
}
