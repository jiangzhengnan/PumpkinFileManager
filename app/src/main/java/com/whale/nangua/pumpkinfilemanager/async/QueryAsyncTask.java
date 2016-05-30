package com.whale.nangua.pumpkinfilemanager.async;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.whale.nangua.pumpkinfilemanager.adapter.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nangua on 2016/5/30.
 */
public class QueryAsyncTask extends AsyncTask<Integer,Integer,ArrayList<File>> {

    TextView tv;
    String path;
    HashMap<File, String> searchfilemap;
    String query;
    FileAdapter fileAdapter;
    AlertDialog dialog;
    public QueryAsyncTask(TextView tv,String path,String query,FileAdapter fileAdapter,AlertDialog dialog) {
        this.tv = tv;
        this.path = path;
        this.query = query;
        this.fileAdapter = fileAdapter;
        this.dialog = dialog;
    }

    ArrayList<File> data;
    @Override
    protected ArrayList<File> doInBackground(Integer... params) {
        int total=0;
        data = new ArrayList<>();
        searchfilemap = new HashMap<>();

          searchByPath(path);

        if (searchfilemap.size() > 0) {
            //取出map中数据，赋值给data
            Object[] list = searchfilemap.entrySet().toArray();
            for (int i = 0; i < searchfilemap.size(); i++) {
                data.add(new File(list[i].toString()));
            }
        }
        return data;
    }

    @Override
    protected void onPostExecute(ArrayList<File> data) {
        tv.setText("扫描完成，共扫描文件：" + filenum + "个");
        fileAdapter.setfiledata(data);
        //
        fileAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int v = values[0];
        tv.setText("已扫描文件数：" + v);
        fileAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPreExecute() {
        tv.setText("开始扫描当前目录下文件");
    }
    int filenum = 0;
    int totalnum = 0;
    private void searchByPath(String path) {
        File[] files = new File(path).listFiles();
        filenum += files.length;
        publishProgress(filenum);
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                searchByPath(path + "/" + f.getName());
            } else {
                if (f.getName().contains(query)) {
                    searchfilemap.put(files[i], files[i].getName());
                }
            }
        }

    }
}
