package com.slm.method;

/**
 * Created by Leaves on 2016/6/6.
 */
public enum DownloadStatus {
    Remote_File_Noexist,            //远程文件不存在
    Local_Bigger_Remote,            //本地文件大于远程文件
    Download_From_Break_Success,    //断点下载文件成功
    Download_From_Break_Failed,     //断点下载文件失败
    Download_Success,               //下载文件成功
    Download_New_Success,           //全新下载文件成功
    Download_New_Failed,            //全新下载文件失败
    Download_Failed;                //全新下载文件失败
}