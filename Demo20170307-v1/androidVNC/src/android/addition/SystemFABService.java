package android.addition;

import android.androidVNC.R;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by mWX435313 on 2017/3/14.
 */

public class SystemFABService extends Service {

    private static final String TAG = "SystemFABService";


    private  IBinder mBinder = new LocalBinder();

    private FloatingActionButton actionButton;
    private FloatingActionMenu actionMenu;
    private ImageView icon;

    private boolean serviceWillBeDismissed;

    private int screenWidth;
    private int screenHeight;
    private int screenWidthHalf;
    private int statusHeight;


    public SystemFABService(){}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {

        public SystemFABService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SystemFABService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceWillBeDismissed = false;

        FloatingActionButton();

        Log.d("service", "onCreate: ");

        EventBus.getDefault().register(this);

        init();
        //initEvent();
        initListener();

    }


    private int mLastX;
    private int mLastY;

    private void initListener() {

        Log.d(TAG, "initListener: ");

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
                        if ( actionMenu.isOpen()){
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
                            if(event.getRawX()>=screenWidthHalf){

                                from = (int) event.getRawX();

                                //startAnimaLeft(from,screenWidth-v.getWidth());
                            }else {

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

    private int lastX;
    private int lastY;
    private int mDownX;
    private int mDownY;

    private boolean isDrag;

    private void initEvent() {

        Log.d("service", "initEvent: ");

        actionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:

                        mDownX = (int) event.getRawX();
                        mDownY = (int) event.getRawY();

//                        isDrag=false;

                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        lastX=rawX;
                        lastY=rawY;
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        isDrag=true;

                        //计算手指移动了多少
                        int dx=rawX-lastX;
                        int dy=rawY-lastY;
                        //这里修复一些华为手机无法触发点击事件的问题
                        int distance= (int) Math.sqrt(dx*dx+dy*dy);
                        if(distance==0){
                            isDrag=false;
                            break;
                        }

                        float x=v.getX()+dx;
                        float y=v.getY()+dy;
                        //检测是否到达边缘 左上右下
                        x=x<0?0:x>screenWidth-v.getWidth()?screenWidth-v.getWidth():x;
                        y=y<statusHeight?statusHeight:y+v.getHeight()>screenHeight?screenHeight-v.getHeight():y;
                        v.setX(x);
                        v.setY(y);
                        lastX=rawX;
                        lastY=rawY;
                        //Log.i("getX="+getX()+";getY="+getY()+";screenHeight="+screenHeight);
                        break;
                    case MotionEvent.ACTION_UP:

//                        if(isDrag){


                            //恢复按压效果
                            //v.setPressed(false);

                            //Log.i("getX="+getX()+"；screenWidthHalf="+screenWidthHalf);

                            //检测移动的距离，如果很微小可以认为是点击事件
                            if (Math.abs(event.getRawX() - mDownX) < 10 && Math.abs(event.getRawY() - mDownY) < 10) {

                                actionButton.callOnClick();

                            } else {

                                if(rawX>=screenWidthHalf){
                                    v.animate().setInterpolator(new DecelerateInterpolator())
                                            .setDuration(500)
                                            .xBy(screenWidth-v.getWidth()-v.getX())
                                            .start();
                                }else {
                                    ObjectAnimator oa=ObjectAnimator.ofFloat(this,"x",v.getX(),0);
                                    oa.setInterpolator(new DecelerateInterpolator());
                                    oa.setDuration(500);
                                    oa.start();
                                }


                            }


//                        }
                        break;
                }
                //如果是拖拽则消耗事件，否则正常传递即可。
                return isDrag || true;
            }
        });


        actionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
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

    private void init() {

        WindowManager wm  = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;
        screenWidthHalf=screenWidth/2;
        screenHeight = metric.heightPixels;
        statusHeight = getStatusHeight();

    }

    private int getStatusHeight(){
        /**
         * 获取状态栏高度——方法2
         * */
        int statusBarHeight = 0;

        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("service", "状态栏高度:" + statusBarHeight);

        return statusBarHeight;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VncCanvas2FABEvent event) {
        if (actionMenu!=null&&actionMenu.isOpen()){
            actionMenu.close(true);
        }
    };


    private void FloatingActionButton() {
        // Create a button to attach the menu:
        // Create an icon
        icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_action_new_light);

        WindowManager.LayoutParams params = FloatingActionButton.Builder.getDefaultSystemWindowParams(this);


        actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .setSystemOverlay(true)
                .setLayoutParams(params)
                .build();

        SubActionButton button1 = buildSubActionButton(this, R.drawable.ic_action_back_light);
        SubActionButton button2 = buildSubActionButton(this, R.drawable.ic_action_camera_light);
        SubActionButton button3 = buildSubActionButton(this, R.drawable.ic_action_video_light);



        //Create the menu with the items:
        actionMenu = new FloatingActionMenu.Builder(this,true)
                .addSubActionView(button1,button1.getLayoutParams().width, button1.getLayoutParams().height)
                .addSubActionView(button2,button2.getLayoutParams().width, button2.getLayoutParams().height)
                .addSubActionView(button3,button3.getLayoutParams().width, button3.getLayoutParams().height)
                .setStartAngle(180)
                .setEndAngle(270)
                .attachTo(actionButton)
                .build();
//        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
//        WindowManager mWindowManager = (WindowManager)VncCanvasActivity.this.getSystemService(getApplication().WINDOW_SERVICE);
//        mWindowManager.addView(actionButton, wmParams);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EventBus.getDefault().post(new FAB2SystemEvent());

                serviceWillBeDismissed = true;

                actionMenu.close(true);

                // TODO 关闭页面

                Log.d("service", "stopSelf: ");

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

        actionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {

            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {

                if(serviceWillBeDismissed) {
                    SystemFABService.this.stopSelf();
                    serviceWillBeDismissed = false;
                }

            }
        });
    }


    private SubActionButton buildSubActionButton(Context context, int iconResourceId) {
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(context);
        ImageView itemIcon = new ImageView(context);
        itemIcon.setImageResource(iconResourceId);
        return itemBuilder.setContentView(itemIcon).build();
    }

    @Override
    public void onDestroy() {

        Log.d("service", "serviceonDestroy: ");

        if(actionMenu != null && actionMenu.isOpen()) actionMenu.close(false);

        if(actionButton != null) actionButton.detach();

        EventBus.getDefault().unregister(this);

        super.onDestroy();


    }

    public interface OnSystemFABStateChangeListener{
          void onClose();
    }

    private OnSystemFABStateChangeListener onSystemFABStateChangeListener;

    public void setOnSystemFABStateChangeListener(OnSystemFABStateChangeListener listener){
        this.onSystemFABStateChangeListener = listener;
    }

}
