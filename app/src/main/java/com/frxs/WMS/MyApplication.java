package com.frxs.WMS;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import com.allenliu.versionchecklib.core.http.HttpParams;
import com.allenliu.versionchecklib.core.http.HttpRequestMethod;
import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.ForceUpdateListener;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.frxs.WMS.comms.Config;
import com.frxs.WMS.comms.GlobelDefines;
import com.frxs.WMS.greendao.utils.DbCore;
import com.frxs.WMS.model.AppVersionInfo;
import com.frxs.WMS.model.GetDeliverList;
import com.frxs.WMS.model.PackingBoxBean;
import com.frxs.WMS.model.UserInfo;
import com.frxs.WMS.rest.RestClient;
import com.frxs.WMS.rest.model.ApiRequest;
import com.frxs.WMS.rest.model.ApiResponse;
import com.frxs.WMS.rest.service.apkUpdate.DownloadService;
import com.frxs.WMS.rest.service.rxjava.BaseObserver;
import com.frxs.WMS.rest.service.rxjava.RxSchedulers;
import com.frxs.core.base.BaseActivity;
import com.frxs.core.utils.FileUtil;
import com.frxs.core.utils.JsonParser;
import com.frxs.core.utils.PrefUtils;
import com.frxs.core.utils.SerializableUtil;
import com.frxs.core.utils.SharedPreferencesHelper;
import com.frxs.core.utils.SystemUtils;
import com.frxs.core.utils.ToastUtils;
import com.frxs.core.widget.MaterialDialog;
import com.frxs.ktprefer.activity.HomeActivity;
import com.frxs.ktprefer.activity.MyBaseActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeReader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2017/03/29
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public class MyApplication extends Application {

    private static MyApplication mInstance;
    private static SparseArray<RestClient> restClientSparseArray = new SparseArray<RestClient>();
    private UserInfo mUserInfo;// 用户信息
    private List<PackingBoxBean> mPackingBoxBeans;
    private List<GetDeliverList.DriversBean> deliverList = new ArrayList<>();
    private boolean needCheckUpgrade = true; // 是否需要检测更新
    private HomeActivity stHomeActivity;
    private BarcodeReader barcodeReader;
    private DownloadBuilder builder;
    private Activity mActivity;
//    private boolean isForceUpdate = false;

    static {
        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
            @NonNull
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);
                return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate);
            }
        });
    }

    public static MyApplication getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Not yet initialized");
        }

        return mInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mInstance != null) {
            throw new IllegalStateException("Not a singleton");
        }

        mInstance = this;

        DbCore.init(this);

        initData();

        initRestClient();

        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                barcodeReader = aidcManager.createBarcodeReader();
            }
        });


    }
    public List<PackingBoxBean> getPackingBoxBean() {
        return mPackingBoxBeans;
    }

    public static RestClient getRestClient(int clientType) {
        return restClientSparseArray.get(clientType);
    }

    private void initRestClient() {
        int env = getEnvironment();
        restClientSparseArray.put(Config.TYPE_BASE, new RestClient(Config.getBaseUrl(Config.TYPE_BASE, env)));
        restClientSparseArray.put(Config.TYPE_UPDATE, new RestClient(Config.getBaseUrl(Config.TYPE_UPDATE, env)));
    }

    public void resetRestClient() {
        restClientSparseArray.clear();
        initRestClient();
    }

    private void initData() {
        // Get the user Info
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(this, Config.PREFS_NAME);
        String userStr = helper.getString(Config.KEY_USER, "");
        if (!TextUtils.isEmpty(userStr)) {
            Object object = null;
            try {
                object = SerializableUtil.str2Obj(userStr);
                if (null != object) {
                    mUserInfo = (UserInfo) object;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String deliverListStr = FileUtil.readInternalStoragePrivate(this, Config.DELIVERS_FILE_NAME);
        if (null != deliverListStr && deliverListStr.trim().length() > 0) {
            Type listType = new TypeToken<List<GetDeliverList.DriversBean>>() { }.getType();
            List<GetDeliverList.DriversBean> delivers = JsonParser.fromJson(deliverListStr, listType);
            if (null != delivers && delivers.size() > 0) {
                deliverList.addAll(delivers);
            }
        }
    }

    public void saveDeliverList(List<GetDeliverList.DriversBean> delivers) {
        deliverList.clear();

        if (null != delivers) {
            deliverList.addAll(delivers);
        }

        String fileContent = JsonParser.toJson(deliverList);
        FileUtil.writeInternalStoragePrivate(this, Config.DELIVERS_FILE_NAME, fileContent);
    }

    public List<GetDeliverList.DriversBean> getDeliverList() {
        return deliverList;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.mUserInfo = userInfo;

        String userStr = "";
        try {
            userStr = SerializableUtil.obj2Str(userInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(this, Config.PREFS_NAME);
        helper.putValue(Config.KEY_USER, userStr);
    }

    public void setEnvironment(int environmentId) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(this, GlobelDefines.PREFS_NAME);
        helper.putValue(GlobelDefines.KEY_ENVIRONMENT, environmentId);
    }

    public int getEnvironment() {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(this, GlobelDefines.PREFS_NAME);
        return helper.getInt(GlobelDefines.KEY_ENVIRONMENT, Config.networkEnv);
    }

    public UserInfo getUserInfo() {
        if (null == mUserInfo) {
            initData();
        }

        return mUserInfo;
    }



    public String getUserAccount() {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(this, GlobelDefines.PREFS_NAME);
        return helper.getString(GlobelDefines.KEY_USER_ACCOUNT, "");
    }

    public void setUserAccount(String userAccount) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(this, GlobelDefines.PREFS_NAME);
        helper.putValue(GlobelDefines.KEY_USER_ACCOUNT, userAccount);
    }

    public void logout() {
        // 清空用户信息
        setUserInfo(null);
        if (null != stHomeActivity) {
            stHomeActivity.finish();
            stHomeActivity = null;
        }

        deliverList.clear();
        PrefUtils.setValue(this, Config.KEY_SYNC_TIME, "");

        String fileContent = JsonParser.toJson(deliverList);
        FileUtil.writeInternalStoragePrivate(this, Config.DELIVERS_FILE_NAME, fileContent);
    }

    public HomeActivity getHomeActivity() {
        return stHomeActivity;
    }

    public void setHomeActivity(HomeActivity stHomeActivity) {
        this.stHomeActivity = stHomeActivity;
    }

    public void exitApp(int code) {
        System.exit(code);
    }

    public boolean isNeedCheckUpgrade() {
        return needCheckUpgrade;
    }

    /**
     * 更新版本的网路请求
     *
     * @param activity
     */
    public void prepare4Update(final Activity activity, final boolean isShow) {
        if (!SystemUtils.checkNet(this) || !SystemUtils.isNetworkAvailable(this)) {
            ToastUtils.show(this, "网络不可用");
            return;
        }
        mActivity = activity;
        ((BaseActivity)mActivity).showProgressDialog();
        //开始检测了升级之后，设置标志位为不再检测升级
        if (needCheckUpgrade) {
            needCheckUpgrade = false;
        }

        String url = Config.getBaseUrl(Config.TYPE_UPDATE, getEnvironment()) + "AppVersion/AppVersionUpdateGet";
        HttpParams httpParams = new HttpParams();
        httpParams.put("SysType", 0); // 0:android;1:ios
        httpParams.put("AppType", 0); //  0: 复核APP
        builder = AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(url)
                .setRequestMethod(HttpRequestMethod.POSTJSON)
                .setRequestParams(httpParams)
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(String result) {
                        ((BaseActivity)mActivity).dismissProgressDialog();
                        Type type = new TypeToken< ApiResponse<AppVersionInfo>>() {}.getType();
                        ApiResponse<AppVersionInfo> respData = new Gson().fromJson(result, type);
                        int versionCode = Integer.valueOf(SystemUtils.getVersionCode(getApplicationContext()));
                        if(respData.getData() == null) {
                            ToastUtils.show(activity, "更新接口无数据");
                            return null;
                        }
                        if (versionCode >= respData.getData().getCurCode()) {
                            ToastUtils.show(activity, "已是最新版本");
                            return null;
                        }
                        if (respData.getData().getUpdateFlag() == 0) {
                            return null;
                        }
                        if (respData.getData().getUpdateFlag() == 2) {
                            builder.setForceUpdateListener(new ForceUpdateListener() {
                                @Override
                                public void onShouldForceUpdate() {
                                    forceUpdate();
                                }
                            });
                        }
                        return crateUIData(respData.getData().getDownUrl(), respData.getData().getUpdateRemark());
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
                        ((BaseActivity)mActivity).dismissProgressDialog();
                        ToastUtils.show(activity,"request failed");

                    }
                });
        builder.setShowNotification(true);
        builder.setShowDownloadingDialog(true);
        builder.setShowDownloadFailDialog(true);
        builder.setForceRedownload(true);
        builder.excuteMission(activity);

       /* AjaxParams params = new AjaxParams();
        params.put("SysType", "0"); // 0:android;1:ios
        params.put("AppType", "7"); //  0:兴盛店订货平台，1:拣货APP，2:兴盛店配送APP，3:装箱APP，4:采购APP，5:网络店订货平台(不用)，6：网络店配送APP(不用))， 7:  调度 APP
        RestClient updateClient = getRestClient(Config.TYPE_UPDATE);
        updateClient.getApiService().AppVersionUpdateGet(params.getUrlParams()).enqueue(new SimpleCallback<ApiResponse<AppVersionInfo>>() {
            @Override
            public void onResponse(ApiResponse<AppVersionInfo> result, int code, String msg) {
                if (result.isSuccessful()) {
                    AppVersionInfo respData = result.getData();
                    if (null != respData) {
                        int versionCode = Integer.valueOf(SystemUtils.getVersionCode(getApplicationContext()));

                        String curVersion = respData.getCurVersion();
                        int curCode = respData.getCurCode();

                        if (versionCode < curCode) {
                            int updateFlag = respData.getUpdateFlag();
                            String updateRemark = respData.getUpdateRemark();
                            String downloadUrl = respData.getDownUrl();
                            switch (updateFlag) {
                                case 0: // 0:不需要
                                    break;
                                case 1: // 1:建议升级
                                    showUpdateDialog(activity, false, downloadUrl, curVersion, updateRemark);
                                    break;
                                case 2: // 2：强制升级
                                    showUpdateDialog(activity, true, downloadUrl, curVersion, updateRemark);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } else {
                    ToastUtils.show(activity, result.getInfo());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AppVersionInfo>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });*/
    }

    /**
     * @return
     * @important 使用请求版本功能，可以在这里设置downloadUrl
     * 这里可以构造UI需要显示的数据
     * UIData 内部是一个Bundle
     */
    private UIData crateUIData(String downloadUrl, String updateRemark) {
        UIData uiData = UIData.create();
        uiData.setTitle(getString(R.string.update_title));
        uiData.setDownloadUrl(downloadUrl);
        uiData.setContent(updateRemark);
        return uiData;
    }

    /**
     * 强制更新操作
     */
    private void forceUpdate() {
        mActivity.finish();
    }

    /**
     * 弹出更新的dialog
     *
     * @param activity
     * @param isForceUpdate 是否强制更新 true是，false否
     * @param downloadUrl   下载链接
     * @param curVersion    最新版本
     * @param updateRemark  更新内容
     * @description
     */
    private void showUpdateDialog(final Activity activity, final boolean isForceUpdate, final String downloadUrl, String curVersion, String updateRemark) {
        final MaterialDialog updateDialog = new MaterialDialog(activity);
        updateDialog.setTitle(R.string.update_title);
        updateDialog.setMessage(String.format(activity.getResources().getString(R.string.updade_content), curVersion,
                updateRemark));
        updateDialog.setCanceledOnTouchOutside(false);// 对话框外点击无效

        // 立即更新
        updateDialog.setPositiveButton("立即更新", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
                DownloadService downLoadService = new DownloadService(activity, downloadUrl, isForceUpdate);
                downLoadService.execute();
                if (!isForceUpdate) {
                    ToastUtils.showShortToast(activity, "程序在后台下载，请稍等...");
                }
            }
        });

        // 稍后更新（强制更新）:点击稍后更新也要更新
        updateDialog.setNegativeButton("稍后更新", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
                if (isForceUpdate) {
                    activity.finish();
                    System.exit(0);
                }
            }
        });
        updateDialog.show();
    }


    public BarcodeReader getBarcodeReader() {
        return barcodeReader;
    }

    public void syncCaseData(final BaseActivity activity) {
        if (!SystemUtils.checkNet(this) || !SystemUtils.isNetworkAvailable(this)) {
            ToastUtils.show(this, "网络不可用");
            return;
        }
        activity.showProgressDialog();

        ApiRequest params = new ApiRequest(activity);
        params.put("UserID", mUserInfo.getEmpID());
        params.put("UserName", mUserInfo.getUserAccount());
        params.put("WID", mUserInfo.getWID());
        params.put("OpAreaID", mUserInfo.getOpAreaID());

        getRestClient(Config.TYPE_BASE).getApiService().GetWPackingBoxList(params.getUrlParams())
                .compose(RxSchedulers.compose(activity, true))
                .subscribe(new BaseObserver<List<PackingBoxBean>>() {
                    @Override
                    public void onResponse(ApiResponse<List<PackingBoxBean>> result) {
                        activity.dismissProgressDialog();
                        if (result.isSuccessful()) {
                            mPackingBoxBeans = result.getData();

                        } else {
                            if (result.isAuthenticationFailed()) {
                                ToastUtils.show(activity, getString(R.string.authentication_failed));
                                ((MyBaseActivity)activity).reLogin();
                            } else {
                                ToastUtils.show(activity, result.getInfo());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        super.onFailure(t);
                        activity.dismissProgressDialog();
                        ToastUtils.show(activity, t.getMessage());
                    }
                });
    }
}
