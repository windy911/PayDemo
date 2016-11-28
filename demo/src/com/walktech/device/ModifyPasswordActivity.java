package com.walktech.device;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.walktech.device.utils.Constant;
import com.walktech.device.utils.SPHelp;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by windy on 2016/11/10.
 * 修改密码
 */

public class ModifyPasswordActivity extends BaseActivity {

    private TextView tvTitle;

    @ViewInject(R.id.edtOldPwd)
    private EditText edtOldPwd;
    @ViewInject(R.id.edtNewPwd)
    private EditText edtNewPwd;
    @ViewInject(R.id.edtNewPwd2)
    private EditText edtNewPwd2;
    @ViewInject(R.id.btnModify)
    private Button btnModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.modify_password, null);
        setContentView(rootView);
        ViewUtils.inject(this, rootView);
        initView();
    }

    private void initView() {
        initTitle();

    }

    @OnClick({R.id.btnModify})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnModify:
                modifyPwd();
                break;
        }
    }

    public boolean checkValid(){
        String oldPwd = edtOldPwd.getText().toString().trim();
        String newPwd = edtNewPwd.getText().toString().trim();
        String newPwd2 = edtNewPwd2.getText().toString().trim();

        if(oldPwd.length()<=0){
            Toast.makeText(ModifyPasswordActivity.this,"请输入原密码",Toast.LENGTH_SHORT).show();
            return false;
        }else if(newPwd.length()<=0){
            Toast.makeText(ModifyPasswordActivity.this,"请输入新密码",Toast.LENGTH_SHORT).show();
            return false;
        }else if(newPwd2.length()<=0){
            Toast.makeText(ModifyPasswordActivity.this,"请再次输入新密码",Toast.LENGTH_SHORT).show();
            return false;
        }else if(!newPwd.equals(newPwd2)){
            Toast.makeText(ModifyPasswordActivity.this,"两次输入密码不一致",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void modifyPwd(){
        if(checkValid()){
            requestModify();
        }
    }

    private void requestModify() {

        String oldPwd = edtOldPwd.getText().toString().trim();
        String newPwd = edtNewPwd.getText().toString().trim();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("pwd", oldPwd);
        map.put("pwdNew", newPwd);

        String httpUrl = Constant.API_MODIFY_PWD;
        String token = SPHelp.getInstance(ModifyPasswordActivity.this).getStringValue(SPHelp.USER_TOKEN);

        httpService.postStringRequestWithToken(Request.Method.POST, httpUrl, map, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.d("httpService", "onResponse: " + s);

                        try {
                            JSONObject json = new JSONObject(s);
                            String code = json.getString("code");
                            String msg = json.getString("message");

                            if ("0".equals(code)) {
                                Toast.makeText(ModifyPasswordActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ModifyPasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(ModifyPasswordActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
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

    private void initTitle() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("修改密码");
    }


}
