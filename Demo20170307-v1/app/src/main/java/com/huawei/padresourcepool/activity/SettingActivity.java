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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.padresourcepool.R;
import com.huawei.padresourcepool.adapter.DetailListAdapter;
import com.huawei.padresourcepool.bean.DetailListBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class SettingActivity extends AppCompatActivity {

    private Button connect;
    private EditText vnc_ip;
    private EditText video_url;
    private VncDatabase database;
    private ConnectionBean connectionBean;
    List<IjkOptions> ijkOptions;
    LinearLayout opt_layout;
    EditText opt_name;
    EditText opt_value;
    RadioGroup opt_category;
    RadioGroup codecType;
    SharedPreferences prf;
    private int category = -1;
    private StringBuilder stringBuilder;
    private Settings settings;
    private int codec;
    private DetailListBean detailListBean;
    private SharedPreferences config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();

        prf = getSharedPreferences("opt", MODE_PRIVATE);
        database = new VncDatabase(this);
        connectionBean = new ConnectionBean();
        ijkOptions = new ArrayList<>();
        stringBuilder = new StringBuilder();
        settings = new Settings(this);
        initViews();
        initOpts();
    }

    private void init() {
        Intent intent = getIntent();
        detailListBean = (DetailListBean) intent.getSerializableExtra(DetailListAdapter.PLAY_PARAMES_INFO);
    }

    private void initOpts() {
        String first = prf.getString("first", "true");
        if (prf.getString("first", "true").equals("true")) {
            prf.edit().putString("framedrop", "60," + IjkMediaPlayer.OPT_CATEGORY_PLAYER)
                    .putString("skip_loop_filter", "0," + IjkMediaPlayer.OPT_CATEGORY_CODEC)
                    .putString("fflags", "nobuffer," + IjkMediaPlayer.OPT_CATEGORY_FORMAT)
                    .putString("mediacodec_mpeg4", "1," + IjkMediaPlayer.OPT_CATEGORY_PLAYER)
                    .putString("start-on-prepared", "1," + IjkMediaPlayer.OPT_CATEGORY_PLAYER)
                    .putString("analyzeduration", "2000000," + IjkMediaPlayer.OPT_CATEGORY_FORMAT)
                    .putString("rtsp_transport", "tcp," + IjkMediaPlayer.OPT_CATEGORY_FORMAT)
                    .commit();
//            config.edit().putBoolean("first", false).apply();
            settings.useMediaCodec(false);
            prf.edit().putString("first", "false").apply();
        }
        show();
    }


    private void show() {
        Map<String, String> opts = (Map<String, String>) prf.getAll();
        for (Map.Entry<String, String> entry : opts.entrySet()) {
            try {
                System.out.println("key= " + entry.getKey() + " and value= "
                        + entry.getValue());
                String name = entry.getKey();
                if (name.equals("first") || name.equals("vnc_ip") || name.equals("url"))
                    continue;
                String value = entry.getValue();
                Log.i("huawei", "value=" + value);
                String[] cat = value.split(",");
                Log.i("huawei", "category=" + cat);
                createView(Integer.parseInt(cat[1]), name, cat[0]);
                Log.e("huawei", "category=" + cat[1] + ", name=" + name + ", value=" + cat[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        vnc_ip = (EditText) findViewById(R.id.vnc_ip);
        video_url = (EditText) findViewById(R.id.video_url);
        opt_layout = (LinearLayout) findViewById(R.id.show_opt_layout);
        opt_name = (EditText) findViewById(R.id.opt_name);
        opt_value = (EditText) findViewById(R.id.opt_value);
        opt_category = (RadioGroup) findViewById(R.id.opt_category);
        codecType = (RadioGroup) findViewById(R.id.codec);
        RadioButton hw = (RadioButton) findViewById(R.id.hw);
        RadioButton sw = (RadioButton) findViewById(R.id.sw);

        if (settings.getUsingMediaCodec()) {
            hw.setChecked(true);
        } else {
            sw.setChecked(true);
        }

        codecType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.hw:
                        codec = 1;
                        settings.useMediaCodec(true);
                        break;
                    case R.id.sw:
                        settings.useMediaCodec(false);
                        codec = 0;
                        break;
                }
            }
        });
        opt_category.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.player_category:
                        category = IjkMediaPlayer.OPT_CATEGORY_PLAYER;
                        break;
                    case R.id.format_category:
                        category = IjkMediaPlayer.OPT_CATEGORY_FORMAT;
                        break;
                    case R.id.codec_category:
                        category = IjkMediaPlayer.OPT_CATEGORY_CODEC;
                        break;
                }
            }
        });

        //回显操作
        config = getSharedPreferences("config", MODE_PRIVATE);
        String ip = config.getString("vnc_ip"+detailListBean.position, detailListBean.vnc);
        String port = config.getString("vnc_port"+detailListBean.position, detailListBean.port);
        String url = config.getString("url"+detailListBean.position, detailListBean.url);

        vnc_ip.setText(ip);
        video_url.setText(url);
    }

    public void reset(View view) {
        prf.edit().clear().commit();
        prf.edit().putString("framedrop", "60," + IjkMediaPlayer.OPT_CATEGORY_PLAYER)
                .putString("skip_loop_filter", "0," + IjkMediaPlayer.OPT_CATEGORY_CODEC)
                .putString("fflags", "nobuffer," + IjkMediaPlayer.OPT_CATEGORY_FORMAT)
                .putString("mediacodec_mpeg4", "1," + IjkMediaPlayer.OPT_CATEGORY_PLAYER)
                .putString("start-on-prepared", "1," + IjkMediaPlayer.OPT_CATEGORY_PLAYER)
                .putString("analyzeduration", "2000000," + IjkMediaPlayer.OPT_CATEGORY_FORMAT)
                .putString("rtsp_transport", "tcp," + IjkMediaPlayer.OPT_CATEGORY_FORMAT)
                .commit();
        opt_layout.removeAllViews();
        show();
    }

    public void addOpts(View view) {
        if (category == -1) {
            Toast.makeText(this, "没选Category！", Toast.LENGTH_LONG).show();
            return;
        }

        String name = opt_name.getText().toString().trim();
        String value = opt_value.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "参数不能空！", Toast.LENGTH_SHORT).show();
            return;
        }

