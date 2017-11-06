package android.player;

/**
 * Created by tWX366549 on 2017/1/14.
 */
public class IjkPlayer {

//    private Context context;
//    private VncCanvasActivity activity;
//    private int mIsLiveStreaming = 1;
//    private View mLoadingView;
//    private PLVideoTextureView mVideoView;
//    private String mVideoPath = null;
//    private Toast mToast = null;
//    private boolean mIsActivityPaused = true;
//    private int mDisplayAspectRatio = PLVideoTextureView.ASPECT_RATIO_PAVED_PARENT; //default
//    private static final int MESSAGE_ID_RECONNECTING = 0x01;
//    private CheckBox toggleFullScreen;
//    private boolean isVideoPrepared;
//    private int lastOrientation = 0;
//
//    @Override
//    public void init(Context context, VncCanvasActivity activity) {
//        this.context = context;
//        this.activity = activity;
//    }
//
//    private PLMediaPlayer.OnPreparedListener mOnPreparedListener = new PLMediaPlayer.OnPreparedListener() {
//        @Override
//        public void onPrepared(PLMediaPlayer plMediaPlayer) {
//            Log.e("huawei", "onPrepared");
//            isVideoPrepared = true;
//        }
//    };
//
//    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
//        @Override
//        public void onCompletion(PLMediaPlayer plMediaPlayer) {
//        }
//    };
//
//    private PLMediaPlayer.OnInfoListener onInfoListener = new PLMediaPlayer.OnInfoListener() {
//        @Override
//        public boolean onInfo(PLMediaPlayer plMediaPlayer, int i, int i1) {
//            return false;
//        }
//    };
//
//    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
//        @Override
//        public boolean onError(PLMediaPlayer mp, int errorCode) {
//            boolean isNeedReconnect = false;
//            switch (errorCode) {
//                case PLMediaPlayer.ERROR_CODE_INVALID_URI:
//                    showToastTips("Invalid URL !");
//                    break;
//                case PLMediaPlayer.ERROR_CODE_404_NOT_FOUND:
//                    showToastTips("404 resource not found !");
//                    break;
//                case PLMediaPlayer.ERROR_CODE_CONNECTION_REFUSED:
//                    showToastTips("Connection refused !");
//                    break;
//                case PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT:
//                    showToastTips("Connection timeout !");
//                    isNeedReconnect = true;
//                    break;
//                case PLMediaPlayer.ERROR_CODE_EMPTY_PLAYLIST:
//                    showToastTips("Empty playlist !");
//                    break;
//                case PLMediaPlayer.ERROR_CODE_STREAM_DISCONNECTED:
//                    showToastTips("Stream disconnected !");
//                    isNeedReconnect = true;
//                    break;
//                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
//                    showToastTips("Network IO Error !");
//                    isNeedReconnect = true;
//                    break;
//                case PLMediaPlayer.ERROR_CODE_UNAUTHORIZED:
//                    showToastTips("Unauthorized Error !");
//                    break;
//                case PLMediaPlayer.ERROR_CODE_PREPARE_TIMEOUT:
//                    showToastTips("Prepare timeout !");
//                    isNeedReconnect = true;
//                    break;
//                case PLMediaPlayer.ERROR_CODE_READ_FRAME_TIMEOUT:
//                    showToastTips("Read frame timeout !");
//                    isNeedReconnect = true;
//                    break;
//                case PLMediaPlayer.ERROR_CODE_HW_DECODE_FAILURE:
//                    generateOptions(AVOptions.MEDIA_CODEC_SW_DECODE);
//                    isNeedReconnect = true;
//                    break;
//                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
//                    break;
//                default:
//                    showToastTips("unknown error !");
//                    break;
//            }
//            // Todo pls handle the error status here, reconnect or call finish()
//            if (isNeedReconnect) {
//                sendReconnectMessage();
//            } else {
//                activity.finish();
//            }
//            // Return true means the error has been handled
//            // If return false, then `onCompletion` will be called
//            return true;
//        }
//    };
//
//    private void sendReconnectMessage() {
////        if (isVideoBuffering) return;
//        showToastTips("正在重连...");
//        mLoadingView.setVisibility(View.VISIBLE);
//        mHandler.removeCallbacksAndMessages(null);
//        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_ID_RECONNECTING), 500);
//    }
//
//    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what != MESSAGE_ID_RECONNECTING) {
//                return;
//            }
//            if (mIsActivityPaused || !Utils.isLiveStreamingAvailable()) {
//                activity.finish();
//                return;
//            }
//            if (!Utils.isNetworkAvailable(context)) {
//                sendReconnectMessage();
//                return;
//            }
//            mVideoView.setVideoPath(mVideoPath);
//            mVideoView.start();
//        }
//    };
//
//    private void showToastTips(final String tips) {
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mToast != null) {
//                    mToast.cancel();
//                }
//                mToast = Toast.makeText(context, tips, Toast.LENGTH_SHORT);
//                mToast.show();
//            }
//        });
//    }
//
//    private AVOptions generateOptions(int codecType) {
//        AVOptions options = new AVOptions();
//        // the unit of timeout is ms
//        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
//        options.setInteger(AVOptions.KEY_BUFFER_TIME, 2 * 100);
//        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
//        options.setInteger(AVOptions.KEY_PROBESIZE, 1024);
//        // Some optimization with buffering mechanism when be set to 1
//        options.setInteger(AVOptions.KEY_LIVE_STREAMING, mIsLiveStreaming);
//        if (mIsLiveStreaming == 1) {
//            options.setInteger(AVOptions.KEY_DELAY_OPTIMIZATION, 1);
//        }
//        // 1 -> hw codec enable, 0 -> disable [recommended]
//        options.setInteger(AVOptions.KEY_MEDIACODEC, codecType);
//
//        // whether start play automatically after prepared, default value is 1
//        options.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);
//        return options;
//
//    }
//
//    @Override
//    public void setOritation(int degree) {
//        if (mVideoView != null) mVideoView.setDisplayOrientation(degree);
//    }
//
//    @Override
//    public ViewGroup onCreate() {
//        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.canvas, null);
//        mVideoView = (PLVideoTextureView) view.findViewById(R.id.ijkVideoView);
//        mLoadingView = view.findViewById(R.id.LoadingView);
//        mVideoView.setVisibility(View.VISIBLE);
//        mVideoView.setBufferingIndicator(mLoadingView);
//        mLoadingView.setVisibility(View.VISIBLE);
//        mVideoPath = activity.getIntent().getStringExtra("videoPath");
//        mIsLiveStreaming = activity.getIntent().getIntExtra("liveStreaming", 1);
//        int codec = activity.getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
//        mVideoView.setAVOptions(generateOptions(codec));
//        // You can mirror the display
//        // mVideoView.setMirror(true);
//
//        // You can also use a custom `MediaController` widget
////        mMediaController = new MediaController(this, false, mIsLiveStreaming==1);
////        mVideoView.setMediaController(mMediaController);
//
//        mVideoView.setOnCompletionListener(mOnCompletionListener);
//        mVideoView.setOnErrorListener(mOnErrorListener);
//        mVideoView.setOnInfoListener(onInfoListener);
//        mVideoView.setOnPreparedListener(mOnPreparedListener);
//        mVideoView.setDisplayAspectRatio(mDisplayAspectRatio);
//        mVideoView.setVideoPath(mVideoPath);
//        mVideoView.start();
//        return view;
//    }
//
//    @Override
//    public void requestFocus() {
//        mVideoView.requestFocus();
//    }
//
//    @Override
//    public void onResume() {
//        mIsActivityPaused = false;
//        mVideoView.start();
//    }
//
//    @Override
//    public void onPause() {
//        mToast = null;
//        mIsActivityPaused = true;
//    }
//
//    @Override
//    public void onDestroy() {
//        if (mVideoView != null)
//            mVideoView.stopPlayback();
//    }
//
//    @Override
//    public void onLowMemory() {
//
//    }
//
//    @Override
//    public int getPlayerType() {
//        return VncCanvasActivity.VIDEO_TYPE_INJK;
//    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//
//    }
//
//    @Override
//    public boolean isPrepared() {
//        return isVideoPrepared;
//    }
}
