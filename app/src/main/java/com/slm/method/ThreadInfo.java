package com.slm.method;

import java.io.Serializable;

/**
 * Created by Leaves on 2016/6/4.
 * 线程信息
 */
public class ThreadInfo implements Serializable {

    /**
     * 线程id
     */
    private int thread_id;

    /**
     * ftp id
     */
    private int ftp_id;

    /**
     * 线程对应下载链接
     */
    private String remote_url;

    /**
     * 线程对应本地位置
     */
    private String local_url;

    /**
     * 该线程载文件总长度长度
     */
    private int length = 0;

    /**
     * 下载开始位置
     */
    private int start = 0;

    /**
     * 下载结束位置
     */
    private int end = 0;

    /**
     * 下载进度
     */
    private int finished = 0;

    /**
     * 改任务是否已下载完成 0 未完成 1 完成
     */
    private int status = 0;

    /**
     * 备用，非数据库字段
     */
    private int Remark = -1;

    public ThreadInfo() { }

    public ThreadInfo(int ftp_id, String remote_url, String local_url, int length, int start, int end, int finished, int status) {
        this.ftp_id = ftp_id;
        this.remote_url = remote_url;
        this.local_url = local_url;
        this.length = length;
        this.start = start;
        this.end = end;
        this.finished = finished;
        this.status = status;
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public int getFtp_id() {
        return ftp_id;
    }

    public void setFtp_id(int ftp_id) {
        this.ftp_id = ftp_id;
    }

    public String getRemote_url() {
        return remote_url;
    }

    public void setRemote_url(String remote_url) {
        this.remote_url = remote_url;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRemark() {
        return Remark;
    }

    public void setRemark(int remark) {
        Remark = remark;
    }

}
