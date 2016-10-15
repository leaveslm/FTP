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

import com.slm.adapter.UploadAdapter;
import com.slm.dbhelper.ThreadDAO;
import com.slm.method.Constant;
import com.slm.method.ThreadInfo;
import com.slm.upDown.UploadService;

import java.util.List;

public class UploadActivity extends AppCompatActivity {

    private ListView uploadListView = null;
    private List<ThreadInfo> threadList = null;
    private UploadAdapter uploadAdapter = null;

    private ThreadDAO threadDAO = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 加载数据到ListView
            uploadListView.setAdapter(uploadAdapter);
            Log.i("info ---->", "uploadListView is finished.");
            // 注册广播接收器
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UploadService.ACTION_UPDATE);
            intentFilter.addAction(UploadService.ACTION_CANCEL);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UploadService.ACTION_UPDATE.equals(intent.getAction())) {
                ThreadInfo threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");

                int finished = threadInfo.getFinished();
                int position = threadInfo.getRemark();
                uploadAdapter.updateProgress(position, finished);

                if (threadInfo.getFinished() == threadInfo.getLength()) {
                    String fileName = threadInfo.getLocal_url().substring(threadInfo.getLocal_url().lastIndexOf("/") + 1);
                    Toast.makeText(UploadActivity.this, fileName + " 上载完毕", Toast.LENGTH_SHORT).show();
                }

            } else if (UploadService.ACTION_CANCEL.equals(intent.getAction())) {
                ThreadInfo threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
                threadList.remove(threadInfo.getRemark());
                uploadAdapter = new UploadAdapter(UploadActivity.this, threadList);
                handler.sendEmptyMessage(0);
                Toast.makeText(UploadActivity.this, "上载取消", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // 初始化控件
        initView();

        // 上载列表初始化
        threadDAO = new ThreadDAO(this);
        initDataByThread();

    }

    /**
     * 上载列表初始化线程
     */
    private void initDataByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取待上载线程信息
                threadList = threadDAO.getThreadList(2); // 2 未上载完 3 已上载完
                uploadAdapter = new UploadAdapter(UploadActivity.this, threadList);
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 上载列表初始化
     */
    private void initView() {
        // 返回按钮
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 初始化视图控件
        uploadListView = (ListView) findViewById(R.id.uploadListView);
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
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 按了返回键时应暂停上载

        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}