package com.walktech.device;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by windy on 2016/11/10.
 * 支付结果
 */

public class PayResultActivity extends BaseActivity {

    public static final String BUNDLE_BALANCE = "bundle_balance";
    private TextView tvTitle;
    private TextView tvBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result_activity);
        initView();
    }

    private void initView() {
        initTitle();
        String balance = getIntent().getStringExtra(BUNDLE_BALANCE);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        tvBalance.setText("余额 ¥" + balance);
    }

    private void initTitle() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("支付结果");
    }


}
