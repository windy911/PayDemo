package com.walktech.device.data.bean;

/**
 * Created by windy on 2016/11/17.
 */

public class UserAccount {
    public String userName;
    public String userAccountStatus;
    public String userAccountBalance;

    public UserAccount(String name, String status, String balance) {
        this.userName = name;
        this.userAccountStatus = status;
        this.userAccountBalance = balance;

    }

    public static String getUserAccountString(String status) {
        if ("ACTIVE".equals(status)) {
            return "正常";
        } else if ("FREEZE".equals(status)) {
            return "冻结";
        } else if ("UNKOWN".equals(status)) {
            return "未知";
        } else {
            return "未知状态";
        }
    }

    public static String getUserBanlanceString(String balance) {
        if (balance != null) {
            return balance + "元";
        } else {
            return "--" + "元";
        }
    }
}
