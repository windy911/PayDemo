package com.walktech.device;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.walktech.device.response.HttpService;


/**
 * Created by windy on 2016/11/11.
 */

public class BaseActivity extends Activity{
    public HttpService httpService;
    public LayoutInflater inflater;
    public View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpService = new HttpService();
    }
}



