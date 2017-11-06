package com.antlersoft.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;

public class CatchHandler implements UncaughtExceptionHandler {

    private String content;

    private CatchHandler() {
    }

    public static CatchHandler getInstance() {

        return mCatchHandler;
    }

    private static CatchHandler mCatchHandler = new CatchHandler();

    private Context mContext;


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (thread.getName().equals("main")) {
            ToastException(thread, ex);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } else {
            handleException(thread, ex);
        }

    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void ToastException(final Thread thread, final Throwable ex) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                StringBuilder builder = new StringBuilder();
                builder.append("\n----------" + DateUtils.getDate(System.currentTimeMillis()) + "----------\n");
                builder.append("At thread: ").append(thread.getName())
                        .append("\n");
                builder.append("Exception is :\n").append(ex.getMessage());
//                Toast.makeText(mContext, builder.toString(), Toast.LENGTH_LONG)
//                        .show();
                content = builder.toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.writeCrashLog(content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Log.e("davy", "exe : " + content);
                Looper.loop();
            }
        }.start();
    }

    private void handleException(final Thread thread, final Throwable ex) {
        new AlertDialog.Builder(mContext).setMessage("ex").show();
    }
}
