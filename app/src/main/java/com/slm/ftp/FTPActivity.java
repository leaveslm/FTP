package com.slm.ftp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.slm.adapter.RemoteAdapter;
import com.slm.dbhelper.ThreadDAO;
import com.slm.method.ThreadInfo;
import com.slm.upDown.FTP;
import com.slm.method.FTPInfo;
import com.slm.upDown.DownloadService;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTPActivity extends AppCompatActivity {

    /**
     * commons ftp
     */
    private FTP ftp = null;

    /**
     * ftp 站点对象
     */
    private FTPInfo ftpInfo = null;

    /**
     * 当前 FTP 路径
     */
    private String currentRemotePath = "/";

    /**
     * 上一次点击的 FTP 路径
     */
    private String preRemotePath = "/";

    /**
     * FTP文件集合
     */
    private List<FTPFile> remoteFile = null;

    private RemoteAdapter remoteAdapter = null;
    private ListView remoteListView = null;
    private ThreadDAO threadDAO = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 加载数据到ListView
            remoteListView.setAdapter(remoteAdapter);
            Log.i("info ---->", "loadRemoteView is finished.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        // 返回按钮
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initView();
        // 获取对应 FTP 登陆信息
        ftpInfo = (FTPInfo) getIntent().getSerializableExtra("ftpInfo");
        // 初始化ftp列表内存
        remoteFile = new ArrayList<FTPFile>();
        initByThread();

        threadDAO = new ThreadDAO(this);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        remoteListView = (ListView) findViewById(R.id.remoteListView);
        remoteListView.setOnItemClickListener(remoteListViewItemClick);
        registerForContextMenu(remoteListView);  // 向ListView控件注册上下文菜单
    }

    /**
     * 线程中初始化ftp列表视图
     */
    private void initByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                openConnection();
                loadDate();  // 耗时操作
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    /**
     * 线程中加载数据
     */
    private void loadDateByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadDate();  // 耗时操作
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    /**
     * 打开ftp连接
     */
    private void openConnection() {
        try {
            if (ftp != null) {
                // 关闭FTP服务
                ftp.closeConnect();
            }
            // 初始化FTP
            ftp = new FTP(ftpInfo);
            // 打开FTP服务
            ftp.openConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载数据
     */
    private void loadDate() {
        try {
            remoteFile.clear();
            // 加载FTP列表
            remoteFile = ftp.listFiles(currentRemotePath);
            // FTP列表适配器
            remoteAdapter = new RemoteAdapter(this, remoteFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * remoteListView 单击事件
     */
    private AdapterView.OnItemClickListener remoteListViewItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (remoteFile.get(position).isDirectory()) {
                // ftp 路径设置
                if (remoteFile.get(position).getName().equals("/")) { // 返回根目录
                    currentRemotePath = preRemotePath = "/";
                } else if (remoteFile.get(position).getName().equals("..")) { // 返回上一级目录
                    if (!currentRemotePath.equals("/")) {
                        currentRemotePath = preRemotePath;
                        String tempPath = preRemotePath.substring(0, preRemotePath.length() - 1);
                        Log.i("tempPath ---->", tempPath);
                        preRemotePath = tempPath.substring(0, tempPath.lastIndexOf("/") + 1);
                    }
                } else {
                    preRemotePath = currentRemotePath;
                    currentRemotePath += remoteFile.get(position).getName() + "/";
                }
                Log.i("preRemotePath ---->", preRemotePath);
                Log.i("currentRemotePath ---->", currentRemotePath);
                loadDateByThread();
            } else {
                Log.i("size --->", String.valueOf(remoteFile.get(position).getSize()));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "本地文件列表");
        menu.add(0, 2, 0, "下载列表");
        menu.add(0, 3, 0, "上载列表");
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
                Intent intent = new Intent(FTPActivity.this, LocalActivity.class);
                intent.putExtra("remotePath", currentRemotePath);
                intent.putExtra("ftpInfo", ftpInfo);
                FTPActivity.this.startActivity(intent);
                break;

            case 2:
                Intent intent1 = new Intent(FTPActivity.this, DownloadActivity.class);
                startActivity(intent1);
                break;

            case 3:
                Intent intent2 = new Intent(FTPActivity.this, UploadActivity.class);
                startActivity(intent2);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("文件操作");
        menu.add(0, 1, 0, "快速下载");
        menu.add(0, 2, 0, "加入下载列表");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case 1: // 下载
                final String remotePath = currentRemotePath;
                final String fileName = remoteFile.get((int) info.id).getName().toString();
                final String localPath = ftpInfo.getDownloadPath();
                Log.i("localPath ---->", localPath);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ftp.download(remotePath, fileName, localPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Log.i("Download ---->", remoteFile.get((int) info.id).getName().toString());
                break;

            case 2: // 加入下载列表
                if (!remoteFile.get((int) info.id).isDirectory()) {
                    int ftp_id = ftpInfo.getFtp_id();
                    String remote_url = currentRemotePath + remoteFile.get((int) info.id).getName().toString();
                    String local_url = ftpInfo.getDownloadPath();
                    int length = (int) remoteFile.get((int) info.id).getSize();
                    int start = 0, end = 0, finished = 0, status = 0;
                    ThreadInfo threadInfo = new ThreadInfo(ftp_id, remote_url, local_url, length, start, end, finished, status);

                    boolean result = threadDAO.insertThread(threadInfo);
                    if (result) {
                        Toast.makeText(FTPActivity.this, "已加入下载列表", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FTPActivity.this, "加入下载列表失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FTPActivity.this, "暂时不支持文件夹下载", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 监听返回键
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
