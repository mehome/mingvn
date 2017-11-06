package android.player;

import android.androidVNC.IVideoPlayer;
import android.androidVNC.VncCanvasActivity;
import android.content.Context;
import android.view.View;

import org.libsdl.app.SDLActivity;

/**
 * Created by tWX366549 on 2017/1/17.
 */
public class SDLPlayer implements IVideoPlayer {

    private SDLActivity sdlActivity;

    public SDLPlayer() {
        sdlActivity = new SDLActivity();
    }

    @Override
    public void init(Context context, VncCanvasActivity activity) {
        sdlActivity.init(context, activity);
    }

    @Override
    public View onCreate() {
        return sdlActivity.onCreate();
    }

    @Override
    public void onResume() {
        sdlActivity.onResume();
    }

    @Override
    public void onPause() {
        sdlActivity.onPause();
    }

    @Override
    public void onDestroy() {
        sdlActivity.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        sdlActivity.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void requestFocus() {
        sdlActivity.requestFocus();
    }

    @Override
    public void onLowMemory() {
        sdlActivity.onLowMemory();
    }

    @Override
    public int getPlayerType() {
        return sdlActivity.getPlayerType();
    }

    @Override
    public void setOritation(int degree) {
        sdlActivity.setOritation(degree);
    }

    @Override
    public boolean isPrepared() {
        return sdlActivity.isPrepared();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onStop() {

    }
}
