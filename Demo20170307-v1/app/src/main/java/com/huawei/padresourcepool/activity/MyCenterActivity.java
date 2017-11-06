package com.huawei.padresourcepool.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huawei.padresourcepool.R;
import com.huawei.padresourcepool.adapter.MyCenterAdapter;
import com.huawei.padresourcepool.widget.DragLayout;

import java.util.ArrayList;
import java.util.List;

public class MyCenterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "MyCenterActivity";
    private RecyclerView mycenter_recycle;
    private DragLayout drag_layout;
    private MyCenterAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<String> datas = new ArrayList<>();
    private Handler handler = new Handler();
    boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_center);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("我的Engage package");

        drag_layout = (DragLayout) findViewById(R.id.drag_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drag_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);


            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);

                //关闭打开的item
                adapter.closeAllLayout();
            }
        };


        drag_layout.setDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        initView();

        initEvent();

        initData();



    }

    private void initEvent() {

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mycenter_recycle.setLayoutManager(layoutManager);
        mycenter_recycle.setItemAnimator(new DefaultItemAnimator());

        adapter = new MyCenterAdapter(this, datas);

        drag_layout.setAdapterInterface(adapter);

        mycenter_recycle.setAdapter(adapter);



        //当recycleView上下滑动的时候，把拉出的item关闭
        /*mycenter_recycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    adapter.closeAllLayout();
                }
            }
        });
        */



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        datas.clear();
                        getData();

                       // Toast.makeText(MyCenterActivity.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();

                    }
                },1500);


            }
        });

    }

    private void initData() {

        getData();

    }

    private void getData(){
        for (int i = 0; i < 1; i++)
        {
            //datas.add("拜访内蒙古电信" + i);
            datas.add("拜访内蒙古电信");
        }

        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        adapter.notifyItemRemoved(adapter.getItemCount());

    }

    private void initView() {

        mycenter_recycle = (RecyclerView) findViewById(R.id.mycenter_recycle);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        ////////////
        swipeRefreshLayout.setEnabled(false);

    }

    @Override
    public void onBackPressed() {

        if (drag_layout.isDrawerOpen(GravityCompat.START)) {
            drag_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drag_layout.closeDrawer(GravityCompat.START);
        return true;
    }

}
