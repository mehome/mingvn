package android.player.detector;

import android.util.Log;

/**
 * Created by tWX366549 on 2017/2/15.
 */

public class NioClientNioSocketReadHandler extends NioSocketReadHandler {

    @Override
    public void processMessage(int type, String receivedMsg) {
//        Log.i(TAG, "processMessage : receivedMsg = " + receivedMsg);
    }

    @Override
    public boolean send(int type, String sendMsg) {
        return true;
    }
}
