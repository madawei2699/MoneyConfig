package com.mdw.moneyconfig.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Properties;

/**
 * 常量类
 * @author zFish
 *
 */

public class Constant {

    // DataService线程执行完毕
    public static final int DATASERVICEOK = 0;
    // 查询费率线程执行完毕
    public static final int SEARCHSERVICEOK = 1;
    // 查询基金历史净值线程执行完毕
    public static final int FUNDPRICEOK = 2;

}
