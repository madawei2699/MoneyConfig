package com.mdw.moneyconfig.utils;

import android.app.Application;

/**
 * 在任意位置使用MyApplication.getInstance()获取context
 * @author zFish
 *
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}