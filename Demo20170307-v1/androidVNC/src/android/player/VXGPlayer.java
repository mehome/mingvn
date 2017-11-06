package android.player;

import android.androidVNC.IVideoPlayer;
import android.androidVNC.R;
import android.androidVNC.VncCanvasActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.List;

import veg.mediaplayer.sdk.M3U8;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayerConfig;
import veg.mediaplayer.sdk.teststreamcontrol.SharedSettings;

/**
 * Created by yWX449849 on 2017/2/24.
 */
class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
{
    public static final float MIN_ZOOM = 0.7f;
    public static final float MAX_ZOOM = 1.0f;
    public float scaleFactor = 1.0f;
    public boolean zoom = false;

    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
        Log.e("Player", "onScale " + scaleFactor);
        return true;
    }
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector)
    {
        Log.e("Player", "onScaleBegin");
        zoom = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector)
    {
        Log.e("Player", "onScaleEnd");
        zoom = false;
    }

}

class ViewSizes
{
    public float dx = 0;
    public float dy = 0;

    public float orig_width = 0;
    public float orig_height = 0;

    public ScaleListener listnrr = null;

}
class InternalBuffersScheduler implements Runnable {
    private MediaPlayer mPlayer = null;
    private int mMaxThreshold = -1;
    private int mMinThreshold = -1;
    private int mMeasurementInterval = 100;

    // Speed control
    private int mSpeed 			= 1000;
    private int mNormalSpeed 	= 1000;
    private int mOverSpeed 		= 1050; //  1,2X RATE
    private int mDownSpeed 		= 950;  //  0,2X RATE

    /////////////////////////////////////////////////////////////
    // Frames number threshold
    /////////////////////////////////////////////////////////////
	/*
	TEST CASE 1
	// Upper level of buffering
	private int mUpperMaxFrames 	= 50;
	private int mUpperNormalFrames 	= 30;

	// Lower level of buffering
	private int mLowerMinFrames 	= 10;
	private int mLowerNormalFrames 	= 30;
	private int mVideoFrames		= 0;
	*/

	/*
	//TEST CASE 2
	// Upper level of buffering
	private int mUpperMaxFrames 	= 30;
	private int mUpperNormalFrames 	= 10;

	// Lower level of buffering
	private int mLowerMinFrames 	= 00;
	private int mLowerNormalFrames 	= 10;
	private int mVideoFrames		= 0;
	*/


    //TEST CASE 3 - 0 - 5 video frames in buffers
    // Upper level of buffering
    private int mUpperMaxFrames 	= 5;
    private int mUpperNormalFrames	= 1;

    // Lower level of buffering
    private int mLowerMinFrames 	= 0;
    private int mLowerNormalFrames	= 1;
    private int mVideoFrames		= 0;

    private int debug_log			= 0;



    InternalBuffersScheduler(MediaPlayer player,
                             int maxVal /* bytes */,
                             int minVal /* bytes */,
                             int over_speed, /* in MediaPlayer's values */
                             int low_speed,
                             int interval /* milliseconds */) {
        if (maxVal < minVal) {
            Log.e(getClass().getName(), "Max threshold less than Min threshold, assume it equal");
            maxVal = minVal;
        }

        mMaxThreshold		= maxVal;
        mMinThreshold		= minVal;
        mPlayer				= player;
        mSpeed				= mNormalSpeed;
        mOverSpeed			= over_speed;
        mDownSpeed			= low_speed;
        mMeasurementInterval = interval;
    }

