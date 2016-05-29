package com.whale.nangua.pumpkinfilemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.whale.nangua.pumpkinfilemanager.adapter.FileAdapter;
import com.whale.nangua.pumpkinfilemanager.utils.FileSortFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnCopyFileListener{
    private TextView showtv;
    private ListView lv;
    //菜单
    private Menu actionMenu;
    private ArrayList<File> data = new ArrayList<>();
    private File[] files;
    private FileAdapter fileAdapter;

    private String rootpath;
    private Stack<String> nowPathStack;

    private ProgressBar main_progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        main_progressbar = (ProgressBar) findViewById(R.id.main_progressbar);
        rootpath = Environment.getExternalStorageDirectory().toString();
        nowPathStack = new Stack<>();
        lv = (ListView) findViewById(R.id.lv);
        showtv = (TextView) findViewById(R.id.showtv);
        //获得本地文件信息列表，绑定到data
        files = Environment.getExternalStorageDirectory()
                .listFiles();
        //将根路径推入路径栈
        nowPathStack.push(rootpath);
        for (File f : files) {
            data.add(f);
        }
        showtv.setText(getPathString());
        fileAdapter = new FileAdapter(this, data);
        fileAdapter.setonCopyListner(this);
        lv.setAdapter(fileAdapter);

        lv.setOnItemClickListener(new FileItemClickListener());
    }

    @Override
    public void doCopy(File file) {
        watingCopyFile = file;
        Toast.makeText(MainActivity.this,file.getName() + "被添加到粘贴板",Toast.LENGTH_SHORT).show();
    }



    static File watingCopyFile;

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
                nowPathStack.push("/" + file.getName());
                showChangge(getPathString());
            }
        }
    }

    //显示改变data之后的文件数据列表
    private void showChangge(String path) {
        showtv.setText(path);
        files = new File(path).listFiles();
        data.clear();
        for (File f : files) {
            data.add(f);
        }
        files = fileAdapter.setfiledata(data);

    }

    //得到当前栈路径的String
    private String getPathString() {
        Stack<String> temp = new Stack<>();
        temp.addAll(nowPathStack);
        String result = "";
        while (temp.size() != 0) {
            result = temp.pop() + result;
        }
        return result;
    }
    long lastBackPressed = 0;
    @Override
    public void onBackPressed() {
        if (nowPathStack.peek() == rootpath) {
            //当前时间
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBackPressed < 2000) {
                super.onBackPressed();
            } else {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            }
            lastBackPressed = currentTime;
        } else {
            nowPathStack.pop();
            showChangge(getPathString());
        }

    }

    MenuItem searchItem;
    SearchView searchView;
    MenuItem sortItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        //得到搜索框
        searchItem = menu.findItem(R.id.action_search);
        sortItem = menu.findItem(R.id.action_sort);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_folder:
                doCreateNewFolder();
                break;
            case R.id.action_copy_paste:
                doPaste();
                break;
            case R.id.action_sort_date:
                fileAdapter.setSortWay(FileSortFactory.SORT_BY_FOLDER_AND_TIME);
                fileAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sort_size:
                fileAdapter.setSortWay(FileSortFactory.SORT_BY_FOLDER_AND_SIZE);
                fileAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sort_name:
                fileAdapter.setSortWay(FileSortFactory.SORT_BY_FOLDER_AND_NAME);
                fileAdapter.notifyDataSetChanged();
                break;
            default:
                files = fileAdapter.setfiledata();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //判断当前是复制还是粘贴的标识符
    private boolean iscopy = true;

    /**
     * 复制或粘贴
     */
    private void doPaste() {
        File newFile = new File(getPathString()+"/"+watingCopyFile.getName());
        if (watingCopyFile.equals(null)) {
            Snackbar.make(findViewById(R.id.main_view), "当前粘贴板为空，不能粘贴", Snackbar.LENGTH_SHORT).show();
        } else {
            main_progressbar.setVisibility(View.VISIBLE);
            if (watingCopyFile.isFile()&&watingCopyFile.exists()){
                try {
                    FileInputStream fis = new FileInputStream(watingCopyFile);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len = -1;
                    long contentSize = watingCopyFile.length();
                    long readed = 0;
                    byte[] buff = new byte[8192];
                    while ((len=fis.read(buff))!=-1){
                        //写文件
                        fos.write(buff,0,len);
                        readed+=len;
                        //发布进度
                    }
                    fos.flush();
                    fis.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    main_progressbar.setVisibility(View.INVISIBLE);
                }
            }
            if (newFile.exists()) {
                Toast.makeText(MainActivity.this,"复制" + newFile.getName() + "成功",Toast.LENGTH_SHORT).show();
                fileAdapter.notifyDataSetChanged();
            }
        }

    }


    AlertDialog mydialog;
    EditText newfloder_name;
    /**
     * 创建新文件夹
     */
    private void doCreateNewFolder() {
        mydialog = new AlertDialog.Builder(MainActivity.this).create();
        mydialog.show();
        mydialog.getWindow().setContentView(R.layout.newfloder_dialog);
        mydialog.setView(new EditText(MainActivity.this));
        //加入下面两句以后即可弹出输入法
        mydialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        newfloder_name = (EditText) mydialog.getWindow().findViewById(R.id.newfloder_name);

        mydialog.getWindow()
                .findViewById(R.id.newfloder_cancle)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mydialog.dismiss();
                    }
                });
        mydialog.getWindow()
                .findViewById(R.id.newfloder_create)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name =  newfloder_name.getText().toString();
                        if (name != null) {
                            File folder = new File(getPathString() + "/" + name);
                            folder.mkdirs();
                            if (folder.exists()) {
                                Toast.makeText(MainActivity.this,"文件："+name + " 创建成功",Toast.LENGTH_SHORT).show();
                                showChangge(getPathString());
                                mydialog.dismiss();
                            }
                        }

                    }
                });

    }

    boolean ifSearching = false;

    /**
     * 搜索
     * 搜索当前路径下文件夹内文件
     * 使用递归实现
     */
    private void doSearch(String query) {
        String  path = getPathString();
    }
}

