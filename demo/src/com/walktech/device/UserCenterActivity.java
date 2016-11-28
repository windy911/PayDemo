package com.walktech.device;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.walktech.device.data.bean.UserAccount;
import com.walktech.device.utils.Constant;
import com.walktech.device.utils.SPHelp;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by windy on 2016/11/10.
 * 个人中心
 */

public class UserCenterActivity extends BaseActivity {

    private TextView tvTitle;
    private EditText edtUserName, edtPassword;
    private Button btnLogin,btnLogout;
    private LinearLayout llLogined, llUnlogin;

    private TextView tvUserName, tvAccountStatus, tvAccountBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_activity);
        initView();
    }

    private void initView() {
        initTitle();

        llLogined = (LinearLayout) findViewById(R.id.llLogined);
        llUnlogin = (LinearLayout) findViewById(R.id.llunLogin);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvAccountStatus = (TextView) findViewById(R.id.tvAccountStatus);
        tvAccountBalance = (TextView) findViewById(R.id.tvAccountBalance);


        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtPassword = (EditText) findViewById(R.id.edtUserPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogin();
            }
        });

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLogin(false);

            }
        });

        loadUserName();

        String token = SPHelp.getInstance(UserCenterActivity.this).getStringValue(SPHelp.USER_TOKEN);
        if (token != null && (!token.equals(""))) {
            requestUserInfo();
        } else {
            setLogin(false);
        }
    }

    private void initTitle() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("个人中心");


    }

    public void toLogin() {
        if (edtUserName.getText().toString().trim().length() == 0) {
            Toast.makeText(UserCenterActivity.this, "请先输入用户名", Toast.LENGTH_SHORT).show();
        } else if (edtPassword.getText().toString().trim().length() == 0) {
            Toast.makeText(UserCenterActivity.this, "请先输入密码", Toast.LENGTH_SHORT).show();
        }
        saveUserName();

        requestLogin();
    }

    private void requestLogin() {

        String userName = edtUserName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String deviceId = getDeivceId();

        String httpUrl = Constant.API_LOGIN;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", userName);
        map.put("pwd", password);
        map.put("deviceId", deviceId);


        httpService.postStringRequest(Request.Method.POST, httpUrl, map, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);

                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");
                            try{
                                JSONObject dataJson = json.getJSONObject("data");
                                String token = dataJson.getString("token");
                                SPHelp.getInstance(UserCenterActivity.this).setStirngValue(SPHelp.USER_TOKEN, token);
                            }catch (Exception e){

                            }

                            if ("0".equals(code)) {
                                Toast.makeText(UserCenterActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                requestUserInfo();
                            } else {
                                Toast.makeText(UserCenterActivity.this, msg, Toast.LENGTH_SHORT).show();
                                setLogin(false);
                            }

                        } catch (Exception e) {
                            setLogin(false);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("", "");
                    }
                }
        );
    }

//    private void requestLoginGet() {
//
//        String userName = edtUserName.getText().toString().trim();
//        String password = edtPassword.getText().toString().trim();
//        String deviceId = getDeivceId();
//
//        String httpUrl = Constant.API_LOGIN+"?pwd="+password+"&name="+userName;
////        HashMap<String, String> map = new HashMap<String, String>();
////        map.put("name", userName);
////        map.put("pwd", password);
////        map.put("deviceId", deviceId);
//
//
////        NameValuePair data = new BasicNameValuePair("name",userName);
////        NameValuePair data1 = new BasicNameValuePair("pwd",password);
////        NameValuePair data2 = new BasicNameValuePair("deviceId",deviceId);
////        final List<NameValuePair> list = new ArrayList<NameValuePair>();
////        list.add(data);
////        list.add(data1);
////        list.add(data2);
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
////                doPost(0,list);
////            }
////        }).start();
//
//
//        httpService.getStringRequest(Request.Method.POST, httpUrl,  new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String s) {
//                        Log.d("httpService", "onResponse: " + s);
////                        requestUserInfo();
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//                        Log.d("", "");
//                    }
//                }
//        );
//    }

    private void requestUserInfo() {

        String httpUrl = Constant.API_USERINFO;
        String token = SPHelp.getInstance(UserCenterActivity.this).getStringValue(SPHelp.USER_TOKEN);

        httpService.postStringRequestWithToken(Request.Method.GET, httpUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");
                            JSONObject dataJson = json.getJSONObject("data");
                            String name = dataJson.getString("name");
                            String status = dataJson.getString("status");
                            String balanceAmount = dataJson.getString("balanceAmount");

                            if ("0".equals(code)) {
//                                Toast.makeText(UserCenterActivity.this,"获取用户信息成功",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserCenterActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                            setUserInfo(new UserAccount(name, status, balanceAmount));
                            setLogin(true);
                        } catch (Exception e) {
                            setLogin(false);

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
//                        Log.d("", volleyError.getMessage());
                        Toast.makeText(UserCenterActivity.this,"获取信息失败",  Toast.LENGTH_SHORT).show();
                    }
                },
                token
        );
    }

    public String getDeivceId() {
        String android_id = Settings.Secure.getString(UserCenterActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public void saveUserName() {
        SPHelp.getInstance(UserCenterActivity.this).setStirngValue(SPHelp.USER_NAME, edtUserName.getText().toString().trim());
    }

    public void loadUserName() {
        String userName = SPHelp.getInstance(UserCenterActivity.this).getStringValue(SPHelp.USER_NAME);
        if (userName != null) {
            edtUserName.setText(userName);
        }
    }

    public void setUserInfo(final UserAccount userAccount) {
        if (userAccount != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvUserName.setText(userAccount.userName);
                    tvAccountStatus.setText(UserAccount.getUserAccountString(userAccount.userAccountStatus));
                    tvAccountBalance.setText(UserAccount.getUserBanlanceString(userAccount.userAccountBalance));
                }
            });
        }
    }

    public void setLogin(boolean isLogin) {
        if (!isLogin) {
            SPHelp.getInstance(UserCenterActivity.this).setStirngValue(SPHelp.USER_TOKEN, "");
        }
        llLogined.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        llUnlogin.setVisibility(isLogin ? View.GONE : View.VISIBLE);
    }


}
