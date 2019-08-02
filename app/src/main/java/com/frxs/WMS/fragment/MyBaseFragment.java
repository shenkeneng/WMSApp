package com.frxs.WMS.fragment;

import com.frxs.core.base.BaseFragment;
import com.frxs.WMS.MyApplication;
import com.frxs.WMS.comms.Config;
import com.frxs.WMS.model.UserInfo;
import com.frxs.WMS.rest.service.ApiService;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2017/05/09
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public abstract class MyBaseFragment extends BaseFragment {
    protected ApiService mService;

    public ApiService getService() {
        return MyApplication.getRestClient(Config.TYPE_BASE).getApiService();
    }

    public Integer getUserID() {
        UserInfo userInfo = MyApplication.getInstance().getUserInfo();
        if (null != userInfo) {
            return userInfo.getEmpID();
        } else {
            return 0;
        }
    }

  /*  public String getWID() {
        UserInfo userInfo = MyApplication.getInstance().getUserInfo();

        if (null != userInfo) {
            return String.valueOf(userInfo.getWareHouseWID());
        } else {
            return "";
        }
    }*/
}