    private void ControlByNumberVideoFrames(MediaPlayer.BuffersState state) {
        //int LastSpeed = NormalSpeed;
        if (state != null && mUpperMaxFrames > 0 && mUpperNormalFrames > 0) {
            int video_frames = state.getBufferFramesSourceAndVideoDecoder() + state.getBufferFramesBetweenVideoDecoderAndVideoRenderer() ;

            if (mVideoFrames == 0)
                mVideoFrames  = video_frames;
            else
                mVideoFrames  = (mVideoFrames + video_frames)/2;

            if (0 != debug_log) Log.d(getClass().getName(), "IBS Current v_frames: " + video_frames + " m:" + mVideoFrames);

            if (mVideoFrames != 0)
            {
                if (	mVideoFrames >= mUpperMaxFrames &&
                        mNormalSpeed == mSpeed)
                {
                    mSpeed = mOverSpeed;
                    Log.d(getClass().getName(),
                            "IBS Threshold reached, setting playback c_speed to " + mSpeed + " v:" + video_frames + ":" + mVideoFrames);
                    mPlayer.setFFRate(mSpeed);
                }
                else if (	mVideoFrames <= mUpperNormalFrames  &&
                        mOverSpeed == mSpeed
                        )
                {
                    mSpeed = mNormalSpeed;
                    Log.d(getClass().getName(),
                            "IBS Threshold reached setting playback c_speed " + mSpeed  + " v:" + video_frames + ":" + mVideoFrames);
                    mPlayer.setFFRate(mSpeed);
                }
                else if (	mVideoFrames <= mLowerMinFrames &&
                        mNormalSpeed == mSpeed)
                {
                    mSpeed = mDownSpeed;
                    Log.d(getClass().getName(),
                            "IBS Threshold reached setting playback c_speed " + mSpeed  + " v:" + video_frames + ":" + mVideoFrames);
                    mPlayer.setFFRate(mSpeed);
                }
                else if (	mVideoFrames >= mLowerNormalFrames &&
                        mDownSpeed == mSpeed)
                {
                    mSpeed = mNormalSpeed;
                    Log.d(getClass().getName(),
                            "IBS Threshold reached setting playback c_speed " + mSpeed  + " v:" + video_frames + ":" + mVideoFrames);
                    mPlayer.setFFRate(mSpeed);
                }
            }


        }
    }

