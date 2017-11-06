package android.player;

import android.androidVNC.IVideoPlayer;
import android.androidVNC.R;
import android.androidVNC.Utils;
import android.androidVNC.VncCanvasActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.player.ijk.widget.media.AndroidMediaController;
import android.player.ijk.widget.media.IRenderView;
import android.player.ijk.widget.media.IjkVideoView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by tWX366549 on 2017/2/6.
 */
public class NewIjkPlayer implements IVideoPlayer, IMediaPlayer.OnInfoListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener {
    private static final String TAG = "NewIjkPlayer";
    private String mVideoPath;
    private Uri mVideoUri;
    private static final int MESSAGE_ID_RECONNECTING = 0x01;
    private AndroidMediaController mMediaController;
    private IjkVideoView mVideoView;
    private TextView mToastTextView;
    private TableLayout mHudView;

    private Settings mSettings;
    private boolean mBackPressed;
    private Context context;
    private VncCanvasActivity activity;
    private Toast mToast;
    private View mLoadingView;
    private int w;
    private int h;

    @Override
    public void init(Context context, VncCanvasActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public View onCreate() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        w = wm.getDefaultDisplay().getWidth();
        h = wm.getDefaultDisplay().getHeight();
        Log.i(TAG, TAG + "_onCreate");
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.canvas, null);
        mSettings = new Settings(context);
//        mSettings.useMediaCodec(true);
        Intent intent = activity.getIntent();
        String intentAction = intent.getAction();
        mVideoPath = intent.getStringExtra("videoPath");
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        activity.finish();
                        return view;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        activity.finish();
                        return view;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        activity.finish();
                        return view;
                    }
                }
            }
        }

        // init UI

        mMediaController = new AndroidMediaController(context, false);
//        mToastTextView = (TextView) view.findViewById(R.id.toast_text_view);
//        mHudView = (TableLayout) view.findViewById(R.id.hud_view);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mLoadingView = view.findViewById(R.id.LoadingView);
        mVideoView = (IjkVideoView) view.findViewById(R.id.video_view);
//        mVideoView.setMediaController(mMediaController);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setmOnVideoSizeChangedListener(this);
//        mVideoView.setHudView(mHudView);
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            activity.finish();
            return view;
        }
        mVideoView.start();
        return view;
    }

    @Override
    public void onResume() {
        mVideoView.resume();
    }

    @Override
    public void onPause() {
        mVideoView.pause();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    @Override
    public void requestFocus() {
        if (mVideoView != null)
            mVideoView.requestFocus();
    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public int getPlayerType() {
        return VncCanvasActivity.VIDEO_TYPE_INJK;
    }

    @Override
    public void setOritation(int degree) {
        if (mVideoView != null) {
            mVideoView.setVideoRotation(degree);
            if (degree == 0 || degree == 180) {
//                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(1080, 1920);
//                lp.gravity = Gravity.CENTER;
//                mVideoView.setLayoutParams(lp);
                mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
            } else {
//                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(1920, 1080);
//                lp.gravity = Gravity.CENTER;
//                mVideoView.setLayoutParams(lp);
                mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
            }
//            mVideoView.requestLayout();
        }
    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;
    }

    @Override
    public void onStop() {
        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
//            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }


    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if (what == 3) {
//            Toast.makeText(context, "开始播放", Toast.LENGTH_LONG).show();
        }
        Log.i(TAG, TAG + "_onInfo what=" + what + ",    extra : " + extra);
        return true;
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.e(TAG, TAG + "_onError what=" + what + ",    extra : " + extra);
        return true;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        Log.e(TAG, TAG + "_onPrepared : width:" + mp.getVideoWidth() + ", height:" + mp.getVideoHeight());
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        Log.i("davy", "onVideoSizeChanged : width:" + width + ", height:" + height);
    }
}
