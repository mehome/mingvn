package android.player.detector;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by tWX366549 on 2017/2/15.
 */

public class NioSocketManager
        extends Thread {
    public static final String TAG = "NioSocketManager";

    /**
     * vncserver ip
     */
    private String host;

    /**
     * vncserver 横竖屏切换监听端口
     */
    private int port = 5911;

    private SocketChannel channel;
    private Selector selector;

    private boolean running = true;
    private NioSocketReadHandler socketReaderHandle;
    private Handler handler;

    public NioSocketManager(Handler handler) {
        this.handler = handler;
    }

    public void initial(String host, int port) {
        this.host = host;
        this.port = port;
        alwaysConnect();
    }

    /**
     * 连接服务器，3s连接一次直到连接成功
     */
    private void alwaysConnect() {
        while (running) {
            try {
                connectServer();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "connect server failed , server ip : " + host + ", port : " + port);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
            break;
        }
        Log.e(TAG, "connect server success!");
    }

    private void connectServer() throws IOException {
        //获得一个Socket通道
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        try {
            selector = Selector.open();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "selector open failed! error : " + e.getMessage());
        }
        //连接，当执行channel.finishConnect()才能完成连接
        channel.connect(new InetSocketAddress(host, port));
        //注册,先注册连接事件
        channel.register(selector, SelectionKey.OP_CONNECT);

        if (this.socketReaderHandle == null) {
            this.socketReaderHandle = new NioClientNioSocketReadHandler();
            socketReaderHandle.setHandler(handler);
        }

        while (running) {
            try {
                if (channel != null && channel.finishConnect()) {//完成连接
                    if (socketReaderHandle != null) {
                        socketReaderHandle.setChannel(channel);
                        socketReaderHandle.setSelector(selector);
                        socketReaderHandle.setConnected(true);
                    }
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    break;//退出连接循环
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (channel != null) {
                    channel.close();
                    channel = null;
                }

                if (selector != null) {
                    selector.close();
                    selector = null;
                }
                throw new IOException(e);
            }
            continue;
        }
    }

    public void listen() throws Exception {
        //轮询Selector
        while (true) {
            synchronized (selector) {
                selector.select();
            }

            if (socketReaderHandle == null || !socketReaderHandle.isConnected()) {
                Log.e(TAG, "channel close, listen quit!");
                break;
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();//删除已经选中的key
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    socketReaderHandle.setChannel(channel);
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    socketReaderHandle.handleReadEvent();
                } else {
                    socketReaderHandle.handleWriteEvent();
                }
            }
        }
    }


    @Override
    public void run() {
        while (running) {
            try {
                listen();//死循环
            } catch (Exception e) {
                e.printStackTrace();
            }

            initial(host, port);
        }
    }

    public boolean send(int type, String sendMsg) {
        try {
            if (socketReaderHandle == null) {
                return false;
            }
            return socketReaderHandle.send(type, sendMsg);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        Log.e(TAG, TAG + "_close");
        try {
            running = false;
            if (channel.isOpen()) {
                channel.socket().close();
                channel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (selector != null) {
                selector.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
