package com.huawei.padresourcepool.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huawei.padresourcepool.R;
import com.huawei.padresourcepool.adapter.DetailListAdapter;
import com.huawei.padresourcepool.bean.DetailListBean;

import java.util.ArrayList;
import java.util.List;

public class DetailListActivityBackup1 extends AppCompatActivity {

    private static final String TAG = "DetailListActivityBackup1";
    private RecyclerView recyclerView;
    private DetailListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<DetailListBean> datas = new ArrayList<>();
    boolean isLoading;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //TODO 动态设置title
        getSupportActionBar().setTitle("我的Engage package");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        initView();

        initEvent();

        initData();

    }

    private void initData() {
        getData();

    }

    private void getData(){
       /* for (int i = 0; i < 4; i++) {
                datas.add(""+i);
        }*/

        /*DetailListBean bean1 = new DetailListBean("韩国LGU+移动视频", "(杜塞)", "UTC-08:00", "2017-02-20 09:00-11:00", R.drawable.item_detaillist_bg1
                ,"rtsp://10.224.20.28:5000/phone1","10.224.28.12",0,"5901");
        DetailListBean bean2 = new DetailListBean("土耳其Turkcell", "(杜塞)", "UTC-08:00", "2017-02-20 09:00-11:00", R.drawable.item_detaillist_bg2
                ,"rtsp://10.224.20.28:5000/phone2","10.224.28.69",1,"5901");*/

        DetailListBean bean3 = new DetailListBean("韩国LGU+移动视频", "(深圳)", "UTC-08:00", "2017-02-20 09:00-11:00", R.drawable.item_detaillist_bg3
                ,"rtsp://10.56.26.144:5000/phone2","10.56.5.11",2,"5901");

        DetailListBean bean4 = new DetailListBean("菲律宾Globe", "(深圳)", "UTC-08:00", "2017-02-20 09:00-11:00", R.drawable.item_detaillist_bg4
                ,"rtsp://10.56.26.144:5000/phone1","10.56.5.107",3,"5901");

       /* datas.add(bean1);
        datas.add(bean2);*/

        datas.add(bean3);
        datas.add(bean4);

        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        adapter.notifyItemRemoved(adapter.getItemCount());

    }

    private void initEvent() {


        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new DetailListAdapter(this, datas);

        recyclerView.setAdapter(adapter);


        /*//item点击事件
        adapter.setOnItemClickListener(new DetailListAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {

                //直接播放
                Intent intent = new Intent(DetailListActivityBackup1.this, SettingActivity.class);

                //传递参数

                startActivity(intent);
            }
        });*/


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        datas.clear();
                        getData();

                       // Toast.makeText(DetailListActivityBackup1.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();

                    }
                },1500);


            }
        });

      /*  recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisibleItemPosition + 1 == adapter.getItemCount()) {

                    Log.d(TAG, "loading executed");

                    boolean isRefreshing = swipeRefreshLayout.isRefreshing();
                    if (isRefreshing) {
                        adapter.notifyItemRemoved(adapter.getItemCount());
                        return;
                    }
                    if (!isLoading) {
                        isLoading = true;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getData();
                                Log.d(TAG, "load more completed");
                                isLoading = false;
                            }
                        }, 1500);
                    }
                }


            }
        });*/

    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);


        //////////////////////
        swipeRefreshLayout.setEnabled(false);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.item1:
                //Toast.makeText(this,"点击了item1",Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
