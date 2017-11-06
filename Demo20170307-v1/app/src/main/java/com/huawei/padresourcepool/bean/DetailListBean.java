package com.huawei.padresourcepool.bean;

import java.io.Serializable;

/**
 * Created by mWX435313 on 2017/2/13.
 */

public class DetailListBean implements Serializable {
    public int position;
    public String title;
    public String location;
    public String des;
    public String time;
    public int resId;
    public String url;
    public String vnc;
    public String port;

    public DetailListBean(String title, String location, String des, String time, int resId, String url, String vnc,int position,String port) {
        this.title = title;
        this.location = location;
        this.des = des;
        this.time = time;
        this.resId = resId;
        this.url = url;
        this.vnc = vnc;
        this.position = position;
        this.port = port;
    }

    public DetailListBean(){};

}
