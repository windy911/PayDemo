package com.walktech.device;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.walktech.device.data.bean.UserAccount;
import com.walktech.device.utils.Constant;
import com.walktech.device.utils.SPHelp;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by windy on 2016/11/10.
 * 收银台
 */

public class CashDeskActivity extends BaseActivity {
    public static final String TAG = CashDeskActivity.class.getName();

    public static final String STR_SHOP_NAME = "商户名称";
    public static final int HANDLER_MSG_UID = 1;
    public static final int HANDLER_MSG_CID = 2;

    public static final int STATUS_GREY = 0;
    public static final int STATUS_YELLOW = 1;
    public static final int STATUS_RED = 2;
    public static String strUUID = "";
    public static String strCARDID = "";
    public static CashDeskActivity mInstance;
    private long mExitTime;

    @ViewInject(R.id.btnMenu)
    private Button btnMenu;
    @ViewInject(R.id.btnSetting)
    private Button btnSetting;
    @ViewInject(R.id.edtAmount)
    private EditText edtAmount;
    @ViewInject(R.id.tvTitle)
    private TextView tvTitle;
    @ViewInject(R.id.btnNFCStatus)
    private Button btnNFCStatus;
    @ViewInject(R.id.btnComfirm)
    private Button btnComfirm;

    @ViewInject(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;// 左侧抽屉界面
    @ViewInject(R.id.menu_layout)
    private RelativeLayout mMenu_layout;// 主画面,中间的页面

    @ViewInject(R.id.rlSideMenuBg)
    private LinearLayout rlSideMenuBg;

    @ViewInject(R.id.tvUserName)
    private TextView tvUserName;

    @ViewInject(R.id.llNotLogin)
    private LinearLayout llNotLogin;
    @ViewInject(R.id.edtUserName)
    private EditText edtUserName;
    @ViewInject(R.id.edtUserPassword)
    private EditText edtUserPassword;
    @ViewInject(R.id.btnLogin)
    private Button btnLogin;
    @ViewInject(R.id.tvNewregister)
    private TextView tvNewregister;

    @ViewInject(R.id.llisLogined)
    private LinearLayout llisLogined;
    @ViewInject(R.id.tvAccountStatus)
    private TextView tvAccountStatus;
    @ViewInject(R.id.tvAccountBalance)
    private TextView tvAccountBanlace;
    @ViewInject(R.id.btnLogout)
    private Button btnLogout;
    @ViewInject(R.id.tvModifyPWD)
    private TextView tvModifyPWD;

    @ViewInject(R.id.llRegister)
    private LinearLayout llRegister;
    @ViewInject(R.id.edtRegisterName)
    private EditText edtRegisterName;
    @ViewInject(R.id.edtRegisterPWD)
    private EditText edtRegisterPWD;
    @ViewInject(R.id.edtRegisterPWD2)
    private EditText edtRegisterPWD2;
    @ViewInject(R.id.btnRegister)
    private Button btnRegister;
    @ViewInject(R.id.tvToLogin)
    private TextView tvToLogin;
    @ViewInject(R.id.tvShopName)
    private TextView tvShopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.cash_desk_activity, null);
        setContentView(rootView);
        ViewUtils.inject(this, rootView);
        initView();
        mExitTime = System.currentTimeMillis() - 2000;
        btnSetting.requestFocus();

