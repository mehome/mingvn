/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package veg.mediaplayer.sdk.teststreamcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import android.preference.PreferenceManager;
import veg.mediaplayer.sdk.MediaPlayer;
import veg.mediaplayer.sdk.MediaPlayer.MediaPlayerCallback;
import veg.mediaplayer.sdk.MediaPlayer.PlayerModes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerNotifyCodes;
import veg.mediaplayer.sdk.MediaPlayer.PlayerProperties;
import veg.mediaplayer.sdk.MediaPlayer.PlayerRecordFlags;
import veg.mediaplayer.sdk.MediaPlayer.PlayerState;
import veg.mediaplayer.sdk.MediaPlayer.VideoShot;
import veg.mediaplayer.sdk.MediaPlayerConfig;

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
	
		while ((mPlayer.getState() == PlayerState.Opened  ||
				mPlayer.getState() == PlayerState.Opening ||
				mPlayer.getState() == PlayerState.Started ||
				mPlayer.getState() == PlayerState.Paused)) {
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

public class MainActivity extends Activity implements OnClickListener, MediaPlayerCallback, OnTouchListener
{
    private static final String TAG 	 = "MediaPlayerTest";
    
	public  static AutoCompleteTextView	edtIpAddress;
	public  static ArrayAdapter<String> edtIpAddressAdapter;
	public  static Set<String>			edtIpAddressHistory;
	private Button						btnConnect;
	private Button						btnHistory;
	private Button						btnShot;
	private Button						btnRecord;
	private boolean						is_record = false;

	private StatusProgressTask 			mProgressTask = null;
	
	private SharedPreferences 			settings;
    private SharedPreferences.Editor 	editor;

    private boolean 					playing = false;
    private MediaPlayer 				player = null;
    //private MediaPlayer 				player_record = null;
    private MainActivity 				mthis = null;

    private RelativeLayout 				playerStatus = null;
    private TextView 					playerStatusText = null;
    private TextView 					playerHwStatus = null;
    
	public ScaleGestureDetector 		detectors = null;	
	public ViewSizes 					mSurfaceSizes 	= null;
    
    private MulticastLock multicastLock = null;
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
    
	private Object waitOnMe = new Object();
	private PlayerStates player_state = PlayerStates.ReadyForUse; 
	private PlayerConnectType reconnect_type = PlayerConnectType.Normal;
	private int mOldMsg = 0;

	private Toast toastShot = null;
	
	// Event handler
	
	private Handler handler = new Handler() 
    {
		String strText = "Connecting";
		
		String sText;
		String sCode;
		
		@Override
	    public void handleMessage(Message msg) 
	    {
	    	PlayerNotifyCodes status = (PlayerNotifyCodes) msg.obj;
	        switch (status) 
	        {
	        	case CP_CONNECT_STARTING:
	        		if (reconnect_type == PlayerConnectType.Reconnecting)
	        			strText = "Reconnecting";
	        		else
	        			strText = "Connecting";
	        			
	        		startProgressTask(strText);
	        		
	        		player_state = PlayerStates.Busy;
	    			showStatusView();
	    			
	    			reconnect_type = PlayerConnectType.Normal;
	    			setHideControls();
	    			break;
	    			
	        	case PLP_BUILD_SUCCESSFUL:
	        		sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
	        		sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
	        		Log.i(TAG, "=Status PLP_BUILD_SUCCESSFUL: Response sText="+sText+" sCode="+sCode);
	        		break;
	                
		    	case VRP_NEED_SURFACE:
		    		player_state = PlayerStates.Busy;
		    		showVideoView();
					break;
	
		    	case PLP_PLAY_SUCCESSFUL:
		    		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("");
		    		setTitle(R.string.app_name);

					buffersScheduler = new InternalBuffersScheduler(player, 40000, 10000, 1050, 950, 100);
					new Thread(buffersScheduler).start();

					
			        break;
	                
	        	case PLP_CLOSE_STARTING:
	        		player_state = PlayerStates.Busy;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setUIDisconnected();
	    			break;
	                
	        	case PLP_CLOSE_SUCCESSFUL:
	        		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			System.gc();
	    			setShowControls();
	    			setUIDisconnected();
	                break;
	                
	        	case PLP_CLOSE_FAILED:
	        		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	   			break;
	               
	        	case CP_CONNECT_FAILED:
	        		player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_BUILD_FAILED:
	        		sText = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_TEXT);
	        		sCode = player.getPropString(PlayerProperties.PP_PROPERTY_PLP_RESPONSE_CODE);
	        		Log.i(TAG, "=Status PLP_BUILD_FAILED: Response sText="+sText+" sCode="+sCode);

	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_PLAY_FAILED:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case PLP_ERROR:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	                
	            case CP_INTERRUPTED:
	            	player_state = PlayerStates.ReadyForUse;
	        		stopProgressTask();
	    			playerStatusText.setText("Disconnected");
	    			showStatusView();
	    			setShowControls();
	    			setUIDisconnected();
	    			break;
	            case CP_RECORD_STARTED:
	            	Log.v(TAG, "=handleMessage CP_RECORD_STARTED");
	            	{
	            		String sFile = player.RecordGetFileName(1);
	            		Toast.makeText(getApplicationContext(),"Record Started. File "+sFile, Toast.LENGTH_LONG).show();
	            	}
	            	break;

	            case CP_RECORD_STOPPED:
	            	Log.v(TAG, "=handleMessage CP_RECORD_STOPPED");
	            	{
	            		String sFile = player.RecordGetFileName(0);
	            		Toast.makeText(getApplicationContext(),"Record Stopped. File "+sFile, Toast.LENGTH_LONG).show();
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
						if (toastShot != null)
							toastShot.cancel();
	            		player.Close();
	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
		    			player_state = PlayerStates.ReadyForUse;
		    			setShowControls();
		    			setUIDisconnected();
	            	}
	                break;
	
	            case CP_ERROR_DISCONNECTED:
	            	if (player_state != PlayerStates.Busy)
	            	{
	            		player_state = PlayerStates.Busy;
						if (toastShot != null)
							toastShot.cancel();
	            		player.Close();

	        			playerStatusText.setText("Disconnected");
		    			showStatusView();
		    			player_state = PlayerStates.ReadyForUse;
		    			setUIDisconnected();
	            		
						Toast.makeText(getApplicationContext(), "Demo Version!",
								   Toast.LENGTH_SHORT).show();
						
	            	}
	                break;
	            default:
	            	player_state = PlayerStates.Busy;
	        }
	    }
	};

	// callback from Native Player 
	@Override
	public int OnReceiveData(ByteBuffer buffer, int size, long pts) 
	{
		Log.e(TAG, "Form Native Player OnReceiveData: size: " + size + ", pts: " + pts);
		return 0;
	}
    

	// All event are sent to event handlers    
	@Override
	public int Status(int arg)
	{
		
		PlayerNotifyCodes status = PlayerNotifyCodes.forValue(arg);
		if (handler == null || status == null)
			return 0;
		
		Log.e(TAG, "Form Native Player status: " + arg);
	    switch (PlayerNotifyCodes.forValue(arg)) 
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
	
    public String getRecordPath()
    {
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
    		      Environment.DIRECTORY_DCIM), "RecordsMediaPlayer");
    	
	    if (! mediaStorageDir.exists()){
	        if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory())){
	            Log.e(TAG, "<=getRecordPath() failed to create directory path="+mediaStorageDir.getPath());
	            return "";
	        }
	    }
	    return mediaStorageDir.getPath();
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
		String  strUrl;

		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifi.createMulticastLock("multicastLock");
		multicastLock.setReferenceCounted(true);
		multicastLock.acquire();
		
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		setContentView(R.layout.vxg_main);
		mthis = this;
		
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		SharedSettings.getInstance(this).loadPrefSettings();
		SharedSettings.getInstance().savePrefSettings();
		
		playerStatus 		= (RelativeLayout)findViewById(R.id.playerStatus);
		playerStatusText 	= (TextView)findViewById(R.id.playerStatusText);
		playerHwStatus 		= (TextView)findViewById(R.id.playerHwStatus);
		
		player = (MediaPlayer)findViewById(R.id.playerView);
		
		//record only
		//player_record = new MediaPlayer(this, false);

		strUrl = settings.getString("connectionUrl", "rtsp://10.20.16.127");
		
		player.getSurfaceView().setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		HashSet<String> tempHistory = new HashSet<String>();
		/*
		tempHistory.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
		tempHistory.add("http://ingest1.netdynamix.fm:1935/Magic828/Magic828.stream/playlist.m3u8");
		tempHistory.add("http://66.118.69.161/hls-live/livepkgr/_definst_/liveevent/wettfm.m3u8");
		tempHistory.add("http://live.tananana.ro:8010/stream-48.aac");
		tempHistory.add("http://dorognoe.hostingradio.ru:8000/dorognoe_acc");
		tempHistory.add("http://icaria2.cdnstream1.com/1053_64");
		tempHistory.add("http://05.stmip.net:7892");
		tempHistory.add("http://radio5tr.canliyayinda.com:8117");
		tempHistory.add("http://192.163.244.136:9982");
		tempHistory.add("http://95.211.115.197:8182");
		tempHistory.add("http://stm29.kshost.com.br:15742");
		tempHistory.add("http://185.12.109.172:7200");
		tempHistory.add("http://67.212.166.178:7500");
		tempHistory.add("http://sc03.warpradio.com:8530");
		tempHistory.add("http://yayin3.canlitv.com:7055");
		tempHistory.add("http://edge.live.mp3.mdn.newmedia.nacamar.net/primaton/livestream4hi.mp3");
		tempHistory.add("http://auds2.intacs.com/caribbeanhotfm");
		tempHistory.add("http://www.fm-tsuyama.jp:1080/fmtsuyama_live.ogg");
		tempHistory.add("http://lodz.radio.pionier.net.pl:8000/radiozak.ogg");
		tempHistory.add("mms://164.obj.netromedia.net/ciurfm");
		tempHistory.add("mms://audio.asdafm.com/ASDAFMLive");
		tempHistory.add("rtsp://rmlive.rferl.org/encoder/channel4.rm");
		tempHistory.add("rtsp://aac.cbs.co.kr/cbs939/_definst_/cbs939.stream");
		tempHistory.add("rtmp://cp72151.live.edgefcs.net:80/live?ovpfv=2.1.6/WGPR-FM@30042");
		tempHistory.add("rtmp://stream.usiouxfalls.edu/kcfs/live_mp3_audio");
		tempHistory.add("rtmp://mediaxi.mcot.net/live/mp4:fm1005_live");*/
		tempHistory.add("rtsp://10.20.16.127");
		
	
		player.setOnTouchListener(new OnTouchListener()
		{
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) 
            {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) 
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                    	if (player.getState() == PlayerState.Paused)
                    		player.Play();
                    	else
                        	if (player.getState() == PlayerState.Started)
                        		player.Pause();
                    }
                }
            		
	        	return true;
            }
        });
		
			
		edtIpAddressHistory = settings.getStringSet("connectionHistory", tempHistory);

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
	
		edtIpAddress = (AutoCompleteTextView)findViewById(R.id.edit_ipaddress);
		edtIpAddress.setText(strUrl);

		edtIpAddress.setOnEditorActionListener(new OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) 
			{
				if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) 
				{
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
	
				}
				return false;
			}
		});

		btnHistory = (Button)findViewById(R.id.button_history);

		// Array of choices
		btnHistory.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(MainActivity.edtIpAddress.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				if (edtIpAddressHistory.size() <= 0)
					return;

				String urlHistory[] = {	"rtsp://russiatoday.fms.visionip.tv/rt/Russia_al_yaum_1000k_1/1000k_1",
										"rtsp://www.tvarm.ru:1935/live/myStream1.sdp",
										"rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov",
										"rtsp://live240.impek.com/brtvhd",
										"http://tv.life.ru/lifetv/480p/index.m3u8",
										"rtsp://10.20.16.127",
										"rtmp://121.121.31.106:443/video2/VXG.stream_high"};

				MainActivity.edtIpAddressAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.vxg_history_item, new ArrayList<String>(edtIpAddressHistory));
				MainActivity.edtIpAddress.setAdapter(MainActivity.edtIpAddressAdapter);
				MainActivity.edtIpAddress.showDropDown();
			}   
		});

		btnShot = (Button)findViewById(R.id.button_shot);

		// Array of choices
		btnShot.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				if (player != null)
				{
					Log.e("SDL", "getVideoShot()");

	    	    	SharedSettings sett = SharedSettings.getInstance();
					
					//VideoShot frame = player.getVideoShot(200, 200);
					VideoShot frame = player.getVideoShot(-1, -1);
					if (frame == null)
						return;
					
					// get your custom_toast.xml ayout
					LayoutInflater inflater = getLayoutInflater();
	 
					View layout = inflater.inflate(R.layout.vxg_videoshot_view,
					  (ViewGroup) findViewById(R.id.videoshot_toast_layout_id));
	 
					ImageView image = (ImageView) layout.findViewById(R.id.videoshot_image);
					image.setImageBitmap(getFrameAsBitmap(frame.getData(), frame.getWidth(), frame.getHeight()));
					
					// Toast...
					if (toastShot != null)
						toastShot.cancel();

					toastShot = new Toast(getApplicationContext());
					toastShot.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toastShot.setDuration(Toast.LENGTH_SHORT);
					toastShot.setView(layout);
					toastShot.show();
					
				}
			}   
		});

		btnConnect = (Button)findViewById(R.id.button_connect);
        btnConnect.setOnClickListener(this);
        
        btnRecord = (Button) findViewById(R.id.button_record);
        //btnRecord.setVisibility(View.GONE);
        btnRecord.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				is_record = !is_record;

				/*if(player_record != null){
					if(is_record){
						//start recording
						int record_flags = PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_AUTO_START) | PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME); //1 - auto start
						int rec_split_time = 30;	
						player_record.RecordSetup(getRecordPath(), record_flags, rec_split_time, 0, "");
						player_record.RecordStart();
						player_record.Play();
					}else{
						//stop recording
						player_record.RecordStop();
					}
				}*/
				
				if(is_record){
					//start recording
					if(player != null){
						int record_flags = get_record_flags();
						int rec_split_time = ( (record_flags & PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME)) != 0 )? 15:0; //15 sec	
						player.RecordSetup(getRecordPath(), record_flags, rec_split_time, 0, "");
						player.RecordStart();
					}
				}else{
					//stop recording
					if(player != null){
						player.RecordStop();
					}
				}
				
				ImageView ivLed  = (ImageView)findViewById(R.id.led);
				if(ivLed != null)
					ivLed.setImageResource( ( is_record ? R.drawable.led_red : R.drawable.led_green) ); 
				btnRecord.setText( is_record? "Stop Record":"Start Record" );
			}
        });
        
        
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_view);
        layout.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (getWindow() != null && getWindow().getCurrentFocus() != null && getWindow().getCurrentFocus().getWindowToken() != null)
					inputManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				return true;
			}
		});
        
		playerStatusText.setText("DEMO VERSION");
		setShowControls();


	player.Close();
	if (playing)
	{
		setUIDisconnected();
	}
	else
	{
		SharedSettings sett = SharedSettings.getInstance();
		boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
		int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
		/*
		MediaPlayerConfig conf = new MediaPlayerConfig();
		
		player.setVisibility(View.INVISIBLE);
		
		//conf.setConnectionUrl("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
		conf.setConnectionUrl(player.getConfig().getConnectionUrl());
		//conf.setConnectionUrl("/storage/emulated/0/DCIM/out.ts"); 
		conf.setConnectionNetworkProtocol(sett.connectionProtocol);
		conf.setConnectionDetectionTime(sett.connectionDetectionTime);
		conf.setConnectionBufferingTime(sett.connectionBufferingTime);
		conf.setDecodingType(sett.decoderType);
		conf.setRendererType(sett.rendererType);
		conf.setSynchroEnable(sett.synchroEnable);
		conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
		//conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
		conf.setEnableColorVideo(sett.rendererEnableColorVideo);
		conf.setEnableAspectRatio(aspect);
		conf.setDataReceiveTimeout(30000);
		conf.setNumberOfCPUCores(0);
		
		//record config
		if(is_record){
			int record_flags = get_record_flags();
			int rec_split_time = ( (record_flags & PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME)) != 0)? 15:0; //15 sec	
			conf.setRecordPath(getRecordPath());
			conf.setRecordFlags(record_flags);
			conf.setRecordSplitTime(rec_split_time);
			conf.setRecordSplitSize(0);
		}else{
			conf.setRecordPath("");
			conf.setRecordFlags(0);
			conf.setRecordSplitTime(0);
			conf.setRecordSplitSize(0);
		}
		Log.v(TAG, "conf record="+is_record);
		
		// Open Player	
		player.Open(conf, mthis);
		*/
		}	

		
        
    }

    private int[] mColorSwapBuf = null;                        // used by saveFrame()
    public Bitmap getFrameAsBitmap(ByteBuffer frame, int width, int height)
    {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(frame);
        return bmp;
    }

	int get_record_flags()
	{
		int flags = PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_AUTO_START) | PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME);	// auto start and split by time
		//+ audio only
		flags |= PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_DISABLE_VIDEO);	
		return flags;	
	}
		
    
    public void onClick(View v) 
	{
		SharedSettings.getInstance().loadPrefSettings();
		if (player != null)
		{
			if (!edtIpAddressHistory.contains(player.getConfig().getConnectionUrl()))
				edtIpAddressHistory.add(player.getConfig().getConnectionUrl());
			
			player.getConfig().setConnectionUrl(edtIpAddress.getText().toString());
			if (player.getConfig().getConnectionUrl().isEmpty())
				return;

			if (toastShot != null)
				toastShot.cancel();
			
			//player_record.Close();
			
			player.Close();
			if (playing)
			{
    			setUIDisconnected();
			}
			else
			{
    	    	SharedSettings sett = SharedSettings.getInstance();
    			boolean bPort = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    	    	int aspect = bPort ? 1 : sett.rendererEnableAspectRatio;
    	    	
    	    	MediaPlayerConfig conf = new MediaPlayerConfig();
    	    	
    	    	//player.setVisibility(View.INVISIBLE);
    	    	
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
   	        	conf.setEnableAspectRatio(aspect);

    	    	conf.setDataReceiveTimeout(30000);
    	    	conf.setNumberOfCPUCores(0);


				conf.setConnectionNetworkProtocol(sett.connectionProtocol);
				conf.setConnectionDetectionTime(sett.connectionDetectionTime);
				conf.setConnectionBufferingTime(sett.connectionBufferingTime);
				conf.setDecodingType(sett.decoderType);
				conf.setRendererType(sett.rendererType);
				conf.setSynchroEnable(1);
				conf.setSynchroNeedDropVideoFrames(sett.synchroNeedDropVideoFrames);
				conf.setEnableColorVideo(sett.rendererEnableColorVideo);
				conf.setEnableAspectRatio(0);
				conf.setAspectRatioMode(0);
				conf.setDataReceiveTimeout(30000);
				conf.setNumberOfCPUCores(2);

    	    	
    	    	//record config
    	    	if(is_record){
					int record_flags = get_record_flags();
					int rec_split_time = ( (record_flags & PlayerRecordFlags.forType(PlayerRecordFlags.PP_RECORD_SPLIT_BY_TIME)) != 0)? 15:0; //15 sec	
    	    		conf.setRecordPath(getRecordPath());
    	    		conf.setRecordFlags(record_flags);
    	    		conf.setRecordSplitTime(rec_split_time);
    	    		conf.setRecordSplitSize(0);
    	    	}else{
    	    		conf.setRecordPath("");
    	    		conf.setRecordFlags(0);
    	    		conf.setRecordSplitTime(0);
    	    		conf.setRecordSplitSize(0);
    	    	}
    	    	Log.v(TAG, "conf record="+is_record);
    	    	
				// Open Player	
        	    //player.Open(conf, mthis);
        	   player.Open(conf, mthis);
				//player.Open(mthis, player.getConfig().getConnectionUrl(), 1, 60000);

				btnConnect.setText("Disconnect");
				
				
				//record only
				conf.setMode(PlayerModes.PP_MODE_RECORD);
				//conf.setRecordTrimPosStart(10000); //from 10th sec
				//conf.setRecordTrimPosEnd(20000); //to 20th sec 
				/*player_record.Open(conf, new MediaPlayerCallback(){

					@Override
					public int Status(int arg) {
						Log.i(TAG, "=player_record Status arg="+arg);
						return 0;
					}

					@Override
					public int OnReceiveData(ByteBuffer buffer, int size,
							long pts) {
						// TODO Auto-generated method stub
						return 0;
					}
					
				});*/
				
				
				playing = true;
			}
		}
    }
 
	protected void onPause()
	{
		Log.e("SDL", "onPause()");
		super.onPause();

		editor = settings.edit();
		editor.putString("connectionUrl", edtIpAddress.getText().toString());

		editor.putStringSet("connectionHistory", edtIpAddressHistory);
		editor.commit();
		
		if (player != null)
			player.onPause();
	}

	@Override
  	protected void onResume() 
	{
		Log.e("SDL", "onResume()");
		super.onResume();
		if (player != null)
			player.onResume();
  	}

  	@Override
	protected void onStart() 
  	{
      	Log.e("SDL", "onStart()");
		super.onStart();
		if (player != null)
			player.onStart();
	}

  	@Override
	protected void onStop() 
  	{
  		Log.e("SDL", "onStop()"); 
		
		super.onStop();
		if (player != null)
			player.onStop();
		
		if (toastShot != null)
			toastShot.cancel();
			

	}

    @Override
    public void onBackPressed() 
    {
		if (toastShot != null)
			toastShot.cancel();
		
		player.Close();
		if (!playing)
		{
	  		super.onBackPressed();
	  		return;			
		}

		setUIDisconnected();
    }
  	
  	@Override
  	public void onWindowFocusChanged(boolean hasFocus) 
  	{
  		Log.e("SDL", "onWindowFocusChanged(): " + hasFocus);
  		super.onWindowFocusChanged(hasFocus);
		if (player != null)
			player.onWindowFocusChanged(hasFocus);
  	}

  	@Override
  	public void onLowMemory() 
  	{
  		Log.e("SDL", "onLowMemory()");
  		super.onLowMemory();
		if (player != null)
			player.onLowMemory();
  	}

  	@Override
  	protected void onDestroy() 
  	{
  		Log.e("SDL", "onDestroy()");
		if (toastShot != null)
			toastShot.cancel();
		
		if (player != null)
			player.onDestroy();
		
		stopProgressTask();
		System.gc();
		
		if (multicastLock != null) {
		    multicastLock.release();
		    multicastLock = null;
		}		
		super.onDestroy();
   	}	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item)  
	{
		switch (item.getItemId())  
		{
//			case R.id.main_opt_settings:
//
//				SharedSettings.getInstance().loadPrefSettings();
//
//				Intent intentSettings = new Intent(MainActivity.this, PreferencesActivity.class);
//				startActivity(intentSettings);
//
//				break;
//			case R.id.main_opt_clearhistory:
//
//				new AlertDialog.Builder(this)
//				.setTitle("Clear History")
//				.setMessage("Do you really want to delete the history?")
//				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
//				{
//					public void onClick(DialogInterface dialog, int which)
//					{
//						HashSet<String> tempHistory = new HashSet<String>();
//						tempHistory.add("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
//						tempHistory.add("http://hls.cn.ru/streaming/2x2tv/tvrec/playlist.m3u8");
//						tempHistory.add("rtsp://rtmp.infomaniak.ch/livecast/latele");
//						tempHistory.add("rtmp://121.121.31.106:443/video2/VXG.stream_high");
//						tempHistory.add("rtsp://10.20.16.127");
//						edtIpAddressHistory.clear();
//						edtIpAddressHistory = tempHistory;
//					}
//				})
//				.setNegativeButton("No", new DialogInterface.OnClickListener()
//				{
//					public void onClick(DialogInterface dialog, int which)
//					{
//						// do nothing
//					}
//				}).show();
//				break;
//			case R.id.main_opt_exit:
//				finish();
//				break;

		}
		return true;
	}

	protected void setUIDisconnected()
	{
		setTitle(R.string.app_name);
		btnConnect.setText("Connect");
		playing = false;
	}

	protected void setHideControls()
	{
		btnShot.setVisibility(View.VISIBLE);
		edtIpAddress.setVisibility(View.GONE);
		btnHistory.setVisibility(View.GONE);
		btnConnect.setVisibility(View.GONE);
	}

	protected void setShowControls()
	{
		setTitle(R.string.app_name);
		
		btnShot.setVisibility(View.GONE);
		edtIpAddress.setVisibility(View.VISIBLE);
		btnHistory.setVisibility(View.VISIBLE);
		btnConnect.setVisibility(View.VISIBLE);
	}

	private void showStatusView() 
	{
		player.setVisibility(View.INVISIBLE);
		playerHwStatus.setVisibility(View.INVISIBLE);
		//player.setAlpha(0.0f);
		playerStatus.setVisibility(View.VISIBLE);
		
	}
	
	private void showVideoView() 
	{
        playerStatus.setVisibility(View.INVISIBLE);
 		player.setVisibility(View.VISIBLE);
		playerHwStatus.setVisibility(View.VISIBLE);

 		SurfaceHolder sfhTrackHolder = player.getSurfaceView().getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		
		setTitle("");
	}
    
	private void startProgressTask(String text)
	{
		stopProgressTask();
	    
	    mProgressTask = new StatusProgressTask(text);
	    //mProgressTask.execute(text);
	    executeAsyncTask(mProgressTask, text);
	}
	
	private void stopProgressTask()
	{
		playerStatusText.setText("");
		setTitle(R.string.app_name);
		
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
            	playerStatusText.getPaint().getTextBounds(maxText, 0, len, bounds);

               	strProgressText = strProgressTextSrc + "...";
                
            	Runnable uiRunnable = null;
                uiRunnable = new Runnable()
                {
                    public void run()
                    {
                        if (stop) return;

    	                playerStatusText.setText(strProgressText);
    	            	
    	            	RelativeLayout.LayoutParams layoutParams = 
    	            		    (RelativeLayout.LayoutParams)playerStatusText.getLayoutParams();
    	           		
    	           		layoutParams.width = bounds.width();
    	           		playerStatusText.setLayoutParams(layoutParams);        	
    	            	playerStatusText.setGravity(Gravity.NO_GRAVITY);
    	            	
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
                    	runOnUiThread(uiRunnable);
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
	
    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) 
    {
    	{
    		task.execute(params);
    	}
    }  
	
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{
		if (detectors != null)
			detectors.onTouchEvent(event);
		
	    switch (event.getAction()) 
	    {
	        case MotionEvent.ACTION_DOWN:
	        	mSurfaceSizes.dx =  event.getX();
	        	mSurfaceSizes.dy =  event.getY();
	            break;
	
	        case MotionEvent.ACTION_MOVE:
	            float x =  event.getX();
	            float y =  event.getY();
	            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
	            float left = lp.leftMargin + (x - mSurfaceSizes.dx); 
	            float top = lp.topMargin + (y - mSurfaceSizes.dy);
	            if (mSurfaceSizes.listnrr != null && mSurfaceSizes.listnrr.zoom)
	            {
	            	int srcw = lp.width;
	            	int srch = lp.height;
	            	
		    		int left_offset = (int) (mSurfaceSizes.orig_width - (mSurfaceSizes.orig_width * mSurfaceSizes.listnrr.scaleFactor));
		    		int top_offset = (int) (mSurfaceSizes.orig_height - (mSurfaceSizes.orig_height * mSurfaceSizes.listnrr.scaleFactor));
		    		Log.e("Player", "ACTION_MOVE2 " + left_offset + "," + top_offset);
		    		
	                lp.leftMargin = left_offset;
	                lp.topMargin  = top_offset;
	                lp.rightMargin = left_offset;
	                lp.bottomMargin  = top_offset;
	            }
	            view.setLayoutParams(lp);
	            break;
	    }	    
	    return true;
	}
	
}
