package com.slm.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.slm.ftp.R;
import com.slm.method.Util;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.List;

/**
 * FTP 列表适配器
 * Created by Leaves on 2016/5/20.
 */
public class RemoteAdapter extends BaseAdapter {

    /**
     * FTP文件列表
     */
    private List<FTPFile> list = new ArrayList<FTPFile>();

    /**
     * 布局
     */
    private LayoutInflater inflater;

    /**
     * 文件夹显示图片
     */
    private Bitmap folderIcon;

    /**
     * 文件显示图片
     */
    private Bitmap docIcon;

    /**
     * 构造函数
     * @param context 当前环境
     * @param remoteFile FTP文件列表
     */
    public RemoteAdapter(Context context, List<FTPFile> remoteFile) {
        this.list = remoteFile;
        this.inflater = LayoutInflater.from(context);
        // 文件夹显示图片
        folderIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
        // 文件显示图片
        docIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.doc);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // 设置视图
            convertView = this.inflater.inflate(R.layout.file_list, null);
            // 获取控件实例
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.fileNameTextView = (TextView) convertView.findViewById(R.id.fileNameTextView);
            holder.fileSizeTextView = (TextView) convertView.findViewById(R.id.fileSizeTextView);
            // 设置标签
            convertView.setTag(holder); // 使用setTag把查找的view缓存起来方便多次重用
        } else {
            // 获取标签
            holder = (ViewHolder) convertView.getTag();
        }
        // 获取文件名
        holder.fileNameTextView.setText(Util.convertString(list.get(position).getName(), "GBK"));  // UTF-8, GBK
        // 如果不是文件夹
        if (!list.get(position).isDirectory()) {
            // 获取显示文件图片
            holder.iconImageView.setImageBitmap(docIcon);
            // 获取文件大小
            holder.fileSizeTextView.setText(Util.getFormatSize(list.get(position).getSize()));
        } else {
            // 获取显示文件夹图片
            holder.iconImageView.setImageBitmap(folderIcon);
        }

        return convertView;
    }

    /**
     * 获取控件
     */
    private class ViewHolder {
        private ImageView iconImageView;
        private TextView fileNameTextView;
        private TextView fileSizeTextView;
    }
}
