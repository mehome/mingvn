package android.androidVNC;

import android.app.Application;
import android.util.Log;

/**
 * Created by tWX366549 on 2017/1/16.
 */
public class VNCApp extends Application {
    public static String PHONE_MODEL;

    @Override
    public void onCreate() {
        super.onCreate();
        PHONE_MODEL = android.os.Build.MODEL; // 手机型号
        Log.i("VNCApp","VNCApp_onCreate");
//        CatchHandler.getInstance().init(getApplicationContext());


    }

}
