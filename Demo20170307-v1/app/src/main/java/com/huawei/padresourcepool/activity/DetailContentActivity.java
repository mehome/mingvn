package com.huawei.padresourcepool.activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.padresourcepool.R;
import com.huawei.padresourcepool.adapter.DetailListAdapter;
import com.huawei.padresourcepool.bean.PlayParamesInfo;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

public class DetailContentActivity extends AppCompatActivity {


    private static final String TAG = "DetailContentActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_content);
        init();
        FloatingActionButton();
        initData();
        initEvent();


    }

    public void init(){
        Intent intent = getIntent();
        PlayParamesInfo info = (PlayParamesInfo) intent.getSerializableExtra(DetailListAdapter.PLAY_PARAMES_INFO);

        TextView tv_info = (TextView) findViewById(R.id.tv_info);
        tv_info.setText(info.ip+" "+info.port+" "+info.url);


    }

    private void initData() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;
        screenHeight = metric.heightPixels;

       screenWidthHalf = screenWidth /2;

        Log.d(TAG, " screenWidth:" + screenWidth + " screenHeight:" + screenHeight);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_new_light);

        Log.d(TAG, " bitmapWidth:" + bitmap.getWidth() + " bitmapHeight:" + bitmap.getHeight());

    }

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

    private void FloatingActionButton() {
        // Create a button to attach the menu:
        // Create an icon
        icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_action_new_light);

        actionButton = new FloatingActionButton.Builder(this)
                .setContentView(icon)
                .build();

        SubActionButton button1 = buildSubActionButton(DetailContentActivity.this, R.drawable.ic_action_back_light);
        SubActionButton button2 = buildSubActionButton(DetailContentActivity.this, R.drawable.ic_action_camera_light);
        SubActionButton button3 = buildSubActionButton(DetailContentActivity.this, R.drawable.ic_action_video_light);

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

    }

    private SubActionButton buildSubActionButton(Activity activity, int iconResourceId) {
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(activity);
        ImageView itemIcon = new ImageView(activity);
        itemIcon.setImageResource(iconResourceId);
        return itemBuilder.setContentView(itemIcon).build();
    }


}
