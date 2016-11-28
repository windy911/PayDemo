package com.walktech.device.response;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.walktech.device.MyApplication;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by windy on 15/8/19.
 */
public class HttpService {
    public static final String TAG = HttpService.class.getSimpleName();

    public HttpService() {
        super();
    }


    //GET
    public void getStringRequest(int method, final String url, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {

        final StringRequest stringRequest = new StringRequest(method, url, listener,errorListener ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("Charset", "UTF-8");
//                headers.put("x-token", "Basic3235_2acbefcc58a749a7b790c6d0ecf30653");
                return headers;
            }
        };
        stringRequest.setRetryPolicy(getRetryPolicy());
        MyApplication.instance.getRequestQueue().add(stringRequest);

    }


    //Post
    public void postStringRequestWithToken(int method, final String url, final Response.Listener<String> listener, final Response.ErrorListener errorListener,final String token) {

        final StringRequest stringRequest = new StringRequest(method, url, listener,errorListener ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("charset", "UTF-8");
                headers.put("Content-Type", "application/json");
                headers.put("x-token", token);
                return headers;
            }
        };
        stringRequest.setRetryPolicy(getRetryPolicy());
        MyApplication.instance.getRequestQueue().add(stringRequest);

    }

    //POST
    public void postStringRequest(int method, final String url, final Map<String, String> map, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        final StringRequest stringRequest = new StringRequest(method, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("charset", "UTF-8");
//                headers.put("Content-Type", "application/json;charset=UTF-8");
                return headers;
            }
        };

        stringRequest.setRetryPolicy(getRetryPolicy());
        MyApplication.instance.getRequestQueue().add(stringRequest);
    }

    //POST
    public void postStringRequestWithToken(int method, final String url, final Map<String, String> map, final Response.Listener<String> listener, final Response.ErrorListener errorListener,final String token) {
        final StringRequest stringRequest = new StringRequest(method, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("charset", "UTF-8");
//                headers.put("Content-Type", "application/json;charset=UTF-8");
                headers.put("x-token", token);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(getRetryPolicy());
        MyApplication.instance.getRequestQueue().add(stringRequest);
    }


    public RetryPolicy getRetryPolicy() {
        RetryPolicy retryPolicy = new DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return retryPolicy;
    }
}
