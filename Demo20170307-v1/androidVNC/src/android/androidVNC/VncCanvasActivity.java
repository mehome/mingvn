/* 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

//
// CanvasView is the Activity for showing VNC Desktop.
//
package android.androidVNC;

import android.addition.FAB2SystemEvent;
import android.addition.SystemFABService;
import android.addition.VncCanvas2FABEvent;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.player.NewIjkPlayer;
import android.player.VXGPlayer;
import android.player.ijk.OnVNCServerOrientationChangedListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.antlersoft.android.bc.BCFactory;
import com.antlersoft.util.ZoomControls;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.libsdl.app.SDLActivity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class VncCanvasActivity extends Activity implements OnVNCServerOrientationChangedListener {
    public static int firstFlag=0;
    public static String currentOrientation;
    private int mLastX;
    private int mLastY;
    private int mDownX;
    private int mDownY;
    private final int ADAPTER_VALUE = 25;
    private FloatingActionButton actionButton;
    private FloatingActionMenu actionMenu;
    private ImageView icon;
    private int screenWidth;
    private int screenWidthHalf;
    private int screenHeight;
    private Intent systemFABServiceIntent;

    private void initEvent() {

        //设置悬浮按钮随手指移动
        actionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();


                Log.d(TAG, " screenWidth:" + screenWidth + " screenHeight:" + screenHeight);

                Log.d(TAG, " getLeft:" + v.getLeft() + " getTop:" + v.getTop());
                Log.d(TAG, " getWidth:" + v.getWidth() + " getHeight:" + v.getHeight());


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        mDownX = (int) event.getRawX();
                        mDownY = (int) event.getRawY();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        //边框四周的限定
                        //手指按下的坐标-view所处的坐标


                        int deltaX = x - mLastX;
                        int deltaY = y - mLastY;

//                        Log.d(TAG, "move, mLastX:" + mLastX + " mLastY:" + mLastY);
//                        Log.d(TAG, "move, deltaX:" + deltaX + " deltaY:" + deltaY);

                        int translationX = (int) actionButton.getTranslationX() + deltaX;
                        int translationY = (int) actionButton.getTranslationY() + deltaY;

                        actionButton.setTranslationX(translationX);
                        actionButton.setTranslationY(translationY);

                        //拖动时如果菜单打开就关闭菜单
                        if (actionMenu.isOpen()) {
                            actionMenu.close(true);
                        }

                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        //setTranslationX之后,view还在原来的位置上,需要自己当手指抬起来之后把view的位置改变到当前位置才行

                        /*if (rangeInDefined(mDownX, (int) event.getRawX() - ADAPTER_VALUE, (int) event.getRawX() + ADAPTER_VALUE)) {
                            if (rangeInDefined(mDownY, (int) event.getRawY() - ADAPTER_VALUE, (int) event.getRawY() + ADAPTER_VALUE)) {
                                actionButton.callOnClick();
                            }
                        }*/
                        //检测移动的距离，如果很微小可以认为是点击事件
                        if (Math.abs(event.getRawX() - mDownX) < 10 && Math.abs(event.getRawY() - mDownY) < 10) {
                            /*try {
                                Field field = View.class.getDeclaredField("mListenerInfo");
                                field.setAccessible(true);
                                Object object = field.get(v);
                                field = object.getClass().getDeclaredField("mOnClickListener");
                                field.setAccessible(true);
                                object = field.get(object);
                                if (object != null && object instanceof View.OnClickListener) {
                                    ((View.OnClickListener) object).onClick(v);
                                }
                            } catch (Exception e) {

                            }*/

                            actionButton.callOnClick();

                        } else {


                            // Log.i("mandroid.cn", "button已移动");

                            //屏幕边吸附功能
                            //可能与控件大小有关,取图片的大小
                            int from;
                            if (event.getRawX() >= screenWidthHalf) {

                                from = (int) event.getRawX();

                                //startAnimaLeft(from,screenWidth-v.getWidth());
                            } else {

                                from = (int) event.getRawX();

                                //startAnimaLeft(from,0);
                            }

                        }

                        break;
                    }
                    default:
                        break;
                }

                mLastX = x;
                mLastY = y;
                return true;
            }
        });


        // Listen menu open and close events to animate the button content view
        actionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees clockwise
                icon.setRotation(0);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 45);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(icon, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees counter-clockwise
                icon.setRotation(45);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(icon, pvhR);
                animation.start();
            }
        });

    }

    private SubActionButton buildSubActionButton(Activity activity, int iconResourceId) {
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(activity);
        ImageView itemIcon = new ImageView(activity);
        itemIcon.setImageResource(iconResourceId);
        return itemBuilder.setContentView(itemIcon).build();
    }

    private void FloatingActionButton() {
        // Create a button to attach the menu:
        // Create an icon
        icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_action_new_light);

        actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        SubActionButton button1 = buildSubActionButton(this, R.drawable.ic_action_back_light);
        SubActionButton button2 = buildSubActionButton(this, R.drawable.ic_action_camera_light);
        SubActionButton button3 = buildSubActionButton(this, R.drawable.ic_action_video_light);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                actionMenu.close(true);
                finish();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                actionMenu.close(true);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                actionMenu.close(true);

            }
        });
        //Create the menu with the items:
        actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .attachTo(actionButton)
                .build();
