package com.frxs.WMS.comms;

/**
 * Created by ewu on 2016/3/23.
 */
public class Config {
    public static final String DB_NAME = "dbname.db";

    public static final String DELIVERS_FILE_NAME = "deliver.txt";

    public static final String PREFS_NAME = "MyFrefsFile";

    public static final String KEY_USER = "key_user";

    public static final String KEY_SYNC_TIME = "key_sync_time"; // 同步数据时间

    public static final String KEY_FROM = "key_from";

    public static final String KEY_BT_MAC = "key_bt_mac"; //指定的蓝牙MAC地址

    // 远程服务器网络 (0:线上环境、1:测试环境、2：开发环境)
    public static int networkEnv = 0;

    public static final int TYPE_BASE = 0;

    public static final int TYPE_UPDATE = 1;

    public static String getBaseUrl(int typeUrl) {
    return getBaseUrl(typeUrl, networkEnv);
}

    public static String getBaseUrl(int typeUrl, int networkEnv) {
        String BASE_URL = "";
        if (networkEnv == 0) {
            if (typeUrl == TYPE_BASE) {
                BASE_URL = "http://app.f6.frxs.cn/api/";
            } else {
                BASE_URL = "http://app.f6.frxs.cn/api/";
            }
        } else if (networkEnv == 1) {
            if (typeUrl == TYPE_BASE) {
                BASE_URL = "http://192.168.6.157:8678/api/";
            } else {
                BASE_URL = "http://192.168.6.157:8678/api/";
                //BASE_URL = "http://192.168.8.214:8444/api/";//GZJ
            }
        } else if (networkEnv == 2) {
            if (typeUrl == TYPE_BASE) {
                BASE_URL = "http://192.168.8.214:8444/api/"; //GZJ
                //BASE_URL ="http://192.168.8.165:9988/api/"; //YQ
            } else {
                BASE_URL = "http://orderapi.erp2.frxs.com/api/";
            }
        } else {
            if (typeUrl == TYPE_BASE) {
                BASE_URL = "http://api_wh.erp2.frxs.com/";
            } else {
                BASE_URL = "http://orderapi.erp2.frxs.com/api/";
            }
        }

        return BASE_URL;
    }
}