    @Override
    public void run() {

        if (0 != debug_log) Log.d(getClass().getName(), "IBS Start State:" + mPlayer.getState());

        while ((mPlayer.getState() == MediaPlayer.PlayerState.Opened  ||
                mPlayer.getState() == MediaPlayer.PlayerState.Opening ||
                mPlayer.getState() == MediaPlayer.PlayerState.Started ||
                mPlayer.getState() == MediaPlayer.PlayerState.Paused)) {
            MediaPlayer.BuffersState state = mPlayer.getInternalBuffersState();

            if (0 != debug_log) Log.d(getClass().getName(), "IBS_Frames:" +
                    "" + state.getBufferFramesSourceAndVideoDecoder() +
                    "/" + state.getBufferFramesBetweenVideoDecoderAndVideoRenderer() +
                    "  V_L:" + state.getBufferVideoLatency() +
                    "  A_L:" + state.getBufferAudioLatency()
            );



            ControlByNumberVideoFrames(state);

			/*
			if (state != null && mMinThreshold > 0 && mMaxThreshold > 0) {
				int current = state.getBufferFilledSizeBetweenSourceAndVideoDecoder() +
						state.getBufferFilledSizeBetweenVideoDecoderAndVideoRenderer();


				Log.d(getClass().getName(), "IBS Current amount of data " + current);
				if (current >= mMaxThreshold) {
					Log.d(getClass().getName(),
							"IBS Max Threshold reached, setting playback speed to " + mSpeed);

					mPlayer.setFFRate(mSpeed);
				} else if (current <= mMinThreshold) {
					Log.d(getClass().getName(),
							"IBS Min Threshold reached setting playback speed to normal");
					mPlayer.setFFRate(1000);
				}

			}
			*/

            try {
                Thread.sleep(mMeasurementInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(getClass().getName(), "IBS interrupted. Player state " +
                mPlayer.getState());
    }
}
public class VXGPlayer implements IVideoPlayer, MediaPlayer.MediaPlayerCallback{
    private Context context;
    private VncCanvasActivity activity;
    private String mVideoPath;
    private StatusProgressTask mProgressTask = null;
    private boolean playing = false;
    private MediaPlayer player = null;
    private SharedPreferences settings;
    public ScaleGestureDetector detectors = null;
    public ViewSizes mSurfaceSizes 	= null;
    private WifiManager.MulticastLock multicastLock = null;
    private InternalBuffersScheduler buffersScheduler = null;
    private enum PlayerStates
    {
        Busy,
        ReadyForUse
    };

    private enum PlayerConnectType
    {
        Normal,
        Reconnecting
    };

    private PlayerStates player_state = PlayerStates.ReadyForUse;
    private PlayerConnectType reconnect_type = PlayerConnectType.Normal;
    private int mOldMsg = 0;
    private Handler handler = new Handler()
    {
        String strText = "Connecting";

        String sText;
        String sCode;

        @Override
        public void handleMessage(Message msg)
        {
            MediaPlayer.PlayerNotifyCodes status = (MediaPlayer.PlayerNotifyCodes) msg.obj;
            switch (status)
            {
                case CP_CONNECT_STARTING:
                    if (reconnect_type == PlayerConnectType.Reconnecting)
                        strText = "Reconnecting";
                    else
                        strText = "Connecting";

                    startProgressTask(strText);

                    player_state = PlayerStates.Busy;
                    reconnect_type =PlayerConnectType.Normal;
                    break;

                case PLP_BUILD_SUCCESSFUL:
                    sText = player.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
                    sCode = player.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
                    //Log.i(TAG, "=Status PLP_BUILD_SUCCESSFUL: Response sText="+sText+" sCode="+sCode);
                    break;

                case VRP_NEED_SURFACE:
                    player_state = PlayerStates.Busy;
                    showVideoView();
                    break;

                case PLP_PLAY_SUCCESSFUL:
                    player_state =PlayerStates.ReadyForUse;
                    stopProgressTask();

                    buffersScheduler = new InternalBuffersScheduler(player, 40000, 10000, 1050, 950, 100);
                    new Thread(buffersScheduler).start();


                    break;

                case PLP_CLOSE_STARTING:
                    player_state = PlayerStates.Busy;
                    stopProgressTask();
                    setUIDisconnected();
                    break;

                case PLP_CLOSE_SUCCESSFUL:
                    player_state = PlayerStates.ReadyForUse;
                    stopProgressTask();
                    System.gc();
                    setUIDisconnected();
                    break;

                case PLP_CLOSE_FAILED:
                    player_state = PlayerStates.ReadyForUse;
                    stopProgressTask();
                    setUIDisconnected();
                    break;

                case CP_CONNECT_FAILED:
                    player_state = PlayerStates.ReadyForUse;
                    stopProgressTask();
                    setUIDisconnected();
                    break;

                case PLP_BUILD_FAILED:
                    sText = player.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
                    sCode = player.getPropString(MediaPlayer.PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);

                    player_state = PlayerStates.ReadyForUse;
                    stopProgressTask();
                    setUIDisconnected();
                    break;

                case PLP_PLAY_FAILED:
                    player_state =PlayerStates.ReadyForUse;
                    stopProgressTask();
                    setUIDisconnected();
                    break;

                case PLP_ERROR:
                    player_state = PlayerStates.ReadyForUse;
                    stopProgressTask();
                    setUIDisconnected();
                    break;

                case CP_INTERRUPTED:
                    player_state = PlayerStates.ReadyForUse;
                    stopProgressTask();
                    setUIDisconnected();
                    break;
                case CP_RECORD_STARTED:

                {
                    String sFile = player.RecordGetFileName(1);
                }
                break;

                case CP_RECORD_STOPPED:
                {
                    String sFile = player.RecordGetFileName(0);
                }
                break;

                //case CONTENT_PROVIDER_ERROR_DISCONNECTED:
                case CP_STOPPED:
                case VDP_STOPPED:
                case VRP_STOPPED:
                case ADP_STOPPED:
                case ARP_STOPPED:
                    if (player_state != PlayerStates.Busy)
                    {
                        stopProgressTask();
                        player_state = PlayerStates.Busy;
                        player.Close();
                        player_state =PlayerStates.ReadyForUse;
                        setUIDisconnected();
                    }
                    break;

                case CP_ERROR_DISCONNECTED:
                    if (player_state != PlayerStates.Busy)
                    {
                        player_state =PlayerStates.Busy;
                        player.Close();
                        player_state = PlayerStates.ReadyForUse;
                        setUIDisconnected();

                        Toast.makeText(context, "Demo Version!",
                                Toast.LENGTH_SHORT).show();

                    }
                    break;
                default:
                    player_state = PlayerStates.Busy;
            }
        }
    };
    @Override
    public void init(Context context, VncCanvasActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public View onCreate() {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
        activity.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        activity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.canvas, null);
        Intent intent = activity.getIntent();
        mVideoPath = intent.getStringExtra("videoPath");
        settings = PreferenceManager.getDefaultSharedPreferences(context);

        SharedSettings.getInstance(context).loadPrefSettings();
        SharedSettings.getInstance().savePrefSettings();

        player = (MediaPlayer) view.findViewById(R.id.vxgplayerView);

        player.getSurfaceView().setZOrderOnTop(true);    // necessary

        SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
        //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //player.setRotation(180.0f);
       // player.getSurfaceView().setRotation(180.0f);
        if (player != null)
        {
            player.getConfig().setConnectionUrl(mVideoPath);
            player.Close();
            if (playing)
            {
            }
            else
            {
                openPlayer(-1);
            }
            if (player != null)
                player.onStart();
        }
        return view;
    }
    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height)
    {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }
    @Override
    public void onResume() {
        Log.e("SDL", "onResume()");
        if (player != null)
            player.onResume();
    }
    public void openPlayer(int idHLSStream)
    {
        SharedSettings sett = SharedSettings.getInstance();
        boolean bPort = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;

        MediaPlayerConfig conf = new MediaPlayerConfig();

        player.setVisibility(View.INVISIBLE);

        conf.setConnectionUrl(player.getConfig().getConnectionUrl());

        conf.setConnectionNetworkProtocol(sett.connectionProtocol);
        conf.setConnectionDetectionTime(5000/*sett.connectionDetectionTime*/); // 1000 Ms
        conf.setConnectionBufferingTime(99 /*sett.connectionBufferingTime*/); // 99 Ms 3 frames
        conf.setDecodingType(sett.decoderType);
        conf.setRendererType(sett.rendererType);
        conf.setSynchroEnable(sett.synchroEnable);
        conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
        //conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
        conf.setEnableColorVideo(sett.rendererEnableColorVideo);
        //conf.setEnableAspectRatio(aspect);
        conf.setEnableAspectRatio(0);
        conf.setAspectRatioMode(0);
        conf.setDataReceiveTimeout(30000);
        conf.setNumberOfCPUCores(0);
        player.Open(conf, VXGPlayer.this);

        playing = true;
    }
    @Override
    public void onPause() {
        if (player != null)
            player.onPause();
    }
    private void showVideoView()
    {
       // player.setVisibility(View.VISIBLE);
        SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
    }
    private void startProgressTask(String text)
    {
        stopProgressTask();

        mProgressTask = new StatusProgressTask(text);
        executeAsyncTask(mProgressTask, text);
    }

    private void stopProgressTask()
    {
        if (mProgressTask != null)
        {
            mProgressTask.stopTask();
            mProgressTask.cancel(true);
        }
    }

    private class StatusProgressTask extends AsyncTask<String, Void, Boolean>
    {
        String strProgressTextSrc;
        String strProgressText;
        Rect bounds = new Rect();
        boolean stop = false;

        public StatusProgressTask(String text)
        {
            stop = false;
            strProgressTextSrc = text;
        }

        public void stopTask() { stop = true; }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                if (stop) return true;

                String maxText = "Disconnected.....";//strProgressTextSrc + "....";
                int len = maxText.length();
                //playerStatusText.getPaint().getTextBounds(maxText, 0, len, bounds);
                strProgressText = strProgressTextSrc + "...";
                Runnable uiRunnable = null;
                uiRunnable = new Runnable()
                {
                    public void run()
                    {
                        if (stop) return;
                        synchronized(this) { this.notify(); }
                    }
                };

                int nCount = 4;
                do
                {
                    try
                    {
                        Thread.sleep(300);
                    }
                    catch ( InterruptedException e ) { stop = true; }

                    if (stop) break;

                    if (nCount <= 3)
                    {
                        strProgressText = strProgressTextSrc;
                        for (int i = 0; i < nCount; i++)
                            strProgressText = strProgressText + ".";
                    }

                    synchronized ( uiRunnable )
                    {
                        //runOnUiThread(uiRunnable);
                        try
                        {
                            uiRunnable.wait();
                        }
                        catch ( InterruptedException e ) { stop = true; }
                    }

                    if (stop) break;

                    nCount++;
                    if (nCount > 3)
                    {
                        nCount = 1;
                        strProgressText = strProgressTextSrc;
                    }
                }

                while(!isCancelled());
            }
            catch (Exception e)
            {
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            mProgressTask = null;
        }
        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }
    }
    private DownloadAndParseHLSStream downloadHLSStreamsTask = null;
    List<M3U8.HLSStream> listStreams = null;