//        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
//        WindowManager mWindowManager = (WindowManager)VncCanvasActivity.this.getSystemService(getApplication().WINDOW_SERVICE);
//        mWindowManager.addView(actionButton, wmParams);
    }

    @Override
    public void onOrientationChanged(String orientation) {
        currentOrientation=orientation;
        if(VncCanvasActivity.firstFlag<=3)
            VncCanvasActivity.firstFlag++;
        if(orientation.trim().equals("SCREEN_ORIENTATION_PORTRAIT")){
            Log.i("Handler","SCREEN_ORIENTATION_PORTRAI");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else if(orientation.trim().equals("SCREEN_ORIENTATION_LANDSCAPE")){
            Log.i("Handler","SCREEN_ORIENTATION_LANDSCAPE");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else if(orientation.trim().equals("SCREEN_ORIENTATION_REVERSE_LANDSCAPE")){
            Log.i("Handler","SCREEN_ORIENTATION_REVERSE_LANDSCAPE");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    /**
     * @author Michael A. MacDonald
     */
    class ZoomInputHandler extends AbstractGestureInputHandler {

        /**
         * In drag mode (entered with long press) you process mouse events
         * without sending them through the gesture detector
         */
        private boolean dragMode;

        /**
         * Key handler delegate that handles DPad-based mouse motion
         */
        private DPadMouseKeyHandler keyHandler;

        ZoomInputHandler() {
            super(VncCanvasActivity.this);
            keyHandler = new DPadMouseKeyHandler(VncCanvasActivity.this, vncCanvas.handler);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getHandlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getString(
                    R.string.input_mode_touch_pan_zoom_mouse);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return TOUCH_ZOOM_MODE;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.VncCanvasActivity.ZoomInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyDown(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.VncCanvasActivity.ZoomInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyUp(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return trackballMouse(evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDown(android.view.MotionEvent)
         */
        @Override
        public boolean onDown(MotionEvent e) {
            panner.stop();
            return true;
        }

        /**
         * Divide stated fling velocity by this amount to get initial velocity
         * per pan interval
         */
        static final float FLING_FACTOR = 8;

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent,
         *      android.view.MotionEvent, float, float)
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            showZoomer(false);
            panner.start(-(velocityX / FLING_FACTOR),
                    -(velocityY / FLING_FACTOR), new Panner.VelocityUpdater() {

                        /*
                         * (non-Javadoc)
                         *
                         * @see android.androidVNC.Panner.VelocityUpdater#updateVelocity(android.graphics.Point,
                         *      long)
                         */
                        @Override
                        public boolean updateVelocity(PointF p, long interval) {
                            double scale = Math.pow(0.8, interval / 50.0);
                            p.x *= scale;
                            p.y *= scale;
                            return (Math.abs(p.x) > 0.5 || Math.abs(p.y) > 0.5);
                        }

                    });
            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractGestureInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (dragMode) {
                vncCanvas.changeTouchCoordinatesToFullFrame(e);
                if (e.getAction() == MotionEvent.ACTION_UP)
                    dragMode = false;
                return vncCanvas.processPointerEvent(e, true);
            } else
                return super.onTouchEvent(e);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
         */
        @Override
        public void onLongPress(MotionEvent e) {
            showZoomer(true);
            BCFactory.getInstance().getBCHaptic().performLongPressHaptic(
                    vncCanvas);
            dragMode = true;
            vncCanvas.processPointerEvent(vncCanvas
                    .changeTouchCoordinatesToFullFrame(e), true);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent,
         *      android.view.MotionEvent, float, float)
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (inScaling)
                return false;
            showZoomer(false);
            return vncCanvas.pan((int) distanceX, (int) distanceY);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            vncCanvas.changeTouchCoordinatesToFullFrame(e);
            vncCanvas.processPointerEvent(e, true);
            e.setAction(MotionEvent.ACTION_UP);
            return vncCanvas.processPointerEvent(e, false);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            vncCanvas.changeTouchCoordinatesToFullFrame(e);
            vncCanvas.processPointerEvent(e, true, true);
            e.setAction(MotionEvent.ACTION_UP);
            return vncCanvas.processPointerEvent(e, false, true);
        }

    }

    public class TouchpadInputHandler extends AbstractGestureInputHandler {
        /**
         * In drag mode (entered with long press) you process mouse events
         * without sending them through the gesture detector
         */
        private boolean dragMode;
        float dragX, dragY;

        /**
         * Key handler delegate that handles DPad-based mouse motion
         */
        private DPadMouseKeyHandler keyHandler;

        TouchpadInputHandler() {
            super(VncCanvasActivity.this);
            keyHandler = new DPadMouseKeyHandler(VncCanvasActivity.this, vncCanvas.handler);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getHandlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getString(
                    R.string.input_mode_touchpad);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return TOUCHPAD_MODE;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.VncCanvasActivity.ZoomInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyDown(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.VncCanvasActivity.ZoomInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyUp(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return trackballMouse(evt);
        }

        private float fineCtrlScale(float delta) {
            float sign = (delta > 0) ? 1 : -1;
            delta = Math.abs(delta);
            if (delta >= 1 && delta <= 3) {
                delta = 1;
            } else if (delta <= 10) {
                delta *= 0.34;
            } else if (delta <= 30) {
                delta *= delta / 30;
            } else if (delta <= 90) {
                delta *= (delta / 30);
            } else {
                delta *= 3.0;
            }
            return sign * delta;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
         */
        @Override
        public void onLongPress(MotionEvent e) {


            showZoomer(true);
            BCFactory.getInstance().getBCHaptic().performLongPressHaptic(
                    vncCanvas);
            dragMode = true;
            dragX = e.getX();
            dragY = e.getY();
            // send a mouse down event to the remote without moving the mouse.
            remoteMouseStayPut(e);
            vncCanvas.processPointerEvent(e, true);

        }

        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent,
         *      android.view.MotionEvent, float, float)
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {

            if (BCFactory.getInstance().getBCMotionEvent().getPointerCount(e2) > 1) {
                if (inScaling)
                    return false;
                showZoomer(false);
                return vncCanvas.pan((int) distanceX, (int) distanceY);
            } else {
                // compute the relative movement offset on the remote screen.
                float deltaX = -distanceX * vncCanvas.getScale();
                float deltaY = -distanceY * vncCanvas.getScale();
                deltaX = fineCtrlScale(deltaX);
                deltaY = fineCtrlScale(deltaY);

                // compute the absolution new mouse pos on the remote site.
                float newRemoteX = vncCanvas.mouseX + deltaX;
                float newRemoteY = vncCanvas.mouseY + deltaY;


                if (dragMode) {
                    if (e2.getAction() == MotionEvent.ACTION_UP)
                        dragMode = false;
                    dragX = e2.getX();
                    dragY = e2.getY();
                    e2.setLocation(newRemoteX, newRemoteY);
                    return vncCanvas.processPointerEvent(e2, true);
                } else {
                    e2.setLocation(newRemoteX, newRemoteY);
                    vncCanvas.processPointerEvent(e2, false);
                }
            }
            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractGestureInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (dragMode) {
                // compute the relative movement offset on the remote screen.
                float deltaX = (e.getX() - dragX) * vncCanvas.getScale();
                float deltaY = (e.getY() - dragY) * vncCanvas.getScale();
                dragX = e.getX();
                dragY = e.getY();
                deltaX = fineCtrlScale(deltaX);
                deltaY = fineCtrlScale(deltaY);

                // compute the absolution new mouse pos on the remote site.
                float newRemoteX = vncCanvas.mouseX + deltaX;
                float newRemoteY = vncCanvas.mouseY + deltaY;


                if (e.getAction() == MotionEvent.ACTION_UP)
                    dragMode = false;
                e.setLocation(newRemoteX, newRemoteY);
                return vncCanvas.processPointerEvent(e, true);
            } else
                return super.onTouchEvent(e);
        }

        /**
         * Modify the event so that it does not move the mouse on the
         * remote server.
         *
         * @param e
         */
        private void remoteMouseStayPut(MotionEvent e) {
            e.setLocation(vncCanvas.mouseX, vncCanvas.mouseY);

        }

        /*
         * (non-Javadoc)
         * confirmed single tap: do a single mouse click on remote without moving the mouse.
         * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            boolean multiTouch = (BCFactory.getInstance().getBCMotionEvent().getPointerCount(e) > 1);
            remoteMouseStayPut(e);

            vncCanvas.processPointerEvent(e, true, multiTouch || vncCanvas.cameraButtonDown);
            e.setAction(MotionEvent.ACTION_UP);
            return vncCanvas.processPointerEvent(e, false, multiTouch || vncCanvas.cameraButtonDown);
        }

        /*
         * (non-Javadoc)
         * double tap: do two  left mouse right mouse clicks on remote without moving the mouse.
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            remoteMouseStayPut(e);
            vncCanvas.processPointerEvent(e, true, true);
            e.setAction(MotionEvent.ACTION_UP);
            return vncCanvas.processPointerEvent(e, false, true);
        }


        /*
         * (non-Javadoc)
         *
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDown(android.view.MotionEvent)
         */
        @Override
        public boolean onDown(MotionEvent e) {
            panner.stop();
            return true;
        }
    }

    private final static String TAG = "VNC";

    AbstractInputHandler inputHandler;

    VncCanvas vncCanvas;

    VncDatabase database;

    private MenuItem[] inputModeMenuItems;
    private AbstractInputHandler inputModeHandlers[];
    private static ConnectionBean connection;
    private boolean trackballButtonDown;
    private static final int inputModeIds[] = {R.id.itemInputFitToScreen,
            R.id.itemInputTouchpad,
            R.id.itemInputMouse, R.id.itemInputPan,
            R.id.itemInputTouchPanTrackballMouse,
            R.id.itemInputDPadPanTouchMouse, R.id.itemInputTouchPanZoomMouse};

    ZoomControls zoomer;
    Panner panner;

    /*****************************
     * video player add by tianwei start
     **********************************/
    IVideoPlayer videoPlayer = null;
    public static final int VIDEO_TYPE_INJK = 1;
    public static final int VIDEO_TYPE_ANDROID_PLAYER = 0;
    private int lastOrientation;
    private OrientationDetector orientationDetector;

    class OrientationDetector extends OrientationEventListener {

        public OrientationDetector(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {

            if (orientation > 350 || orientation < 10) { //0度
                orientation = 0;
            } else if (orientation > 80 && orientation < 100) { //90度
                orientation = 90;
            } else if (orientation > 170 && orientation < 190) { //180度
                orientation = 180;
            } else if (orientation > 260 && orientation < 280) { //270度
                orientation = 270;
            } else {
                return;
            }
            if (orientation != lastOrientation) {
                Log.i("davy ", "setOritation:" + orientation);
                if (orientation == 270) {
//                    vncCanvas.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            return true;
//                        }
//                    });
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    videoPlayer.setOritation(90);
                } else if (orientation == 0) {
//                    vncCanvas.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            return false;
//                        }
//                    });
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    videoPlayer.setOritation(orientation);
                }
                lastOrientation = orientation;
            }
        }
    }

    /*******************
     * ijkPlayer start
     ************************/
    private int protoType;

    private View initVideoPlayer() {
        protoType = 2;
//        protoType = getIntent().getIntExtra("proto", 0);
        switch (protoType) {
            case 0:
                videoPlayer = new SDLActivity();
                break;
            case 1:
               videoPlayer = new NewIjkPlayer();
                videoPlayer.setOritation(0);
                break;
            case 2:
                videoPlayer = new VXGPlayer();
                videoPlayer.setOritation(0);
                break;
        }
        VncCanvasActivity.firstFlag=0;
        videoPlayer.init(this, this);
        return videoPlayer.onCreate();
    }

    /**
     * 全屏切换
     */
    public void toggleFullScreen() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 获取界面方向
     */
    public int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onPause();
//        orientationDetector.disable();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

        }
        videoPlayer.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        videoPlayer.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        videoPlayer.onBackPressed();


        stopService(systemFABServiceIntent);

        super.onBackPressed();
    }

    private int getNavigationBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("dbw", "Navi height:" + height);
        return height;
    }


    /*******************ijkPlayer End************************/

    /*****************************
     * video player add by tianwei end
     **********************************/

    @Override
    public void onCreate(Bundle icicle) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//        View decorView = getWindow().getDecorView();
//        Log.e("huawei", "decor view : " + decorView);
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
////        }
        database = new VncDatabase(this);
        Intent i = getIntent();
        connection = new ConnectionBean();
        Uri data = i.getData();
        if ((data != null) && (data.getScheme().equals("vnc"))) {
            String host = data.getHost();
            // This should not happen according to Uri contract, but bug introduced in Froyo (2.2)
            // has made this parsing of host necessary
            int index = host.indexOf(':');
            int port;
            if (index != -1) {
                try {
                    port = Integer.parseInt(host.substring(index + 1));
                } catch (NumberFormatException nfe) {
                    port = 0;
                }
                host = host.substring(0, index);
            } else {
                port = data.getPort();
            }
            if (host.equals(VncConstants.CONNECTION)) {
                if (connection.Gen_read(database.getReadableDatabase(), port)) {
                    MostRecentBean bean = androidVNC.getMostRecent(database.getReadableDatabase());
                    if (bean != null) {
                        bean.setConnectionId(connection.get_Id());
                        bean.Gen_update(database.getWritableDatabase());
                    }
                }
            } else {
                connection.setAddress(host);
                connection.setNickname(connection.getAddress());
                connection.setPort(port);
                List<String> path = data.getPathSegments();
                if (path.size() >= 1) {
                    connection.setColorModel(path.get(0));
                }
                if (path.size() >= 2) {
                    connection.setPassword(path.get(1));
                }
                connection.save(database.getWritableDatabase());
            }
        } else {

            Bundle extras = i.getExtras();

            if (extras != null) {
                connection.Gen_populate((ContentValues) extras
                        .getParcelable(VncConstants.CONNECTION));
            }
            if (connection.getPort() == 0)
                connection.setPort(5901);

            // Parse a HOST:PORT entry
            String host = connection.getAddress();
            if (host.indexOf(':') > -1) {
                String p = host.substring(host.indexOf(':') + 1);
                try {
                    connection.setPort(Integer.parseInt(p));
                } catch (Exception e) {
                }
                connection.setAddress(host.substring(0, host.indexOf(':')));
            }
        }
        super.onCreate(icicle);

        setContentView(initVideoPlayer());

        vncCanvas = (VncCanvas) findViewById(R.id.vnc_canvas);

        vncCanvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                EventBus.getDefault().post(new VncCanvas2FABEvent());

                return false;
            }
        });
        vncCanvas.registOnServerOrientationChangedListener(this);
        vncCanvas.requestFocus();
        zoomer = (ZoomControls) findViewById(R.id.zoomer);
        zoomer.hide();
        zoomer.setOnZoomInClickListener(new View.OnClickListener() {

            /*
             * (non-Javadoc)
             *
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v) {
                showZoomer(true);
                vncCanvas.scaling.zoomIn(VncCanvasActivity.this);

            }

        });
        zoomer.setOnZoomOutClickListener(new View.OnClickListener() {

            /*
             * (non-Javadoc)
             *
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v) {
                showZoomer(true);
                vncCanvas.scaling.zoomOut(VncCanvasActivity.this);
            }

        });
        zoomer.setOnZoomKeyboardClickListener(new View.OnClickListener() {

            /*
             * (non-Javadoc)
             *
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v) {
                InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.toggleSoftInput(0, 0);
                vncCanvas.requestFocus();
            }

        });
        panner = new Panner(this, vncCanvas.handler);

        inputHandler = getInputHandlerById(R.id.itemInputDPadPanTouchMouse);
        orientationDetector = new OrientationDetector(this);

        //FloatingActionButton();
        //initEvent();


        systemFABServiceIntent = new Intent(this, SystemFABService.class);

        //bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);

        startService(systemFABServiceIntent);


        //隐藏虚拟按键，并且全屏
        hideBottomUIMenu();

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FAB2SystemEvent event) {

        finish();

    };



    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }



    private SystemFABService systemFABService;

    private boolean isBind = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            SystemFABService.LocalBinder myBinder = (SystemFABService.LocalBinder)binder;

            systemFABService = myBinder.getService();

            isBind = true;



            systemFABService.setOnSystemFABStateChangeListener(new SystemFABService.OnSystemFABStateChangeListener() {
                @Override
                public void onClose() {

                    VncCanvasActivity.this.finish();
                }
            });


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //isBind = false;
            systemFABService = null;
            Log.d("service", "onServiceDisconnected: ");

        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        videoPlayer.onResume();
        vncCanvas.initializeVncCanvas(connection, new Runnable() {
            public void run() {
                setModes();

                if (vncCanvas.getMeasuredWidth() < vncCanvas.getMeasuredHeight() &&
                        vncCanvas.getFramebufferWidth() > vncCanvas.getFramebufferHeight()) {
                    //vncCanvas.rotateCanvas();
                   // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (vncCanvas.getMeasuredWidth() > vncCanvas.getMeasuredHeight() &&
                        vncCanvas.getFramebufferWidth() < vncCanvas.getFramebufferHeight()) {
                  //  vncCanvas.rotateCanvas();
                   // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                setTitle(vncCanvas.getDesktopName());
            }
        });
//        orientationDetector.enable();
    }

    /**
     * Set modes on start to match what is specified in the ConnectionBean;
     * color mode (already done) scaling, input mode
     */
    void setModes() {
//        showToastTips(connection
//                .getInputMode());
        AbstractInputHandler handler = getInputHandlerByName(connection
                .getInputMode());
        AbstractScaling.getByScaleType(connection.getScaleMode())
                .setScaleTypeForActivity(this);
        this.inputHandler = handler;
//        showPanningState();
    }

    ConnectionBean getConnection() {
        return connection;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.org.libsdl.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == R.layout.entertext) {
            return new EnterTextDialog(this);
        }
        // Default to meta key dialog
        return new MetaKeyDialog(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.org.libsdl.app.Activity#onPrepareDialog(int, android.org.libsdl.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (dialog instanceof ConnectionSettable)
            ((ConnectionSettable) dialog).setConnection(connection);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
//        vncCanvas.setRotation(270);
        Log.i("davy", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        vncCanvas.disableRepaints();
        videoPlayer.onStop();

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onRestart() {
        vncCanvas.enableRepaints();
        super.onRestart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vnccanvasactivitymenu, menu);

        if (vncCanvas.scaling != null)
            menu.findItem(vncCanvas.scaling.getId()).setChecked(true);

        Menu inputMenu = menu.findItem(R.id.itemInputMode).getSubMenu();

        inputModeMenuItems = new MenuItem[inputModeIds.length];
        for (int i = 0; i < inputModeIds.length; i++) {
            inputModeMenuItems[i] = inputMenu.findItem(inputModeIds[i]);
        }
        updateInputMenu();
        menu.findItem(R.id.itemFollowMouse).setChecked(
                connection.getFollowMouse());
        menu.findItem(R.id.itemFollowPan).setChecked(connection.getFollowPan());
        return true;
    }

    /**
     * Change the input mode sub-menu to reflect change in scaling
     */
    void updateInputMenu() {
        if (inputModeMenuItems == null || vncCanvas.scaling == null) {
            return;
        }
        for (MenuItem item : inputModeMenuItems) {
            item.setEnabled(vncCanvas.scaling
                    .isValidInputMode(item.getItemId()));
            if (getInputHandlerById(item.getItemId()) == inputHandler)
                item.setChecked(true);
        }
    }

    /**
     * If id represents an input handler, return that; otherwise return null
     *
     * @param id
     * @return
     */
    AbstractInputHandler getInputHandlerById(int id) {
        if (inputModeHandlers == null) {
            inputModeHandlers = new AbstractInputHandler[inputModeIds.length];
        }
        for (int i = 0; i < inputModeIds.length; ++i) {
            if (inputModeIds[i] == id) {
                if (inputModeHandlers[i] == null) {
                    if (id == R.id.itemInputFitToScreen) {
                        inputModeHandlers[i] = new FitToScreenMode();

                    } else if (id == R.id.itemInputPan) {
                        inputModeHandlers[i] = new PanMode();

                    } else if (id == R.id.itemInputMouse) {
                        inputModeHandlers[i] = new MouseMode();

                    } else if (id == R.id.itemInputTouchPanTrackballMouse) {
                        inputModeHandlers[i] = new TouchPanTrackballMouse();

                    } else if (id == R.id.itemInputDPadPanTouchMouse) {
                        inputModeHandlers[i] = new DPadPanTouchMouseMode();

                    } else if (id == R.id.itemInputTouchPanZoomMouse) {
                        inputModeHandlers[i] = new ZoomInputHandler();

                    } else if (id == R.id.itemInputTouchpad) {
                        inputModeHandlers[i] = new TouchpadInputHandler();

                    }
                }
                return inputModeHandlers[i];
            }
        }
        return null;
    }

    AbstractInputHandler getInputHandlerByName(String name) {
        AbstractInputHandler result = null;
        for (int id : inputModeIds) {
            AbstractInputHandler handler = getInputHandlerById(id);
            if (handler.getName().equals(name)) {
                result = handler;
                break;
            }
        }
        if (result == null) {
            result = getInputHandlerById(R.id.itemInputTouchPanZoomMouse);
        }
        return result;
    }

    int getModeIdFromHandler(AbstractInputHandler handler) {
        for (int id : inputModeIds) {
            if (handler == getInputHandlerById(id))
                return id;
        }
        return R.id.itemInputTouchPanZoomMouse;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        vncCanvas.afterMenu = true;
        int i = item.getItemId();
        if (i == R.id.itemInfo) {
            vncCanvas.showConnectionInfo();
            return true;
        } else if (i == R.id.itemSpecialKeys) {
            showDialog(R.layout.metakey);
            return true;
        } else if (i == R.id.itemColorMode) {
            selectColorModel();
            return true;
            // Following sets one of the scaling options
        } else if (i == R.id.itemZoomable || i == R.id.itemOneToOne || i == R.id.itemFitToScreen) {
            AbstractScaling.getById(item.getItemId()).setScaleTypeForActivity(
                    this);
            item.setChecked(true);
            showPanningState();
            return true;
        } else if (i == R.id.itemCenterMouse) {
            vncCanvas.warpMouse(vncCanvas.absoluteXPosition
                            + vncCanvas.getVisibleWidth() / 2,
                    vncCanvas.absoluteYPosition + vncCanvas.getVisibleHeight()
                            / 2);
            return true;
        } else if (i == R.id.itemDisconnect) {
            vncCanvas.closeConnection();
            finish();
            return true;
        } else if (i == R.id.showKeyboard) {
            InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMgr.toggleSoftInput(0, 0);
            return true;
        } else if (i == R.id.itemEnterText) {
            showDialog(R.layout.entertext);
            return true;
        } else if (i == R.id.itemCtrlAltDel) {
            vncCanvas.sendMetaKey(MetaKeyBean.keyCtrlAltDel);
            return true;
        } else if (i == R.id.itemFollowMouse) {
            boolean newFollow = !connection.getFollowMouse();
            item.setChecked(newFollow);
            connection.setFollowMouse(newFollow);
            if (newFollow) {
                vncCanvas.panToMouse();
            }
            connection.save(database.getWritableDatabase());
            return true;
        } else if (i == R.id.itemFollowPan) {
            boolean newFollowPan = !connection.getFollowPan();
            item.setChecked(newFollowPan);
            connection.setFollowPan(newFollowPan);
            connection.save(database.getWritableDatabase());
            return true;
        } else if (i == R.id.itemArrowLeft) {
            vncCanvas.sendMetaKey(MetaKeyBean.keyArrowLeft);
            return true;
        } else if (i == R.id.itemArrowUp) {
            vncCanvas.sendMetaKey(MetaKeyBean.keyArrowUp);
            return true;
        } else if (i == R.id.itemArrowRight) {
            vncCanvas.sendMetaKey(MetaKeyBean.keyArrowRight);
            return true;
        } else if (i == R.id.itemArrowDown) {
            vncCanvas.sendMetaKey(MetaKeyBean.keyArrowDown);
            return true;
        } else if (i == R.id.itemSendKeyAgain) {
            sendSpecialKeyAgain();
            return true;
        } else if (i == R.id.itemOpenDoc) {
            Utils.showDocumentation(this);
            return true;
        } else {
            AbstractInputHandler input = getInputHandlerById(item.getItemId());
            if (input != null) {
                inputHandler = input;
                connection.setInputMode(input.getName());
                if (input.getName().equals(TOUCHPAD_MODE))
                    connection.setFollowMouse(true);
                item.setChecked(true);
                showPanningState();
                connection.save(database.getWritableDatabase());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private MetaKeyBean lastSentKey;

    private void sendSpecialKeyAgain() {
        if (lastSentKey == null
                || lastSentKey.get_Id() != connection.getLastMetaKeyId()) {
            ArrayList<MetaKeyBean> keys = new ArrayList<MetaKeyBean>();
            Cursor c = database.getReadableDatabase().rawQuery(
                    MessageFormat.format("SELECT * FROM {0} WHERE {1} = {2}",
                            MetaKeyBean.GEN_TABLE_NAME,
                            MetaKeyBean.GEN_FIELD__ID, connection
                                    .getLastMetaKeyId()),
                    MetaKeyDialog.EMPTY_ARGS);
            MetaKeyBean.Gen_populateFromCursor(c, keys, MetaKeyBean.NEW);
            c.close();
            if (keys.size() > 0) {
                lastSentKey = keys.get(0);
            } else {
                lastSentKey = null;
            }
        }
        if (lastSentKey != null)
            vncCanvas.sendMetaKey(lastSentKey);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (isFinishing()) {
        vncCanvas.unregistOnServerOrientationChangedListener();
        vncCanvas.closeConnection();
        vncCanvas.onDestroy();
        database.close();
//        }
        videoPlayer.onDestroy();


       /* if (isBind){
            Log.d("service", "vnccanvasonDestroy: ");
            unbindService(serviceConnection);
        }*/

        //stopService(systemFABServiceIntent);

        Log.d("service", "stopService: ");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent evt) {
//        if (keyCode == KeyEvent.KEYCODE_MENU)
        return super.onKeyDown(keyCode, evt);

//        return inputHandler.onKeyDown(keyCode, evt);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent evt) {
//        if (keyCode == KeyEvent.KEYCODE_MENU)
        return super.onKeyUp(keyCode, evt);

//        return inputHandler.onKeyUp(keyCode, evt);
    }

    public void showPanningState() {
        Toast.makeText(this, inputHandler.getHandlerDescription(),
                Toast.LENGTH_SHORT).show();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.org.libsdl.app.Activity#onTrackballEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                trackballButtonDown = true;
                break;
            case MotionEvent.ACTION_UP:
                trackballButtonDown = false;
                break;
        }
        return inputHandler.onTrackballEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return inputHandler.onTouchEvent(event);
    }

    private void selectColorModel() {
        // Stop repainting the desktop
        // because the display is composited!
        vncCanvas.disableRepaints();

        String[] choices = new String[COLORMODEL.values().length];
        int currentSelection = -1;
        for (int i = 0; i < choices.length; i++) {
            COLORMODEL cm = COLORMODEL.values()[i];
            choices[i] = cm.toString();
            if (vncCanvas.isColorModel(cm))
                currentSelection = i;
        }

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ListView list = new ListView(this);
        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked, choices));
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setItemChecked(currentSelection, true);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                dialog.dismiss();
                COLORMODEL cm = COLORMODEL.values()[arg2];
                vncCanvas.setColorModel(cm);
                connection.setColorModel(cm.nameString());
                connection.save(database.getWritableDatabase());
                Toast.makeText(VncCanvasActivity.this,
                        "Updating Color Model to " + cm.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                Log.i(TAG, "Color Model Selector dismissed");
                // Restore desktop repaints
                vncCanvas.enableRepaints();
            }
        });
        dialog.setContentView(list);
        dialog.show();
    }

    float panTouchX, panTouchY;

    /**
     * Pan based on touch motions
     *
     * @param event
     */
    private boolean pan(MotionEvent event) {
        float curX = event.getX();
        float curY = event.getY();
        int dX = (int) (panTouchX - curX);
        int dY = (int) (panTouchY - curY);

        return vncCanvas.pan(dX, dY);
    }

    boolean defaultKeyDownHandler(int keyCode, KeyEvent evt) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (vncCanvas.processLocalKeyEvent(keyCode, evt))
                return true;
        }
        return super.onKeyDown(keyCode, evt);
    }

    boolean defaultKeyUpHandler(int keyCode, KeyEvent evt) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (vncCanvas.processLocalKeyEvent(keyCode, evt))
                return true;
        }
        return super.onKeyUp(keyCode, evt);
    }

    boolean touchPan(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                panTouchX = event.getX();
                panTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                pan(event);
                panTouchX = event.getX();
                panTouchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                pan(event);
                break;
        }
        return true;
    }

    private static int convertTrackballDelta(double delta) {
        return (int) Math.pow(Math.abs(delta) * 6.01, 2.5)
                * (delta < 0.0 ? -1 : 1);
    }

    boolean trackballMouse(MotionEvent evt) {
        int dx = convertTrackballDelta(evt.getX());
        int dy = convertTrackballDelta(evt.getY());

        evt.offsetLocation(vncCanvas.mouseX + dx - evt.getX(), vncCanvas.mouseY
                + dy - evt.getY());

        if (vncCanvas.processPointerEvent(evt, trackballButtonDown)) {
            return true;
        }
        return VncCanvasActivity.super.onTouchEvent(evt);
    }

    long hideZoomAfterMs;
    static final long ZOOM_HIDE_DELAY_MS = 2500;
    HideZoomRunnable hideZoomInstance = new HideZoomRunnable();

    private void showZoomer(boolean force) {
        if (force || zoomer.getVisibility() != View.VISIBLE) {
            zoomer.show();
            hideZoomAfterMs = SystemClock.uptimeMillis() + ZOOM_HIDE_DELAY_MS;
            vncCanvas.handler
                    .postAtTime(hideZoomInstance, hideZoomAfterMs + 10);
        }
    }

    private class HideZoomRunnable implements Runnable {
        public void run() {
            if (SystemClock.uptimeMillis() >= hideZoomAfterMs) {
                zoomer.hide();
            }
        }

    }

    /**
     * Touches and dpad (trackball) pan the screen
     *
     * @author Michael A. MacDonald
     */
    class PanMode implements AbstractInputHandler {

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            // DPAD KeyDown events are move MotionEvents in Panning Mode
            final int dPos = 100;
            boolean result = false;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    result = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    onTouchEvent(MotionEvent
                            .obtain(1, System.currentTimeMillis(),
                                    MotionEvent.ACTION_MOVE, panTouchX + dPos,
                                    panTouchY, 0));
                    result = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    onTouchEvent(MotionEvent
                            .obtain(1, System.currentTimeMillis(),
                                    MotionEvent.ACTION_MOVE, panTouchX - dPos,
                                    panTouchY, 0));
                    result = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    onTouchEvent(MotionEvent
                            .obtain(1, System.currentTimeMillis(),
                                    MotionEvent.ACTION_MOVE, panTouchX, panTouchY
                                            + dPos, 0));
                    result = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    onTouchEvent(MotionEvent
                            .obtain(1, System.currentTimeMillis(),
                                    MotionEvent.ACTION_MOVE, panTouchX, panTouchY
                                            - dPos, 0));
                    result = true;
                    break;
                default:
                    result = defaultKeyDownHandler(keyCode, evt);
                    break;
            }
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            // Ignore KeyUp events for DPAD keys in Panning Mode; trackball
            // button switches to mouse mode
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    inputHandler = getInputHandlerById(R.id.itemInputMouse);
                    connection.setInputMode(inputHandler.getName());
                    connection.save(database.getWritableDatabase());
                    updateInputMenu();
                    showPanningState();
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    return true;
            }
            return defaultKeyUpHandler(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return touchPan(event);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#handlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getText(R.string.input_mode_panning);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return "PAN_MODE";
        }

    }

    /**
     * The touchscreen pans the screen; the trackball moves and clicks the
     * mouse.
     *
     * @author Michael A. MacDonald
     */
    public class TouchPanTrackballMouse implements AbstractInputHandler {
        private DPadMouseKeyHandler keyHandler = new DPadMouseKeyHandler(VncCanvasActivity.this, vncCanvas.handler);

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyDown(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyUp(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return touchPan(evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return trackballMouse(evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#handlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getText(
                    R.string.input_mode_touchpad_pan_trackball_mouse);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return "TOUCH_PAN_TRACKBALL_MOUSE";
        }

    }

    static final String FIT_SCREEN_NAME = "FIT_SCREEN";
    /**
     * Internal name for default input mode with Zoom scaling
     */
    static final String TOUCH_ZOOM_MODE = "TOUCH_ZOOM_MODE";

    static final String TOUCHPAD_MODE = "TOUCHPAD_MODE";

    /**
     * In fit-to-screen mode, no panning. Trackball and touchscreen work as
     * mouse.
     *
     * @author Michael A. MacDonald
     */
    public class FitToScreenMode implements AbstractInputHandler {
        private DPadMouseKeyHandler keyHandler = new DPadMouseKeyHandler(VncCanvasActivity.this, vncCanvas.handler);

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyDown(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            return keyHandler.onKeyUp(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return trackballMouse(evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#handlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getText(R.string.input_mode_fit_to_screen);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return FIT_SCREEN_NAME;
        }

    }

    /**
     * Touch screen controls, clicks the mouse.
     *
     * @author Michael A. MacDonald
     */
    class MouseMode implements AbstractInputHandler {

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                return true;
            return defaultKeyDownHandler(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                inputHandler = getInputHandlerById(R.id.itemInputPan);
                showPanningState();
                connection.setInputMode(inputHandler.getName());
                connection.save(database.getWritableDatabase());
                updateInputMenu();
                return true;
            }
            return defaultKeyUpHandler(keyCode, evt);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Mouse Pointer Control Mode
            // Pointer event is absolute coordinates.

            vncCanvas.changeTouchCoordinatesToFullFrame(event);
            if (vncCanvas.processPointerEvent(event, true))
                return true;
            return VncCanvasActivity.super.onTouchEvent(event);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#handlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getText(R.string.input_mode_mouse);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return "MOUSE";
        }

    }

    /**
     * Touch screen controls, clicks the mouse. DPad pans the screen
     *
     * @author Michael A. MacDonald
     */
    class DPadPanTouchMouseMode implements AbstractInputHandler {

        private boolean isPanning;

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyDown(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent evt) {
            int xv = 0;
            int yv = 0;
            boolean result = true;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    xv = -1;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    xv = 1;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    yv = -1;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    yv = 1;
                    break;
                default:
                    result = defaultKeyDownHandler(keyCode, evt);
                    break;
            }
            if ((xv != 0 || yv != 0) && !isPanning) {
                final int x = xv;
                final int y = yv;
                isPanning = true;
                panner.start(x, y, new Panner.VelocityUpdater() {

                    /*
                     * (non-Javadoc)
                     *
                     * @see android.androidVNC.Panner.VelocityUpdater#updateVelocity(android.graphics.Point,
                     *      long)
                     */
                    @Override
                    public boolean updateVelocity(PointF p, long interval) {
                        double scale = (2.0 * (double) interval / 50.0);
                        if (Math.abs(p.x) < 500)
                            p.x += (int) (scale * x);
                        if (Math.abs(p.y) < 500)
                            p.y += (int) (scale * y);
                        return true;
                    }

                });
                vncCanvas.pan(x, y);
            }
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onKeyUp(int,
         *      android.view.KeyEvent)
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent evt) {
            boolean result = false;

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    panner.stop();
                    isPanning = false;
                    result = true;
                    break;
                default:
                    result = defaultKeyUpHandler(keyCode, evt);
                    break;
            }
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Mouse Pointer Control Mode
            // Pointer event is absolute coordinates.

//            vncCanvas.changeTouchCoordinatesToFullFrame(event);
//            event.setLocation(event.getX(), event.getY() - 160);
            if (vncCanvas.processPointerEvent(event, true))
                return true;
            return VncCanvasActivity.super.onTouchEvent(event);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#onTrackballEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTrackballEvent(MotionEvent evt) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#handlerDescription()
         */
        @Override
        public CharSequence getHandlerDescription() {
            return getResources().getText(
                    R.string.input_mode_dpad_pan_touchpad_mouse);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.androidVNC.AbstractInputHandler#getName()
         */
        @Override
        public String getName() {
            return "DPAD_PAN_TOUCH_MOUSE";
        }

    }
}


