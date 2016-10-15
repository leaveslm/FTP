package com.slm.ftp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.slm.method.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileExploreActivity extends AppCompatActivity {

    private ListView fileListView;

    private List<String> itemList = null;
    private List<String> pathList = null;
    private String rootPath = "/"; // 根目录
    private TextView currentPath;
    private String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explore);

        // 返回按钮
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        fileListView = (ListView) findViewById(R.id.fileListView);
        currentPath = (TextView) findViewById(R.id.currentPath);
        showFileDir(rootPath);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dealPathPosition(position);
            }
        });
    }

    /* 取得文件架构的 method，将 filePath 的各项文件和子目录显示出来 */
    private void showFileDir(String filePath) {
        currentPath.setText(filePath); // 当前路径
        Log.i("filePath -------->", this.filePath);

        itemList = new ArrayList<String>();
        pathList = new ArrayList<String>();
        File file = new File(filePath);
        File[] files = file.listFiles(); // 通过listFiles可获取当前File（目录）的所有文件及子目录

        if (!filePath.equals(rootPath)) { // 对非根目录的处理
            itemList.add("b1"); // 第一笔设定为[根目录]
            pathList.add(rootPath);
            itemList.add("b2"); // 第二笔设定为[返回上一层]
            pathList.add(file.getParent());
        }
		/* 将所有文件存入ArrayList中 */
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            itemList.add(file1.getName()); // itmes存放文件或目录名称
            pathList.add(file1.getPath()); // path存放对应的路径
        }
		/* 使用告定义的 FileAdapter 来将数据传入 ListActivity */
        fileListView.setAdapter(new FileAdapter(this, itemList, pathList));
    }

    /* 设定ListItem被按下时要做的动作 */
    public void dealPathPosition(int position) {
        File file = new File(pathList.get(position));
        if (file.canRead()) {
            if (file.isDirectory()) {
                showFileDir(pathList.get(position)); // 若是目录，则继续进入该目录
            } else {
                openFile(file); // 如果需要打开文件
//                getExcelFile(file); // 获得选中文件目录
            }
        } else {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("权限不足!").setPositiveButton("OK", null).show();
        }
    }

    private void getExcelFile(File file) {
        String fileName = file.getName();
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase(); // 取文件的扩展名，并转化为小写
        if (currentPath.getText().toString().equals("/")) {
            this.filePath = currentPath.getText().toString() + fileName;
        } else {
            this.filePath = currentPath.getText().toString() + "/" + fileName;
        }
        Log.i("filePath -------->", this.filePath);
        if (end.equals("xls") || end.equals("xlsx")) {
            Toast.makeText(FileExploreActivity.this, "已选择Excel文件 " + fileName, Toast.LENGTH_SHORT).show();
//            if (getIntent().getIntExtra("requestCode", MainActivity.requestCode) == MainActivity.requestCode) {
//                Intent intent = new Intent();
//                intent.putExtra("filePath", this.filePath);
//                setResult(MainActivity.requestCode, intent); //通过Intent对象返回结果，调用setResult方法
//                finish();
//            }
        } else {
            Toast.makeText(FileExploreActivity.this, "该文件不是Excel文件", Toast.LENGTH_SHORT).show();
        }
    }

    /* 用手机打开文件 */
    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Intent.FLAG_ACTIVITY_NEW_TASK
        // 相关解释见http://www.cnblogs.com/xiaoQLu/archive/2012/07/17/2595294.html
        intent.setAction(Intent.ACTION_VIEW);
        // intent.setDataAndType(Uri.fromFile(f),"*/*");
		/* 调用getMIMEType()来取得MimeType */
        String type = getMIMEType(file);// 自定义函数
		/* 设定intent的file与MimeType */
        intent.setDataAndType(Uri.fromFile(file), type);// mime type要用小写
        // audio/* 表示所有音频类
        // video/* 表示所有视频类
        // */* 表示所有类，会列出所有注册的程序供其选择打开
        startActivity(intent); // 通过 Action + Data的方式启动Intent
    }

    /* 判断文件 MimeType 的 method */
    private String getMIMEType(File file) {
        String type = "";
        String fileName = file.getName();
		/* 取得扩展名 */
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase(); // 取文件的扩展名，并转化为小写

		/* 依扩展名的类型决定MimeType */
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") ||
                end.equals("ogg") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        }  else if (end.equals("xls") || end.equals("xlsx")) {
            type = "*";
        } else {
            type = "*";
        }
		/* 如果无法直接打开，就弹出软件列表给用户选择 */
        type += "/*";
        return type;
    }


    class FileAdapter extends ArrayAdapter {
        private LayoutInflater mInflater;
        private int[] mIcons;
        private List<String> items; // itmes存放文件或目录名称
        private List<String> paths; // path存放对应的路径

        /* MyAdapter的构造函数，分别传入上写文、当前目录下的内容列表、路径列表 */
        public FileAdapter(Context context, List<String> itemList, List<String> pathList) {
            super(context, android.R.layout.simple_list_item_1, itemList); // 调用父类，主要是要用到getView的自动调用
            mInflater = LayoutInflater.from(context);// 从传入的上下文获得LayoutInflater,以便在getView改写中动态生成布局视图
            items = itemList;
            paths = pathList;
            mIcons = new int[]{R.drawable.back01, R.drawable.back02, R.drawable.folder, R.drawable.doc};
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text;
            ImageView icon;

            if (convertView == null) { // 如果convertView 为空，需要动态生成布局图，否则只需要进行更新即可
			/* 使用自定义的布局文件：file_row为视图，需要用inflate方法实现 */
                convertView = mInflater.inflate(R.layout.file_row, null);
            }
            text = (TextView) convertView.findViewById(R.id.text);
            icon = (ImageView) convertView.findViewById(R.id.icon);
            File file = new File(paths.get(position).toString());
		    /* 根据当前目录中的信息items来设置相应图标 */
            if (items.get(position).toString().equals("b1")) { // 设置返回根目录的图标
                text.setText("Back to /");
                icon.setImageResource(mIcons[0]);
            } else if (items.get(position).toString().equals("b2")) { // 设置返回上一层的图标
                text.setText("Back to ..");
                icon.setImageResource(mIcons[1]);
            } else { // 设置文件或文件夹的图标
                text.setText(file.getName());
                if (file.isDirectory()) {
                    icon.setImageResource(mIcons[2]);
                } else {
                    icon.setImageResource(mIcons[3]);
                }
            }
            return convertView;
        }

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
}
