package com.huawei.padresourcepool.adapter;

import android.androidVNC.BitmapImplHint;
import android.androidVNC.ConnectionBean;
import android.androidVNC.VncCanvasActivity;
import android.androidVNC.VncConstants;
import android.androidVNC.VncDatabase;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.huawei.padresourcepool.R;
import com.huawei.padresourcepool.activity.DetailMessageActivity;
import com.huawei.padresourcepool.activity.SettingActivity;
import com.huawei.padresourcepool.bean.DetailListBean;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mWX435313 on 2017/2/6.
 */

public class DetailListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static String PLAY_PARAMES_INFO = "PlayParamesInfo";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private  Context context;
    private  List<DetailListBean> datas;
    //保存拉出item的集合
    private List<SwipeLayout> openItems;

    private VncDatabase database;
    private ConnectionBean connectionBean;
    SharedPreferences prf;

    public DetailListAdapter(Context context, List<DetailListBean> datas){
        this.context = context;
        this.datas = datas;
        openItems = new ArrayList<>();

        //播放器的参数
        prf = context.getSharedPreferences("config", MODE_PRIVATE);
        database = new VncDatabase(context);
        connectionBean = new ConnectionBean();

    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            ItemViewHolder holder = new ItemViewHolder(LayoutInflater.from(
                    context).inflate(R.layout.item_detaillist_recycleview, parent,
                    false));
            return holder;

        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_foot_detaillist_recycleview, parent,
                    false);
            return new FootViewHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder,  int position) {

        if (holder instanceof ItemViewHolder) {
            DetailListBean bean = datas.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.iv_icon.setImageResource(bean.resId);
            itemViewHolder.tv_title.setText(bean.title);
            itemViewHolder.tv_location.setText(bean.location);
            itemViewHolder.tv_des.setText(bean.des);
            itemViewHolder.tv_time.setText(bean.time);

            initEvent((ItemViewHolder) holder);


            /*if (onItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getLayoutPosition();
                        onItemClickListener.onItemClickListener(holder.itemView, position);
                    }
                });

            }*/

        }

    }


    @Override
    public int getItemCount() {
        return datas==null?0:datas.size();
        //return datas==null?0:datas.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        /*if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }*/

        return TYPE_ITEM;
    }

    static  class ItemViewHolder extends RecyclerView.ViewHolder {


        private ImageView iv_icon;
        private TextView tv_title;
        private TextView tv_location;
        private TextView tv_des;
        private TextView tv_time;
        private View v;
        SwipeLayout swipeLayout;
        TextView tv_edit;

        public ItemViewHolder(View view) {
            super(view);
            v = view;
            iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_location = (TextView) view.findViewById(R.id.tv_location);
            tv_des = (TextView) view.findViewById(R.id.tv_des);
            tv_time = (TextView) view.findViewById(R.id.tv_time);

            swipeLayout = (SwipeLayout) view.findViewById(R.id.swipeLayout);
            tv_edit = (TextView) view.findViewById(R.id.tv_edit);
        }
    }

    static class FootViewHolder extends RecyclerView.ViewHolder {

        public FootViewHolder(View view) {
            super(view);
        }
    }


   /* public interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }*/


    private void initEvent(final ItemViewHolder holder) {



        //条目的点击事件
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getOpenItems()>0){
                    closeAllLayout();
                }else{
                    //Toast.makeText(context, "itemClick" + holder.tv_title.getText().toString(), Toast.LENGTH_SHORT).show(); //条目的点击事件

                    //获取当前holder位置
                    int position = holder.getLayoutPosition();
                    DetailListBean bean = datas.get(position);

                    //跳转到播放界面
                    Intent intent = new Intent(context, DetailMessageActivity.class);
                    intent.putExtra(DetailListAdapter.PLAY_PARAMES_INFO,bean);
                    context.startActivity(intent);


                }

            }
        });

        final SwipeLayout swipeLayout = holder.swipeLayout;

        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout swipeLayout) {
                //先去关闭打开的（如果没有打开的直接返回）
                closeAllLayout();
                openItems.add(swipeLayout); //保留

            }

            @Override
            public void onOpen(SwipeLayout swipeLayout) {
                //添加
                openItems.add(swipeLayout);

            }

            @Override
            public void onStartClose(SwipeLayout swipeLayout) {

            }

            @Override
            public void onClose(SwipeLayout swipeLayout) {
                //移除
                openItems.remove(swipeLayout);


            }

            @Override
            public void onUpdate(SwipeLayout swipeLayout, int i, int i1) {

            }

            @Override
            public void onHandRelease(SwipeLayout swipeLayout, float v, float v1) {

            }
        });

        //编辑功能
        holder.tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = holder.getLayoutPosition();
                //Toast.makeText(context, "编辑", Toast.LENGTH_SHORT).show();
                swipeLayout.close();
                //跳转到设置界面
                DetailListBean bean = datas.get(position);

                Intent intent = new Intent(context, SettingActivity.class);
                intent.putExtra(DetailListAdapter.PLAY_PARAMES_INFO,bean);
                context.startActivity(intent);

            }
        });

    }

    /**
     * 关闭所有的layout
     */
    public void closeAllLayout() {
        if (openItems.size() == 0) {
            return;
        }
        for (SwipeLayout l : openItems) {
            l.close();
        }
        openItems.clear();
    }

    //获取拉开item的数目
    public int getOpenItems() {
        return openItems.size();
    }


    private void startVnc(String vnc_ip, String vnc_port, String url) {

        connectionBean.setAddress(vnc_ip);
        connectionBean.setForceFull(BitmapImplHint.AUTO);
        connectionBean.setPort(Integer.parseInt(vnc_port));
        saveAndWriteRecent();
        Intent intent = new Intent(context, VncCanvasActivity.class);
        intent.putExtra(VncConstants.CONNECTION, connectionBean.Gen_getValues());
        intent.putExtra("videoPath", url);
        intent.putExtra("proto", 1);
        context.startActivity(intent);
    }

    private void saveAndWriteRecent() {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            connectionBean.save(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}