    private void asyncDownloadHLSStreams(String url)
    {
        if (downloadHLSStreamsTask == null)
        {
            //playerPanelControlTask.cancel(true);
            //playerPanelControlTask = null;
            downloadHLSStreamsTask = new DownloadAndParseHLSStream();
            executeAsyncTask(downloadHLSStreamsTask, url);
        }
    }
    private void closeDownloadHLSStreams()
    {
        if (downloadHLSStreamsTask != null)
        {
            downloadHLSStreamsTask.Stop();
            downloadHLSStreamsTask.cancel(true);
            downloadHLSStreamsTask = null;
        }
    }

    private class DownloadAndParseHLSStream extends AsyncTask<String, Void, Boolean>
    {
        public void Stop(){ stop = true; };
        private boolean stop = false;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            stop = false;
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                do
                {
                    M3U8 m3u8 = new M3U8();
                    m3u8.getDataAndParse(params[0]);
                    listStreams = m3u8.getChannelList();
                    for(int i = 0; i < listStreams.size(); i++)
                    {
                    }
                    break;
                }
                while(!stop && !isCancelled());
            }
            catch (Exception e)
            {
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            downloadHLSStreamsTask = null;
            stop = false;

            if (listStreams == null || listStreams.size() <= 0)
            {
                openPlayer(-1);
                return;
            }
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            downloadHLSStreamsTask = null;
            stop = false;
        }
    }

    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params)
    {

        {
            task.execute(params);
        }
    }

    @Override
    public void onDestroy() {
        if (player != null)
            player.onDestroy();

        stopProgressTask();
        System.gc();

        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (player != null)
            player.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void onLowMemory() {

        if (player != null)
            player.onLowMemory();
    }

    @Override
    public int getPlayerType() {
        return 0;
    }

    @Override
    public void setOritation(int degree) {
    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @Override
    public void onBackPressed() {
        player.Close();
        if (!playing)
        {
            //super.onBackPressed();
            return;
        }
    }

    @Override
    public void onStop() {
        if (player != null)
            player.onStop();
    }
    @Override
    public int Status(int arg)
    {

        MediaPlayer.PlayerNotifyCodes status = MediaPlayer.PlayerNotifyCodes.forValue(arg);
        if (handler == null || status == null)
            return 0;

       // Log.e(TAG, "Form Native Player status: " + arg);
        switch (MediaPlayer.PlayerNotifyCodes.forValue(arg))
        {
            default:
                Message msg = new Message();
                msg.obj = status;
                handler.removeMessages(mOldMsg);
                mOldMsg = msg.what;
                handler.sendMessage(msg);
        }

        return 0;
    }
    protected void setUIDisconnected()
    {
        playing = false;
    }
    @Override
    public int OnReceiveData(ByteBuffer byteBuffer, int i, long l) {
        return 0;
    }
}
