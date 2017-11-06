package com.huawei.padresourcepool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.huawei.padresourcepool.activity.LoginActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button skip_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        skip_login = (Button) findViewById(R.id.skip_login);
        skip_login.setOnClickListener(this);

        startActivity(new Intent(MainActivity.this, LoginActivity.class));

    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()){
            case R.id.skip_login:
                 intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);

                break;

        }
    }
}
