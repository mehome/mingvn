package android.player.detector;

import android.os.Handler;

/**
 * Created by tWX366549 on 2017/2/15.
 */

public class VNCServerOrientationDetector {
    public static final String TAG = "Detector";

    private String host;
    private int port;

    private NioSocketManager mNioSocketManager;
    private SocketClient mSocketClient;

    private Handler handler;

    private static VNCServerOrientationDetector instance;

    private VNCServerOrientationDetector() {
    }

    public synchronized static VNCServerOrientationDetector getInstance() {
        if (instance == null) {
            instance = new VNCServerOrientationDetector();
        }

        return instance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void init(String host, int port) {
        this.host = host;
        this.port = port;
//        initScoket();
        initSocketClient(host, port);
    }

    private void initScoket() {
//        closeClientSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocketClient = new SocketClient(handler);
                    mSocketClient.init(host, port);
                    mSocketClient.start();
                    mSocketClient.read();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initSocketClient(final String host, final int port) {
        closeClientSocket();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mNioSocketManager = new NioSocketManager(handler);
                    mNioSocketManager.initial(host, port);
                    mNioSocketManager.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void closeClientSocket() {
        if (mNioSocketManager != null) {
            mNioSocketManager.close();
        }
//        if (mSocketClient != null) {
//            mSocketClient.close();
//        }
    }


}
