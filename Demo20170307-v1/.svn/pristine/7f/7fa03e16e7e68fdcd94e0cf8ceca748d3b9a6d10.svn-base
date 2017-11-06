package android.androidVNC;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tWX366549 on 2017/1/14.
 */
public interface IVideoPlayer {
    void init(Context context, VncCanvasActivity activity);

    View onCreate();

    void onResume();

    void onPause();

    void onDestroy();

    void onWindowFocusChanged(boolean hasFocus);

    void requestFocus();

    void onLowMemory();

    int getPlayerType();

    void setOritation(int degree);

    boolean isPrepared();

    void onBackPressed();

    void onStop();
}
