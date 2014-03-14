package com.mdw.moneyconfig;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 打开主界面
                Intent MainActivityIntent = new Intent();
                //MainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                //MainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivityIntent.setClass(SplashScreen.this,MainActivity.class);
                startActivity(MainActivityIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }


}