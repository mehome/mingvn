package com.huawei.padresourcepool.activity;

import android.androidVNC.BitmapImplHint;
import android.androidVNC.ConnectionBean;
import android.androidVNC.VncCanvasActivity;
import android.androidVNC.VncConstants;
import android.androidVNC.VncDatabase;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.player.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.padresourcepool.R;
import com.huawei.padresourcepool.adapter.DetailListAdapter;
import com.huawei.padresourcepool.bean.DetailListBean;


public class DetailMessageActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "DetailMessageActivity";

    private Button btn_start_play;
    private EditText et_vnc_ip;
    private EditText et_vnc_port;
    private EditText et_url;
    private VncDatabase database;
    private ConnectionBean connectionBean;
    SharedPreferences config;
    private ImageView iv_icon;
    private DetailListBean detailListBean;
    private TextView tv_title;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_message);

        init();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        config = getSharedPreferences("config", MODE_PRIVATE);
        database = new VncDatabase(this);
        connectionBean = new ConnectionBean();

        settings = new Settings(this);
        initView();
        initData();
        initEvent();

    }

    private void init() {
        Intent intent = getIntent();
        detailListBean = (DetailListBean) intent.getSerializableExtra(DetailListAdapter.PLAY_PARAMES_INFO);

    }

    private void initEvent() {
        btn_start_play.setOnClickListener(this);
    }

    private void initData() {

    }

    private void initView() {
        btn_start_play = (Button) findViewById(R.id.btn_start_play);

        tv_title = (TextView) findViewById(R.id.tv_title);

        et_vnc_ip = (EditText) findViewById(R.id.et_vnc_ip);
        et_vnc_port = (EditText) findViewById(R.id.et_vnc_port);
        et_url = (EditText) findViewById(R.id.et_url);

        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_title.setText(detailListBean.title);


        /*et_vnc_ip.setText(config.getString("vnc_ip", detailListBean.vnc));
        et_vnc_port.setText(config.getString("vnc_port", "5901"));
        et_url.setText(config.getString("url", detailListBean.url));*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item1:
                //Toast.makeText(this,"点击了item1",Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_play:

                String vnc_ip = config.getString("vnc_ip"+detailListBean.position, detailListBean.vnc);
                String vnc_port = config.getString("vnc_port"+detailListBean.position, detailListBean.port);
                String url = config.getString("url"+detailListBean.position, detailListBean.url);

                Log.d(TAG, "vnc_ip: "+detailListBean.vnc);
                Log.d(TAG, "url: "+detailListBean.url);


                startVnc(vnc_ip, vnc_port, url);

     /*           String vnc_ip = et_vnc_ip.getText().toString();
                String vnc_port = et_vnc_port.getText().toString();
                String url = et_url.getText().toString();
                if (TextUtils.isEmpty(vnc_ip) || TextUtils.isEmpty(vnc_port) || TextUtils.isEmpty(url)) {
                    Toast.makeText(DetailMessageActivity.this, "参数不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    startVnc(vnc_ip, vnc_port, url);
//                    PlayParamesInfo playParamesInfo = new PlayParamesInfo(vnc_ip, vnc_port, url);
//                    Intent intent = new Intent(DetailMessageActivity.this, DetailContentActivity.class);
//                    intent.putExtra(PLAY_PARAMES_INFO, playParamesInfo);
//                    startActivity(intent);
                }*/


                break;
        }
    }

    private void startVnc(String vnc_ip, String vnc_port, String url) {
        /*config.edit().putString("vnc_ip", vnc_ip).apply();
        config.edit().putString("vnc_port", vnc_port).apply();
        config.edit().putString("url", url).apply();*/

        connectionBean.setAddress(vnc_ip);
        connectionBean.setForceFull(BitmapImplHint.AUTO);
        connectionBean.setPort(Integer.parseInt(vnc_port));
        saveAndWriteRecent();
        Intent intent = new Intent(this, VncCanvasActivity.class);
        intent.putExtra(VncConstants.CONNECTION, connectionBean.Gen_getValues());
        intent.putExtra("videoPath", url);
        intent.putExtra("proto", 2);
        startActivity(intent);
//        Intent intent=new Intent(DetailMessageActivity.this, veg.mediaplayer.sdk.teststreamcontrol.MainActivity.class);
//        DetailMessageActivity.this.startActivity(intent);
    }

    private void saveAndWriteRecent() {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            connectionBean.save(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
