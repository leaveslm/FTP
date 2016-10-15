package com.slm.ftp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.slm.adapter.LocalAdapter;
import com.slm.dbhelper.ThreadDAO;
import com.slm.method.FTPInfo;
import com.slm.method.ThreadInfo;
import com.slm.upDown.DownloadService;
import com.slm.upDown.UploadService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalActivity extends AppCompatActivity {

    /**
     * 本地文件集合
     */
    private List<File> localFile;

    /**
     * 本地SDCard根目录
     */
    private String currentLocalPath = "/mnt/sdcard/";

    /**
     * 上一次点击的路径
     */
    private String preLocalPath = "/mnt/sdcard/";

    private ListView localListView;
    private LocalAdapter localAdapter;

    private ThreadDAO threadDAO = null;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 加载数据到ListView
            localListView.setAdapter(localAdapter);
            Log.i("info ---->", "localListView is finished !");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        // 返回按钮
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        localListView = (ListView) findViewById(R.id.localListView);

        // 初始化
        initView();
        // 初始化本地列表内存
        localFile = new ArrayList<File>();
        // 加载本地列表
        loadDateByThread();

        threadDAO = new ThreadDAO(this);
    }

    /**
     * 显示本地列表视图
     */
    private void loadDateByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                localFile.clear();
                // 加载本地列表
                listFiles(currentLocalPath);
                // 本地列表适配器
                localAdapter = new LocalAdapter(LocalActivity.this, localFile);
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        localListView = (ListView) findViewById(R.id.localListView);
        localListView.setOnItemClickListener(localListViewItemClick);
        // 向ListView控件注册上下文菜单
        registerForContextMenu(localListView);
    }

    /**
     * localListView 单击事件
     */
    private AdapterView.OnItemClickListener localListViewItemClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (localFile.get(position).isDirectory()) {
                if (localFile.get(position).getName().equals(".")) {  // 返回根目录
                    currentLocalPath = preLocalPath = "/mnt/sdcard/";
                } else if (localFile.get(position).getName().equals("..")) {  // 返回上一级目录
                    if (!currentLocalPath.equals("/mnt/sdcard/")) {
                        currentLocalPath = preLocalPath;
                        String tempPath = preLocalPath.substring(0, preLocalPath.length() - 1);
                        Log.i("tempPath ---->", tempPath);
                        preLocalPath = tempPath.substring(0, tempPath.lastIndexOf("/") + 1);
                    }
                } else {
                    preLocalPath = currentLocalPath;
                    currentLocalPath += localFile.get(position).getName() + "/";
                }
                Log.i("preLocalPath ---->", preLocalPath);
                Log.i("currentLocalPath ---->", currentLocalPath);
                loadDateByThread();
            }
        }
    };

    private void listFiles(String path) {
        // 获取根目录
        File f = new File(path);
        // 获取根目录下所有文件
        File[] files = f.listFiles();
        if (files.length > -1) {
            // 添加返回根目录和上一级路径
            File f1 = new File("./");
            Log.i("root ---->", f1.getName());
            localFile.add(f1);
            f1 = new File("..");
            localFile.add(f1);

            // 循环添加到本地列表
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isHidden() || file.getName().equals("LOST.DIR")) {
                    continue;
                }
                localFile.add(file);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "上载列表");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

            case 1: // 本地文件列表
                Intent intent = new Intent(LocalActivity.this, UploadActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("文件操作");
        menu.add(0, 1, 0, "加入上载列表");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case 1: // 加入上载列表
                if (!localFile.get((int) info.id).isDirectory()) {
                    FTPInfo ftpInfo = (FTPInfo) getIntent().getSerializableExtra("ftpInfo");
                    int ftp_id = ftpInfo.getFtp_id();
                    String remote_url = getIntent().getStringExtra("remotePath");
                    String local_url = localFile.get((int) info.id).getAbsolutePath();
                    int length = (int) localFile.get((int) info.id).length();
                    int start = 0, end = 0, finished = 0, status = 2;
                    ThreadInfo threadInfo = new ThreadInfo(ftp_id, remote_url, local_url, length, start, end, finished, status);

                    boolean result = threadDAO.insertThread(threadInfo);
                    if (result) {
                        Toast.makeText(LocalActivity.this, "已加入上载列表", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LocalActivity.this, "加入上载列表失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LocalActivity.this, "暂时不支持文件夹上载", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }

        return super.onContextItemSelected(item);
    }
}
