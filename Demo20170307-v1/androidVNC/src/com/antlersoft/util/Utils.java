package com.antlersoft.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


/**
 * Created by tWX366549 on 2016/7/19.
 */
public class Utils {
    public static boolean isDebug = false;
    public static String FILE_PATH = "lgu+";
    private static String[] coms = {"ps | grep 'eth0' | awk '{print $2}'"};

    private static long lastClickTime;

    public static boolean isRoot() {
        return false;
    }

    public static synchronized boolean getRootAhth() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    public static ApplicationInfo getApplicationInfo(Context context) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return appInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAppExist(String packageName, Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String createEmergencyFileName(String account) {
        String name = account;
        if (account.contains(".")) {
            name = account.replace(".", "_");
        }
        if (name.contains("@")) {
            name = name.replace("@", "__");
        }
        return name;
    }


    public static String getDownloadDirPath() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        else {
            File apkFile = Environment.getDownloadCacheDirectory();
//            File apkFile = new File(Environment.getDownloadCacheDirectory() + FILE_PATH);
//            if (!apkFile.exists())
//                apkFile.mkdirs();
            chmod(apkFile.getAbsolutePath());
            return apkFile.getAbsolutePath();
        }
    }

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 300) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 修改目录或文件的读写权限
     *
     * @param path
     */
    public static void chmod(String path) {
        ShellUtil.runCommand("chmod 777 " + path);
    }

    public static void chmodSpecial(String path) {
        ShellUtil.runCommand("chmod 660 " + path);
    }

    public static void generateInstall(Context context, File file) {
        // 核心是下面几句代码
        chmod(file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }


    public static String silenceInstall(String apkPath) {
        String result = ShellUtil.runCommand("pm install -r " + apkPath);
//        ProcessBuilder processBuilder = new ProcessBuilder(args);
//        Process process = null;
//        InputStream errIs = null;
//        InputStream inIs = null;
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            int read = -1;
//            process = processBuilder.start();
//            errIs = process.getErrorStream();
//            while ((read = errIs.read()) != -1) {
//                baos.writeEveryThing(read);
//            }
//            baos.writeEveryThing('\n');
//            inIs = process.getInputStream();
//            while ((read = inIs.read()) != -1) {
//                baos.writeEveryThing(read);
//            }
//            byte[] data = baos.toByteArray();
//            result = new String(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (errIs != null) {
//                    errIs.close();
//                }
//                if (inIs != null) {
//                    inIs.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (process != null) {
//                process.destroy();
//            }
//        }
        return result;
    }

    public static boolean forceStopProcess(String packageName) throws Exception {
        DataOutputStream os = null;
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("am force-stop " + packageName + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            String line = "";
            while ((line = reader.readLine()) != null) {
                Log.i("huawei", line);
            }
            return true;
        } catch (Exception e) {
            final String msg = e.getMessage();
            Log.e("test", "runCommand error: " + msg);
            throw new Exception("kill app failed!");
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
//                if (process != null) {
//                    process.destroy();
//                }
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean killProcess(int pid) throws Exception {
        DataOutputStream os = null;
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("kill -9 " + pid + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            String line = "";
            while ((line = reader.readLine()) != null) {
                Log.i("huawei", line);
            }
            return true;
        } catch (Exception e) {
            final String msg = e.getMessage();
            Log.e("test", "runCommand error: " + msg);
            throw new Exception("kill app failed!");
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
//                if (process != null) {
//                    process.destroy();
//                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param content
     * @return
     */
    public static void writeCrashLog(String content) throws FileNotFoundException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "crash.txt"), true);
            outStream.write(content.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("huawei", "e = " + e.getMessage());
            throw new FileNotFoundException(e.getMessage());
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 将字符串写入文件
     *
     * @param content
     * @param file    SETTINGS.xml
     * @return
     */
    public static boolean write(String content, File file) throws FileNotFoundException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content.getBytes("UTF-8"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("huawei", "e = " + e.getMessage());
            throw new FileNotFoundException(e.getMessage());
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveFile(InputStream inputStream, File file) {
        DataInputStream dis = null;

        DataOutputStream dos = null;
        byte buffer[] = new byte[2048];
        int len = 0;
        try {
            dis = new DataInputStream(inputStream);
            dos = new DataOutputStream(new FileOutputStream(file));
            while ((len = dis.read(buffer)) != -1) {
                dos.write(buffer, 0, len);
            }
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                    dis = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                    dos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean write(FileInputStream inputStream, File originalFile) {
        FileChannel inCh = null;
        FileChannel outCh = null;
        try {
            inCh = inputStream.getChannel();
            outCh = new FileOutputStream(originalFile).getChannel();
            inCh.transferTo(0, inCh.size(), outCh);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inCh != null) {
                try {
                    inCh.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outCh != null) {
                try {
                    outCh.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    public static InputStream getLocalFile(Context context, int resId) {
        return context.getResources().openRawResource(resId);
    }



    public static ArrayList<String> getAccountName(Context context) throws Exception {
        ArrayList<String> localAccounts = new ArrayList<>();
        Properties pro = new Properties();
        InputStream is = context.getAssets().open("config");
        pro.load(is);
        Enumeration em = pro.propertyNames();
        while (em.hasMoreElements()) {
            String name = (String) em.nextElement();
            localAccounts.add(name);
        }
        return localAccounts;
    }

    public static void setEmergencyAccounts() {

    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> getEmergencyAccounts(Context context) throws Exception {
        Properties pro = new Properties();
        InputStream is = context.getAssets().open("config");
        pro.load(is);
        Enumeration<String> enu = (Enumeration<String>) pro.propertyNames();
        ArrayList<String> accountNames = new ArrayList<>();
        while (enu.hasMoreElements()) {
            accountNames.add(enu.nextElement());
        }
        return accountNames;
    }

    public static void write(String content, FileOutputStream outputStream) {
        FileOutputStream outStream = null;
        try {
            outputStream.write(content.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("huawei", "e = " + e.getMessage());
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void write(InputStream inputStream, FileOutputStream outputStream) {
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeEveryThing(FileInputStream inputStream, File originalFile) throws FileNotFoundException {
        FileChannel inCh = null;
        FileChannel outCh = null;
        try {
            inCh = inputStream.getChannel();
            outCh = new FileOutputStream(originalFile).getChannel();
            inCh.transferTo(0, inCh.size(), outCh);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileNotFoundException(e.getMessage());
        } finally {
            if (inCh != null) {
                try {
                    inCh.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outCh != null) {
                try {
                    outCh.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeEveryThing(FileInputStream inputStream, FileOutputStream outputStream) {
        FileChannel inCh = null;
        FileChannel outCh = null;
        try {
            inCh = inputStream.getChannel();
            outCh = outputStream.getChannel();
            inCh.transferTo(0, inCh.size(), outCh);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inCh != null) {
                try {
                    inCh.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outCh != null) {
                try {
                    outCh.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean hasSDCard() {
        boolean mHasSDcard = false;
        if (Environment.MEDIA_MOUNTED.endsWith(Environment.getExternalStorageState())) {
            mHasSDcard = true;
        } else {
            mHasSDcard = false;
        }

        return mHasSDcard;
    }

    public static String getSdcardPath() {

        if (hasSDCard()) return Environment.getExternalStorageDirectory().getAbsolutePath();

        return "/sdcard/";
    }

    public static void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String stringFromAssetsFile(Context context, String fileName) {
        AssetManager manager = context.getAssets();
        InputStream file;
        try {
            file = manager.open(fileName);
            byte[] data = new byte[file.available()];
            file.read(data);
            file.close();
            return new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 复制assets文件到指定目
     *
     * @param fileName 文件
     * @param filePath 目录
     */
    public static void copyAssets(Context context, String fileName, String filePath) {
        InputStream inputStream;
        try {
            inputStream = context.getResources().getAssets().open(fileName);// assets文件夹下的文�?
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream fileOutputStream =
                    new FileOutputStream(filePath + "/" + fileName);// 保存到本地的文件夹下的文�?
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean copy(File oldFile, File newFile) {
        if (!oldFile.exists()) {
            return false;
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(oldFile);
            outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[4096];
            while (inputStream.read(buffer) != -1) {
                outputStream.write(buffer);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean copy(FileInputStream inputStream, File newFile) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[4096];
            while (inputStream.read(buffer) != -1) {
                outputStream.write(buffer);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean exist(String url) {
        File file = new File(url);
        return file.exists();
    }


    /**
     * InputStrem 转byte[]
     *
     * @param in
     * @return
     * @throws Exception
     */
    public static byte[] readStreamToBytes(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 8];
        int length = -1;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
        byte[] result = out.toByteArray();
        in.close();
        out.close();
        return result;
    }

    /**
     * 写入文件
     *
     * @param in
     * @param file
     */
    public static void writeFile(InputStream in, File file) throws IOException {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (file != null && file.exists())
            file.delete();

        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 128];
        int len = -1;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        out.close();
        in.close();

    }

    /**
     * 得到Bitmap的byte
     *
     * @return
     * @author YOLANDA
     */
    public static byte[] bmpToByteArray(Bitmap bmp) {
        if (bmp == null)
            return null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 80, output);

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    /*
    * 根据view来生成bitmap图片，可用于截图功能
    */
    public static Bitmap getViewBitmap(View v) {

        v.clearFocus(); //

        v.setPressed(false); //
        // 能画缓存就返回false

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }

        v.buildDrawingCache();

        Bitmap cacheBitmap = v.getDrawingCache();

        if (cacheBitmap == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        // Restore the view

        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;

    }
}
