package com.whale.nangua.pumpkinfilemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView showtv;
    ListView lv;
    //菜单
    Menu actionMenu;
    ArrayList<File> data = new ArrayList<>();
    ArrayAdapter<String> adapter;
    File[] files;
    FileAdapter fileAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv);
        //获得本地文件信息列表，绑定到data
        files = Environment.getExternalStorageDirectory()
                .listFiles();
        for (File f : files) {
            data.add(f);
        }

        fileAdapter = new FileAdapter(this,data);
        lv.setAdapter(fileAdapter);
        lv.setOnItemClickListener(new FileItemClickListener());
    }

    class FileItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(
                AdapterView<?> parent,
                View view,
                int position,
                long id) {

            File file = files[position];
            if (file.isFile()) {
                // 打开
                Intent intent = new Intent();
                // 打开、显示
                intent.setAction(Intent.ACTION_VIEW);
                // /mnt/sdcard/abc.jpg
                // file:///mnt/sdcard/abc.def.jpg
                Uri data = Uri.fromFile(file);
                int index = file.getName().lastIndexOf(".");
                String suffix = file.getName().substring(index + 1);
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
                intent.setDataAndType(data, type);
                startActivity(intent);
            } else {
                //如果是文件夹
                // 清除列表数据
                // 获得目录中的内容，计入列表中
                // 适配器通知数据集改变
                //
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        actionMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                doSearch();
                break;
            case R.id.action_new_folder:
                doCreateNewFolder();
                break;
            case R.id.action_paste:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doCreateNewFolder() {

    }

    private void doSearch() {

    }
}

