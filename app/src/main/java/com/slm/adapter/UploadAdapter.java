package com.slm.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.slm.ftp.R;
import com.slm.method.ThreadInfo;
import com.slm.upDown.UploadService;

import java.util.List;

/**
 * Created by Leaves on 2016/6/11.
 */
public class UploadAdapter extends BaseAdapter {

    private Context context;
    private List<ThreadInfo> list;

    private boolean viewControl = true;
    private boolean buttonControl = true;

    public UploadAdapter(Context context, List<ThreadInfo> list) {
        this.context = context;
        this.list = list;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        // 设置视图控件
        final ThreadInfo threadInfo = list.get(position);

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.upload_list, null);
            viewHolder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.tvFileName),
                    (ProgressBar) convertView.findViewById(R.id.pbProgress),
                    (TextView) convertView.findViewById(R.id.tvProgress),
                    (ImageButton) convertView.findViewById(R.id.btStart),
                    (ImageButton) convertView.findViewById(R.id.btStop),
                    (ImageButton) convertView.findViewById(R.id.btCancel)
            );
            convertView.setTag(viewHolder);

            final String fileName = threadInfo.getLocal_url().substring(threadInfo.getLocal_url().lastIndexOf("/") + 1);
            viewHolder.tvFileName.setText(fileName);
            viewHolder.pbProgress.setMax(100);

            final ViewHolder finalViewHolder = viewHolder;
            viewHolder.btStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 通知Service开始上载
                    Intent intent = new Intent(context, UploadService.class);
                    intent.setAction(UploadService.ACTION_START);
                    threadInfo.setRemark(position);
                    intent.putExtra("threadInfo", threadInfo);
                    context.startService(intent);
                    viewControl = true;
                    finalViewHolder.btStart.setEnabled(false);
                    finalViewHolder.btStop.setEnabled(true);
                    finalViewHolder.btCancel.setEnabled(false);
                    Toast.makeText(context, "开始上载", Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.btStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 通知Service上载暂停
                    Intent intent = new Intent(context, UploadService.class);
                    intent.setAction(UploadService.ACTION_STOP);
                    intent.putExtra("threadInfo", threadInfo);
                    context.startService(intent);
                    viewControl = false;
                    finalViewHolder.tvProgress.setText("暂停");
                    finalViewHolder.btStart.setEnabled(true);
                    finalViewHolder.btStop.setEnabled(false);
                    finalViewHolder.btCancel.setEnabled(true);
                    Toast.makeText(context, "上载暂停", Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 通知Service上载取消
                    Intent intent = new Intent(context, UploadService.class);
                    intent.setAction(UploadService.ACTION_CANCEL);
                    threadInfo.setRemark(position);
                    intent.putExtra("threadInfo", threadInfo);
                    context.startService(intent);
                    Toast.makeText(context, "上载任务关闭", Toast.LENGTH_SHORT).show();
                }
            });

            // 将viewHolder.tvFileName的Tag设为threadInfo的ID，用于唯一标识viewHolder.tvFileName
            viewHolder.tvFileName.setTag(Integer.valueOf(threadInfo.getThread_id()));
        }

        if (!buttonControl) {
            viewHolder.btStart.setEnabled(false);
            viewHolder.btCancel.setEnabled(false);
            buttonControl = true;
        }

        // 设置进度条
        int progress = (int) (threadInfo.getFinished() * 1.0 / threadInfo.getLength() * 100);
        viewHolder.pbProgress.setProgress(progress);
        viewHolder.tvProgress.setText(progress + " %");
        if (threadInfo.getFinished() == threadInfo.getLength()) {
            viewHolder.tvProgress.setText("完成");
            viewHolder.btStart.setEnabled(false);
            viewHolder.btStop.setEnabled(false);
            viewHolder.btCancel.setEnabled(true);
        }

        return convertView;
    }

    /**
     * 更新列表项中的进度条
     *
     * @param position
     */
    public void updateProgress(int position, int finished) {
        ThreadInfo threadInfo = list.get(position);
        threadInfo.setFinished(finished);
        buttonControl = false;
        if (viewControl) {
            // 强制调用getView来刷新每个Item的内容 数据量大时不建议用
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        TextView tvFileName;
        ProgressBar pbProgress;
        TextView tvProgress;
        ImageButton btStart;
        ImageButton btStop;
        ImageButton btCancel;

        public ViewHolder(TextView tvFileName, ProgressBar pbProgress, TextView tvProgress, ImageButton btStart, ImageButton btStop, ImageButton btCancel) {
            this.tvFileName = tvFileName;
            this.pbProgress = pbProgress;
            this.tvProgress = tvProgress;
            this.btStart = btStart;
            this.btStop = btStop;
            this.btCancel = btCancel;
        }
    }
}
