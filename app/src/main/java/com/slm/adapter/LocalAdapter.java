package com.slm.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

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

/**
 * 本地列表适配器
 * @author cui_tao
 */
public class LocalAdapter extends BaseAdapter {
    /**
     * 布局.
     */
    private LayoutInflater inflater;

    /**
     * 根目录
     */
    private Bitmap back01;

    /**
     * 上一级目录
     */
    private Bitmap back02;

    /**
     * 文件夹显示图片.
     */
    private Bitmap icon1;

    /**
     * 文件显示图片.
     */
    private Bitmap icon2;

    /**
     * 本地文件列表.
     */
    private List<File> list;

    /**
     * 构造函数.
     * @param context 当前环境
     * @param li 本地文件列表
     */
    public LocalAdapter(Context context, List<File> li) {
        this.inflater = LayoutInflater.from(context);
        this.list = li;
        // 根目录文件夹显示图片
        this.back01 = BitmapFactory.decodeResource(context.getResources(), R.drawable.back01);
        // 上一级目录文件夹显示图片
        this.back02 = BitmapFactory.decodeResource(context.getResources(), R.drawable.back02);
        // 文件夹显示图片
        this.icon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
        // 文件显示图片
        this.icon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.doc);
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
            convertView = inflater.inflate(R.layout.file_list, null);

            // 获取控件实例
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.fileNameTextView = (TextView) convertView.findViewById(R.id.fileNameTextView);
            holder.fileSizeTextView = (TextView) convertView.findViewById(R.id.fileSizeTextView);

            // 设置标签
            convertView.setTag(holder);
        } else {
            // 获取标签
            holder = (ViewHolder) convertView.getTag();
        }
        // 获取文件
        File file = list.get(position);
        // 判断是否为一个目录
        if (!file.isDirectory()) {
            try {
                // 创建输入流
                FileInputStream inputStream = new FileInputStream(file);
                // 获得流大小
                double size = (double) inputStream.available() / 1;
                // 获取文件大小
                holder.fileSizeTextView.setText(Util.getFormatSize(size));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 获取文件名
        holder.fileNameTextView.setText(file.getName());
        if (file.isDirectory()) {
            // 获取显示文件夹图片
            holder.iconImageView.setImageBitmap(icon1);
//            // 耗时操作
//            if (file.getName().equals(".")) {
//                holder.iconImageView.setImageBitmap(back01);
//            } else if (file.getName().equals("..")) {
//                holder.iconImageView.setImageBitmap(back02);
//            }
        } else {
            // 获取显示文件图片
            holder.iconImageView.setImageBitmap(icon2);
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
