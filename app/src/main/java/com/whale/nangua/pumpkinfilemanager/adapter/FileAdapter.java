package com.whale.nangua.pumpkinfilemanager.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.whale.nangua.pumpkinfilemanager.R;
import com.whale.nangua.pumpkinfilemanager.view.RenameFileDialog;
import com.whale.nangua.pumpkinfilemanager.utils.FileSortFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by nangua on 2016/5/27.
 */
public class FileAdapter extends BaseAdapter {
    ArrayList<File> filedata;
    Context context;
    //排序方法
    int sortWay = FileSortFactory.SORT_BY_FOLDER_AND_NAME;


    public void setSortWay(int sortWay) {
        this.sortWay = sortWay;
    }




    public FileAdapter(Context context, ArrayList<File> data) {
        this.context = context;
        this.filedata = data;
        fileItemListener = new FileListItemListender();
    }

    public File[]  setfiledata(ArrayList<File> data) {
        this.filedata = data;
        sort();
        this.notifyDataSetChanged();
        File[] files = new File[filedata.size()];
        for (int i = 0;i<files.length;i++) {
            files[i] = filedata.get(i);
        }
        return files;
    }

    public File[]  setfiledata() {
        File[] files = new File[filedata.size()];
        for (int i = 0;i<files.length;i++) {
            files[i] = filedata.get(i);
        }
        return files;
    }

    /**
     * 将文件列表排序
     */
    private void sort() {
        Collections.sort(this.filedata, FileSortFactory.getWebFileQueryMethod(sortWay));
    }

    @Override
    public void notifyDataSetChanged() {
        //重新排序
        sort();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filedata.size();
    }

    @Override
    public Object getItem(int position) {
        return filedata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * ibButton点击监听器
     */
    FileListItemListender fileItemListener;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = filedata.get(position);
        fileItemListener = new FileListItemListender();
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView =LayoutInflater.from(context).inflate(R.layout.list_file_cell, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (file.isDirectory()) {
            viewHolder.fileImage.setImageResource(R.drawable.folder);
            viewHolder.fileSize.setText("文件夹");
        } else {
            viewHolder.fileImage.setImageResource(R.drawable.file);
            viewHolder.fileSize.setText(generateSize(file));
        }
        //将position与ibMore绑定
        viewHolder.filemore.setTag(position);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        viewHolder.fileName.setText(file.getName());
        viewHolder.fileTime.setText(format.format(new Date(file.lastModified())));
        viewHolder.filemore.setOnClickListener(fileItemListener);
        return convertView;
    }

    public static String generateSize(File file) {
        if (file.isFile()) {
            long result = file.length();
            long gb = 2 << 29;
            long mb = 2 << 19;
            long kb = 2 << 9;
            if (result < kb) {
                return result + "B";
            } else if (result >= kb && result < mb) {
                    return String.format("%.2fKB", result / (double) kb);
            } else if (result >= mb && result < gb) {
                return String.format("%.2fMB", result / (double) mb);
            } else if (result >= gb) {
                return String.format("%.2fGB", result / (double) gb);
            }
        }
        return null;
    }

    public static class ViewHolder {
        ImageView fileImage;
        TextView fileName;
        TextView fileSize;
        TextView fileTime;
        ImageButton filemore;
        public ViewHolder(View v) {
            fileImage = (ImageView) v.findViewById(R.id.file_image);
            fileName = (TextView) v.findViewById(R.id.file_name);
            fileSize = (TextView) v.findViewById(R.id.file_size);
            fileTime = (TextView) v.findViewById(R.id.file_time);
            filemore = (ImageButton) v.findViewById(R.id.file_more);
        }
    }
    /**
    * ibMore被点击的监听器
    * 点击的时候图标旋转并弹出menu,根据点击的view获取其绑定的position,之后再在file集合中操作数据
    */
    public class FileListItemListender implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        Integer position;


        @Override
        public void onClick(final View v) {
            //获取view中绑定的tag
            position = (Integer) v.getTag();
            //创建菜单
            PopupMenu popupMenu = new PopupMenu(context, v);
            //加载布局
            popupMenu.inflate(R.menu.file_list_popup_menu);
            //设置消失时的监听器
            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    //旋转动画(消失的时候会显示旋转动画)
                    RotateAnimation rotateAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    //设置动画时间300ms
                    rotateAnimation.setDuration(200);
                    //设置动画保留状态
                    rotateAnimation.setFillAfter(true);
                    v.startAnimation(rotateAnimation);
                }
            });
            popupMenu.setOnMenuItemClickListener(this);
            if (filedata.get(position).isDirectory()){
                popupMenu.getMenu().findItem(R.id.more_copy).setVisible(false);
            }else {
                popupMenu.getMenu().findItem(R.id.more_copy).setVisible(true);

            }
            RotateAnimation rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            v.startAnimation(rotateAnimation);
            popupMenu.show();

        }

        /**
         * 菜单项点击
         *
         * @param item
         * @return true 事件处理完毕  false
         */
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.more_rename:
                    doRename();
                    break;
                case R.id.more_copy:
                    doCopy();
                    break;
                case R.id.more_remove:
                    doRemove();
                    break;
                default:
                    break;
            }
            return true;
        }

        /**
         * 删除
         */
        private void doRemove() {
            final File file = filedata.get(position);
             judgeAlertDialog(context, "提醒", "你确认删除" + file.getName() + "吗(不可逆)?", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     deleteDir(file);
                     filedata.remove(file);
                     notifyDataSetChanged();
                     showToast(file.getName() + " 删除成功");
                 }
             }, null);
        }

        /**
         * 显示消息
         *
         * @param message
         */
        private void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        /**
         * 复制
         */
        private void doCopy() {
            if (onCopyFileListener!=null) {
                onCopyFileListener.doCopy(filedata.get(position));
            }
        }



        /**
         * 重命名
         */
        private void doRename() {
            showToast("重命名" + position);
            RenameFileDialog dialog = new RenameFileDialog(context, filedata, position);
            dialog.setOnFileRenameListener(new RenameFileDialog.OnFileRenameListener() {
                @Override
                public void onFileRenamed(boolean success) {
                    String message = null;
                    if (filedata.get(position).isFile()) {
                        message = "文件";
                    } else {
                        message = "文件夹";
                    }
                    if (success) {
                        message += "重命名成功";
                    } else {
                        message += "重命名失败";

                    }
                    showToast(message);
                }
            });
            dialog.show();
            setfiledata(filedata);
        }
    }

    /**
     * 判断对话框
     * @param context
     * @param title
     * @param message
     * @param okListener
     * @param cancleListener
     * @return
     */
    public static AlertDialog judgeAlertDialog(Context context, String title,
                                               String message, DialogInterface.OnClickListener okListener,
                                               DialogInterface.OnClickListener cancleListener) {
        AlertDialog aDialog = new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setNegativeButton("确定", okListener)
                .setPositiveButton("取消", cancleListener).show();
        return aDialog;
    }

    /**
     * 删除文件或文件夹
     * @param file
     */
    public static void deleteDir(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    public interface OnCopyFileListener{
         void doCopy(File file);
    }

    OnCopyFileListener onCopyFileListener;

    public void setonCopyListner(OnCopyFileListener onCopyFileListener){
        this.onCopyFileListener = onCopyFileListener;
    };
}
