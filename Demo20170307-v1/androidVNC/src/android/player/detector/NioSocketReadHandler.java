package android.player.detector;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by tWX366549 on 2017/2/15.
 */

public abstract class NioSocketReadHandler {

    public static final String TAG = "NioSocketReadHandler";

    private SocketChannel channel = null;

    private Selector selector = null;

    private boolean connected = true;

    private Handler handler;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connect) {
        this.connected = connect;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public void handleReadEvent() {
        Log.e(TAG, "handleReadEvent");
        try {
            readData();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "receive data from server failed");
        }
    }

    private void readData() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bufferSize = channel.read(buffer);

        Log.i(TAG, "bufferSize 1 :" + bufferSize);
        String receivedData = "";
        buffer.flip();

       /* while (buffer.hasRemaining()) {
            buffer.get(new byte[buffer.limit()]);
            receivedData += new String(buffer.array());
            Log.i(TAG, "bufferSize 2 :" + bufferSize);

        }*/

       receivedData = new String(buffer.array());
//        String receivedData = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();

        Log.i(TAG, "receive data from server, data : " + receivedData);

        processMessage(0, receivedData);
        sendMsg(receivedData);
    }

    private void sendMsg(String result) {
        if (handler != null) {
            Message msg = new Message();
            msg.what = 123;
            msg.obj = result;
            handler.sendMessage(msg);
        }
    }

    public void handleWriteEvent() {
        Log.e(TAG, "handleWriteEvent");
    }

    public void close() {
        try {
            getChannel().socket().close();
            Log.e(TAG, "socket close");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            getChannel().close();
            Log.e(TAG, "channel close");
            setConnected(false);
            getSelector().close();
            Log.e(TAG, "selector close");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void processMessage(final int type, final String receivedMsg);

    public abstract boolean send(int type, String sendMsg);

    public void setHandler(Handler handler) {
        this.handler=handler;
    }
}
