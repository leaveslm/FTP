package com.slm.ftp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.slm.dbhelper.FtpDAO;
import com.slm.method.Constant;
import com.slm.method.FTPInfo;

/**
 * ftp应用软件
 * 1）具有服务器端（PC）和客户端（android）
 * 2）支持本地和远程系统的文件列表
 * 3）支持文件的下载和上传
 * 4）支持中文文件名解析
 * 5）支持上传文件进度显示
 * 扩展功能
 * 1）支持断点续传
 * 2）支持多线程传输
 */

public class MyFTPActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // 列表控件
    private ListView ftpListView;

    // 数据库操作对象
    private FtpDAO ftpDAO = null;
    private Cursor cursor;

    private SimpleCursorAdapter ftpListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ftp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ftpDAO = new FtpDAO(this);
        cursor = ftpDAO.getCursor();

        ftpListAdapter = new SimpleCursorAdapter(this, R.layout.ftp_list,
                cursor, new String[]{"ftpName", "hostName", "port"},
                new int[]{R.id.ftpNameTextView, R.id.hostNameTextView, R.id.portTextView}, 0);

        ftpListView = (ListView) findViewById(R.id.ftpListView);
        ftpListView.setAdapter(ftpListAdapter);

        registerForContextMenu(ftpListView);  // 向ListView控件注册上下文菜单

        // 设置点击 item 连接到FTP站点
        ftpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyFTPActivity.this, FTPActivity.class);
                intent.putExtra("ftpInfo", ftpDAO.getFTPInfo(id));
                MyFTPActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 1, 0, "添加FTP站点");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case 1: // 添加FTP站点
                Intent intent = new Intent(MyFTPActivity.this, FTPEditActivity.class);
                intent.putExtra("requestCode", Constant.ADD_REQUEST_CODE);
                startActivityForResult(intent, Constant.ADD_REQUEST_CODE);
                break;

            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("编辑FTP站点");
        menu.add(0, 1, 0, "修改");
        menu.add(0, 2, 0, "删除");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1: // 修改
                Intent intent = new Intent(MyFTPActivity.this, FTPEditActivity.class);
                FTPInfo ftpInfo = ftpDAO.getFTPInfo(info.id);
                intent.putExtra("ftpInfo", ftpInfo);
                intent.putExtra("requestCode", Constant.UPDATE_REQUEST_CODE);
                startActivityForResult(intent, Constant.UPDATE_REQUEST_CODE);
                break;

            case 2: // 删除
                FTPInfo ftpInfo1 = new FTPInfo();
                ftpInfo1.setFtp_id((int) info.id);
                delete(ftpInfo1);
                break;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * ftp 站点删除
     *
     * @param ftpInfo
     */
    public void delete(final FTPInfo ftpInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("！确认删除");
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ftpDAO.deleteFTP(ftpInfo);
                boolean result = ftpDAO.isExists(ftpInfo);
                if (!result) {
                    reFresh();
                    Toast.makeText(MyFTPActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyFTPActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * ftp 站点列表刷新
     */
    public void reFresh() {
        cursor = ftpDAO.getCursor();
        ftpListAdapter.swapCursor(cursor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Constant.ADD_REQUEST_CODE) {
            FTPInfo ftpInfo = (FTPInfo) data.getSerializableExtra("ftpInfo");
            ftpDAO.insertFTP(ftpInfo);
            reFresh();

        } else if (resultCode == Constant.UPDATE_REQUEST_CODE) {
            FTPInfo ftpInfo = (FTPInfo) data.getSerializableExtra("ftpInfo");
            ftpDAO.updateFTP(ftpInfo);
            reFresh();

        } else if (resultCode == Constant.FTP_CANCEL) {
            Toast.makeText(MyFTPActivity.this, "操作取消", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_camera) {
            intent = new Intent(MyFTPActivity.this, FTPEditActivity.class);
            intent.putExtra("requestCode", Constant.ADD_REQUEST_CODE);
            startActivityForResult(intent, Constant.ADD_REQUEST_CODE);

        } else if (id == R.id.nav_gallery) {
            intent = new Intent(MyFTPActivity.this, FileExploreActivity.class);
            MyFTPActivity.this.startActivity(intent);

        } else if (id == R.id.nav_slideshow) {
            intent = new Intent(MyFTPActivity.this, DownloadActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            intent = new Intent(MyFTPActivity.this, UploadActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