        edtRegisterName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    requestShopName();
                }
            }
        });


        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {

            }

            @Override
            public void onDrawerOpened(View view) {
                Log.d("Rambo", "onDrawOpened");
                String token = SPHelp.getInstance(CashDeskActivity.this).getStringValue(SPHelp.USER_TOKEN);
                if (token != null && (!token.equals(""))) {
                    requestUserInfo();
                }
            }

            @Override
            public void onDrawerClosed(View view) {
                Log.d("Rambo", "onDrawerClosed");
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });


        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initView() {
        initTitle();
        btnMenu.setVisibility(View.VISIBLE);
        btnSetting.setVisibility(View.VISIBLE);
        refresh();
        initForAccount();
    }


    public void loadUserName() {
        String userName = SPHelp.getInstance(CashDeskActivity.this).getStringValue(SPHelp.USER_NAME);
        if (userName != null) {
            edtUserName.setText(userName);
        }
    }

    private void initForAccount() {
        loadUserName();

        String token = SPHelp.getInstance(CashDeskActivity.this).getStringValue(SPHelp.USER_TOKEN);
        if (token != null && (!token.equals(""))) {
            requestUserInfo();
        } else {
            setLogin(false);
        }
    }

    public void register() {
        String strName = edtRegisterName.getText().toString().trim();
        String strShopName = tvShopName.getText().toString().trim();
        String strPwd = edtRegisterPWD.getText().toString().trim();
        String strPwd2 = edtRegisterPWD2.getText().toString().trim();

        if (strName.length() <= 0) {
            Toast.makeText(CashDeskActivity.this, "请输入商户号", Toast.LENGTH_SHORT).show();
            return;
        } else if (strShopName.length() <= 0 || STR_SHOP_NAME.equals(strShopName)) {
            Toast.makeText(CashDeskActivity.this, "尚未获取商户名称", Toast.LENGTH_SHORT).show();
            return;
        } else if (strPwd.length() <= 0) {
            Toast.makeText(CashDeskActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        } else if (strPwd2.length() <= 0) {
            Toast.makeText(CashDeskActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
            return;
        } else if (!strPwd.equals(strPwd2)) {
            Toast.makeText(CashDeskActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }


        String httpUrl = Constant.API_REGISTER;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", strName);
        map.put("merName",strShopName);
        map.put("pwd", strPwd);


        httpService.postStringRequest(Request.Method.POST, httpUrl, map, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);

                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");
                            try {
                                JSONObject dataJson = json.getJSONObject("data");
                                String token = dataJson.getString("token");
                                SPHelp.getInstance(CashDeskActivity.this).setStirngValue(SPHelp.USER_TOKEN, token);
                            } catch (Exception e) {

                            }

                            if ("0".equals(code)) {
                                Toast.makeText(CashDeskActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                requestUserInfo();
                            } else {
                                Toast.makeText(CashDeskActivity.this, msg, Toast.LENGTH_SHORT).show();
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

    public void go2ModifyPWD() {
        Intent intent = new Intent(CashDeskActivity.this, ModifyPasswordActivity.class);
        startActivity(intent);
    }

    public void clearRegister() {
        edtRegisterName.setText("");
        tvShopName.setText(STR_SHOP_NAME);
        edtRegisterPWD.setText("");
        edtRegisterPWD2.setText("");
    }

    public void toRegisterView() {
        clearRegister();
        llisLogined.setVisibility(View.INVISIBLE);
        llNotLogin.setVisibility(View.INVISIBLE);
        llRegister.setVisibility(View.VISIBLE);
        tvUserName.setText("请注册");
    }

    public void toLoginView() {
        llisLogined.setVisibility(View.INVISIBLE);
        llNotLogin.setVisibility(View.VISIBLE);
        llRegister.setVisibility(View.INVISIBLE);
        tvUserName.setText("请登录");
    }

    public void setLogin(boolean isLogin) {
        if (!isLogin) {
            SPHelp.getInstance(CashDeskActivity.this).setStirngValue(SPHelp.USER_TOKEN, "");
            edtUserPassword.setText("");
            tvUserName.setText("请登录");
            mDrawerLayout.invalidate();
        }
        llisLogined.setVisibility(isLogin ? View.VISIBLE : View.INVISIBLE);
        llNotLogin.setVisibility(isLogin ? View.INVISIBLE : View.VISIBLE);
        llRegister.setVisibility(View.INVISIBLE);
    }

    public void toLogin() {
        if (edtUserName.getText().toString().trim().length() == 0) {
            Toast.makeText(CashDeskActivity.this, "请先输入用户名", Toast.LENGTH_SHORT).show();
        } else if (edtUserPassword.getText().toString().trim().length() == 0) {
            Toast.makeText(CashDeskActivity.this, "请先输入密码", Toast.LENGTH_SHORT).show();
        }
        saveUserName();

        requestLogin();
    }

    private void requestLogin() {

        String userName = edtUserName.getText().toString().trim();
        String password = edtUserPassword.getText().toString().trim();
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
                            try {
                                JSONObject dataJson = json.getJSONObject("data");
                                String token = dataJson.getString("token");
                                SPHelp.getInstance(CashDeskActivity.this).setStirngValue(SPHelp.USER_TOKEN, token);
                            } catch (Exception e) {

                            }

                            if ("0".equals(code)) {
                                Toast.makeText(CashDeskActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                requestUserInfo();
                            } else {
                                Toast.makeText(CashDeskActivity.this, msg, Toast.LENGTH_SHORT).show();
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

    private void requestUserInfo() {

        String httpUrl = Constant.API_USERINFO;
        String token = SPHelp.getInstance(CashDeskActivity.this).getStringValue(SPHelp.USER_TOKEN);

        httpService.postStringRequestWithToken(Request.Method.GET, httpUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");
                            JSONObject dataJson = json.getJSONObject("data");
                            String name = dataJson.getString("merName");
                            String status = dataJson.getString("status");
                            String balanceAmount = dataJson.getString("balanceAmount");

                            if ("0".equals(code)) {
//                                Toast.makeText(UserCenterActivity.this,"获取用户信息成功",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CashDeskActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CashDeskActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                    }
                },
                token
        );
    }

    public void setUserInfo(final UserAccount userAccount) {
        if (userAccount != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvUserName.setText(userAccount.userName);
                    tvAccountStatus.setText(UserAccount.getUserAccountString(userAccount.userAccountStatus));
                    tvAccountBanlace.setText(UserAccount.getUserBanlanceString(userAccount.userAccountBalance));
                }
            });
        }
    }

    public String getDeivceId() {
        String android_id = Settings.Secure.getString(CashDeskActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public void saveUserName() {
        SPHelp.getInstance(CashDeskActivity.this).setStirngValue(SPHelp.USER_NAME, edtUserName.getText().toString().trim());
    }

    @OnClick({R.id.btnSetting, R.id.btnMenu, R.id.btnComfirm, R.id.btnNFCStatus, R.id.btnLogin,
            R.id.btnLogout, R.id.rlSideMenuBg, R.id.tvNewregister,
            R.id.tvToLogin, R.id.tvModifyPWD, R.id.btnRegister})
    public void onClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btnSetting:
                if (MainActivity.mInstance != null) {
                    refresh();
                    MainActivity.mInstance.cancelSwiper();
                }
                intent = new Intent(CashDeskActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btnMenu:
                onMenuClicked();
                break;
            case R.id.btnComfirm:
                requestPay();
                break;
            case R.id.btnNFCStatus:
                if (MainActivity.mInstance != null) {
                    clear();
                    MainActivity.mInstance.readNFC();
                }
                break;
            case R.id.btnLogout:
                setLogin(false);
                break;
            case R.id.btnLogin:
                toLogin();
                break;
            case R.id.rlSideMenuBg:

                break;
            case R.id.tvNewregister:
                toRegisterView();
                break;

            case R.id.tvToLogin:
                toLoginView();
                break;

            case R.id.tvModifyPWD:
                go2ModifyPWD();
                break;

            case R.id.btnRegister:
                register();
                break;
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (closeDrawerMenu()) {
                return false;
            }

            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                exitApp();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void exitApp() {
        finishInstance();
        System.exit(0);
    }

    public static void finishInstance() {
        if (mInstance != null) {
            mInstance.finish();
        }
    }


    private void initTitle() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("收银台");
    }

    public void setShopName(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvShopName.setText(name);
            }
        });
    }

    public void refresh() {
        clear();
        setStatus(STATUS_GREY);
        edtAmount.setText("");
    }

    public void clear() {
        setUUID("");
        setCardID("");
        setStatus(STATUS_YELLOW);
    }

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MSG_UID:
                    setStatus(STATUS_YELLOW);
                    break;
                case HANDLER_MSG_CID:
                    setStatus(STATUS_RED);
                    break;
            }
        }
    };

    public void setUUID(String uuid) {
        strUUID = uuid;
        if (uuid != null && uuid.length() > 0) {
            handler.sendEmptyMessage(HANDLER_MSG_UID);
        }

//        Message msg = new Message();
//        msg.what = HANDLER_MSG_UID;
//        Bundle bundle = new Bundle();
//        bundle.putString("key",uuid);
//        msg.setData(bundle);
//        handler.sendMessage(msg);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                tvTest1.setText("UUID = " + strUUID);
//                Log.d("RAMBO", "UUID = " + strUUID);
//                setStatus(STATUS_RED);
//            }
//        });

    }

    public void setCardID(String cardID) {
        strCARDID = cardID;
        if (cardID != null && cardID.length() > 0) {
            handler.sendEmptyMessage(HANDLER_MSG_CID);
        }
//        Message msg = new Message();
//        msg.what = HANDLER_MSG_CID;
//        Bundle bundle = new Bundle();
//        bundle.putString("key",strCARDID);
//        msg.setData(bundle);
//        handler.sendMessage(msg);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                tvTest2.setText("CARD_ID = " + strCARDID);
//                Log.d("RAMBO", "CARD_ID = " + strCARDID);
//            }
//        });
    }


    //0:red 请按下  //1:yellow：等待刷卡  //2:green 已完成刷卡
    public void setStatus(int status) {
        switch (status) {
            case STATUS_GREY:
                btnNFCStatus.setBackgroundColor(getResources().getColor(R.color.grey));
                btnNFCStatus.setText("未读卡");
                break;
            case STATUS_YELLOW:
                btnNFCStatus.setBackgroundColor(getResources().getColor(R.color.grey));
                btnNFCStatus.setText("请刷卡");
                break;
            case STATUS_RED:
                btnNFCStatus.setBackgroundColor(getResources().getColor(R.color.red));
                btnNFCStatus.setText("读卡完成");
                break;
        }
    }

    private void requestShopName() {
        String httpUrl = Constant.API_MER_INFO;
        String name = edtRegisterName.getText().toString().trim();
        if (name.length() <= 0) return;

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", name);


        httpService.postStringRequest(Request.Method.POST, httpUrl, map, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);

                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");
                            String merName = "";
                            try {
                                JSONObject dataJson = json.getJSONObject("data");
                                merName = dataJson.getString("merName");
                                setShopName(merName);
                            } catch (Exception e) {

                            }

                            if ("0".equals(code)) {
                                Log.d("","获取商户名称成功"+merName);
                            } else {
                                Toast.makeText(CashDeskActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {

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

    private void requestPay() {
        String token = SPHelp.getInstance(CashDeskActivity.this).getStringValue(SPHelp.USER_TOKEN);

        if(token==null || token.length()<=0){
            Toast.makeText(CashDeskActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtAmount.getText().toString().trim().length() == 0) {
            Toast.makeText(CashDeskActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        if (strUUID == null || strUUID.equals("")) {
            Toast.makeText(CashDeskActivity.this, "请先刷卡", Toast.LENGTH_SHORT).show();
            return;
        }

        if (strCARDID == null || strCARDID.equals("")) {
            Toast.makeText(CashDeskActivity.this, "读卡信息缺失", Toast.LENGTH_SHORT).show();
            return;
        }




        String amount = edtAmount.getText().toString().trim();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("amount", amount);
        map.put("uid", strUUID);
        map.put("cid", strCARDID);

        String httpUrl = Constant.API_PAY;


        httpService.postStringRequestWithToken(Request.Method.POST, httpUrl, map, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);

                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");
                            String bal_at = "--";

                            try {
                                if (json.has("data")) {
                                    JSONObject dataJson = json.getJSONObject("data");
                                    if (dataJson != null) {
                                        if (dataJson.has("bal_at")) {
                                            bal_at = dataJson.getString("bal_at");
                                        }
                                    }
                                }
                            } catch (Exception e) {

                            }

                            if ("0".equals(code)) {
                                Toast.makeText(CashDeskActivity.this, "扣款成功", Toast.LENGTH_SHORT).show();
                                go2PayResult(true, bal_at);
                            } else {
                                Toast.makeText(CashDeskActivity.this, msg, Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e) {
                            Toast.makeText(CashDeskActivity.this, "扣款失败", Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("", "");
                    }
                },
                token
        );
    }

    public void go2PayResult(boolean isSuccess, String balance) {
        if (isSuccess) {
            Intent intent = new Intent(CashDeskActivity.this, PayResultActivity.class);
            intent.putExtra(PayResultActivity.BUNDLE_BALANCE, balance);
            startActivity(intent);
            refresh();
        }
    }

    private void onMenuClicked() {
        if (mDrawerLayout.isDrawerOpen(mMenu_layout)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(mMenu_layout);
        }


    }

    public boolean closeDrawerMenu() {
        if (mDrawerLayout.isDrawerOpen(mMenu_layout)) {
            mDrawerLayout.closeDrawers();
            return true;
        } else {
            return false;
        }
    }

}
