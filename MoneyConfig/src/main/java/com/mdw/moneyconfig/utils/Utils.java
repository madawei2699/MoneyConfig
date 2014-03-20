package com.mdw.moneyconfig.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Properties;

/**
 * 工具类
 * @author zFish
 *
 */

public class Utils {

	private static String value;

    /**
     * 获取config参数值
     * @param name
     * @return
     */
	public static String getPropertiesURL(String name){
		
		Properties properties = new Properties();
	    try 
	    {
		   properties.load(MyApplication.getInstance().getAssets().open("config.properties"));
		   value = properties.getProperty(name);
		}
	    catch(Exception e) 
	    {
		   e.printStackTrace();
		}
		return value;
	}

    /**
     * 判断网络是否可用
     * @param
     * @return
     */
    public static boolean detectNetwork() {

        ConnectivityManager manager = (ConnectivityManager)
                MyApplication.getInstance().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }
}
