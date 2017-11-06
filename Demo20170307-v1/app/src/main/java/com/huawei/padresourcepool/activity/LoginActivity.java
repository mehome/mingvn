package com.huawei.padresourcepool.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.padresourcepool.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{



    private EditText et_name, et_pass;
    private Button bt_login;
    private CheckBox cb_auto_login;
    private CheckBox cb_remeber_pwd;

    private Button bt_username_clear;
    private Button bt_pwd_clear;

    private TextWatcher username_watcher;
    private TextWatcher password_watcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        et_name = (EditText) findViewById(R.id.username);
        et_pass = (EditText) findViewById(R.id.password);

        bt_username_clear = (Button)findViewById(R.id.bt_username_clear);
        bt_pwd_clear = (Button)findViewById(R.id.bt_pwd_clear);

        bt_username_clear.setOnClickListener(this);
        bt_pwd_clear.setOnClickListener(this);


        //监听EditText的变化
        initWatcher();
        et_name.addTextChangedListener(username_watcher);
        et_pass.addTextChangedListener(password_watcher);

        bt_login = (Button) findViewById(R.id.login);
        bt_login.setOnClickListener(this);

        cb_auto_login  = (CheckBox) findViewById(R.id.auto_login);
        cb_remeber_pwd    = (CheckBox) findViewById(R.id.remeber_pwd);

    }


    private void initWatcher() {
        username_watcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            public void afterTextChanged(Editable s) {
                et_pass.setText("");
                if(s.toString().length()>0){
                    bt_username_clear.setVisibility(View.VISIBLE);
                }else{
                    bt_username_clear.setVisibility(View.INVISIBLE);
                }
            }
        };

        password_watcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    bt_pwd_clear.setVisibility(View.VISIBLE);
                }else{
                    bt_pwd_clear.setVisibility(View.INVISIBLE);
                }
            }
        };
    }



    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.login:  //登陆

                login();

                //校验登录信息

                //是否自动登录和记住密码

                Intent intent = new Intent(LoginActivity.this, DetailListActivityBackup1.class);
                startActivity(intent);


                //获取屏幕分辨率
                init();

                //获取手机真实分辨率
                getRealScreenSize();

                /*String result = execRootCmd("dumpsys window displays |grep cur");

                int cur = result.indexOf("cur");
                int app = result.indexOf("app");
                String size = result.substring(cur + 4, app).trim();

                Log.d("SettingActivity","result = "+size);*/

                break;

            case R.id.bt_username_clear:
                et_name.setText("");
                et_pass.setText("");
                break;
            case R.id.bt_pwd_clear:
                et_pass.setText("");
                break;

        }
    }

    /**
     * 登陆
     */
    private void login() {

    }

    private void init() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;
        int screenHeight = metric.heightPixels;

        Log.d("SettingActivity","width = "+screenWidth+"---height="+screenHeight);
    }

    private void getScreen(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Toast.makeText(getApplicationContext(), "手机屏幕分辨率："+width+"*"+height, Toast.LENGTH_LONG).show();
    }


    //version >= 4.2.2 有效
    public void getRealScreenSize() {
        DisplayMetrics metrics =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Log.d("SettingActivity","realWidth = "+width+"---realHeight="+height);
    }


    //执行shell命令方法：
    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                result += line+"\r\n";
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    //应用获取root权限
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

}

