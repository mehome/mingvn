package com.huawei.padresourcepool.widget;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.huawei.padresourcepool.adapter.MyCenterAdapter;

/**
 * Created by mWX435313 on 2017/2/6.
 *
 * 处理侧滑删除被取消的时候与侧滑菜单被拉出的事件冲突
 */

public class DragLayout extends DrawerLayout {

    private MyCenterAdapter adapter;


    public DragLayout(Context context) {
        this(context,null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAdapterInterface(MyCenterAdapter adapter) {
        this.adapter = adapter;
    }


    float mDownX ;
    /**
     * 2、传递触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if(!isDrawerOpen(GravityCompat.START)){

            /*switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //获取到按压的是否的x坐标
                    mDownX = ev.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //如果在移动的时候，item打开的个数大于0，就不拦截事件
                    if(adapter.getOpenItems() > 0){
                        return false;
                    }
                    //如果是向左滑，就不拦截事件(不用判断也行)
                    float delta = ev.getRawX() - mDownX;
                    if(delta < 0){
                        return false;
                    }
                    break;
                default:
                    mDownX = 0;
                    break;
            }*/
        }
        return super.onInterceptTouchEvent(ev);
    }

}
