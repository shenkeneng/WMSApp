package com.frxs.WMS.rest.model;


import android.content.Context;

import com.frxs.WMS.MyApplication;
import com.frxs.WMS.model.UserInfo;
import com.frxs.core.utils.SystemUtils;

/**
 * Created by Endoon on 2016/4/12.
 */
public class ApiRequest extends AjaxParams {

    public ApiRequest(Context context) {
        super();

        UserInfo userInfo =  MyApplication.getInstance().getUserInfo();
        if (null != userInfo) {
            put("Token", userInfo.getToken());
            put("UserAccount", userInfo.getUserAccount());
            put("Version", SystemUtils.getAppVersion(context));
        }
    }
}