//        StringBuilder builder = new StringBuilder();
//        builder.append(category).append("/").append(name).append("/").append(value);
//        String optStr = builder.toString();
//        stringBuilder.append(optStr).append(",");

        if (TextUtils.isEmpty(value)) {
            prf.edit().remove(name).apply();
            View childView = opt_layout.findViewWithTag(name);
            if (childView != null)
                opt_layout.removeView(childView);
            return;
        }
        createView(category, name, value);
        prf.edit().putString(name, value + "|" + category).apply();
    }

    private void createView(int category, String name, String value) {
        TextView optTv = (TextView) opt_layout.findViewWithTag(name);
        if (optTv == null) {
            optTv = new TextView(this);
            optTv.setTextSize(14);
            optTv.setTag(name);
            opt_layout.addView(optTv);
        }
        optTv.setText(category + " | " + name + " = " + value);
    }

    public void connect(View view) {
        String ip = vnc_ip.getText().toString();
        String url = video_url.getText().toString();

        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(url)) {
            Toast.makeText(this, "参数不能为空", Toast.LENGTH_SHORT).show();
        } else {
            startVnc(ip, "5901", url);
        }
    }

    private void startVnc(String vnc_ip, String vnc_port, String url) {
        prf.edit().putString("vnc_ip", vnc_ip).apply();
        prf.edit().putString("url", url).apply();

        //保存修改的设置
        config.edit().putString("vnc_ip"+detailListBean.position, vnc_ip).apply();
        config.edit().putString("vnc_port"+detailListBean.position, vnc_port).apply();
        config.edit().putString("url"+detailListBean.position, url).apply();


//        String optStr = stringBuilder.toString();
//        config.edit().putString("opt", optStr.substring(0, optStr.lastIndexOf(","))).apply();
        connectionBean.setAddress(vnc_ip);
        connectionBean.setForceFull(BitmapImplHint.AUTO);
        connectionBean.setPort(Integer.parseInt(vnc_port));
        saveAndWriteRecent();
        Intent intent = new Intent(this, VncCanvasActivity.class);
        intent.putExtra(VncConstants.CONNECTION, connectionBean.Gen_getValues());
        intent.putExtra("videoPath", url);
        intent.putExtra("proto", 1);
        startActivity(intent);
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
