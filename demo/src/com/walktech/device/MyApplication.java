package com.walktech.device;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by windy on 2016/11/11.
 */

public class MyApplication extends Application {
    public static MyApplication instance;
    private RequestQueue mQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initApplication();
    }

    private void initApplication() {

    }


    public synchronized RequestQueue getRequestQueue() {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(instance);
        }
        return mQueue;
    }
}
