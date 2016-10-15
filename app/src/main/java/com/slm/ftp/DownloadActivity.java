package com.slm.ftp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.slm.adapter.DownloadAdapter;
import com.slm.dbhelper.ThreadDAO;
import com.slm.method.Constant;
import com.slm.method.ThreadInfo;
import com.slm.upDown.DownloadService;

import java.util.List;

public class DownloadActivity extends AppCompatActivity {

    private ListView downloadListView = null;
    private List<ThreadInfo> threadList = null;
    private DownloadAdapter downloadAdapter = null;

    private ThreadDAO threadDAO = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 加载数据到ListView
            downloadListView.setAdapter(downloadAdapter);
            Log.i("info ---->", "downloadListView is finished.");
            // 注册广播接收器
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DownloadService.ACTION_UPDATE);
            intentFilter.addAction(DownloadService.ACTION_CANCEL);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                ThreadInfo threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
                int finished = threadInfo.getFinished();
                int position = threadInfo.getRemark();
                downloadAdapter.updateProgress(position, finished);
                if (threadInfo.getFinished() == threadInfo.getLength()) {
                    String fileName = threadInfo.getRemote_url().substring(threadInfo.getRemote_url().lastIndexOf("/") + 1);
                    Toast.makeText(DownloadActivity.this, fileName + " 下载完毕", Toast.LENGTH_SHORT).show();
                }

            } else if (DownloadService.ACTION_CANCEL.equals(intent.getAction())) {
                ThreadInfo threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
//                Log.i("position ---->", threadInfo.getRemark() + " " + threadList.get(threadInfo.getRemark()).getRemote_url());
                threadList.remove(threadInfo.getRemark());
//                downloadAdapter.notifyDataSetChanged();
//                initDataByThread();
                downloadAdapter = new DownloadAdapter(DownloadActivity.this, threadList);
                handler.sendEmptyMessage(0);

                Toast.makeText(DownloadActivity.this, "下载取消", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        // 初始化控件
        initView();

        // 下载列表初始化
        threadDAO = new ThreadDAO(this);
        initDataByThread();

    }

    /**
     * 下载列表初始化线程
     */
    private void initDataByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取待下载线程信息
                threadList = threadDAO.getThreadList(0); // 0 未下载完 1 已下载完
                downloadAdapter = new DownloadAdapter(DownloadActivity.this, threadList);
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 下载列表初始化
     */
    private void initView() {
        // 返回按钮
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 初始化视图控件
        downloadListView = (ListView) findViewById(R.id.downloadListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 1, 0, "");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                setResult(Constant.FTP_CANCEL);
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 监听返回键
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 按了返回键时应暂停下载

        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
