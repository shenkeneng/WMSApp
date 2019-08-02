package com.frxs.WMS.greendao.utils;

import android.content.Context;

import com.frxs.WMS.comms.Config;
import com.frxs.WMS.greendao.gen.DaoMaster;
import com.frxs.WMS.greendao.gen.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;


/**
 * Created by ewu on 2016/10/16.
 */

public class DbCore {
    private static final String DEFAULT_DB_NAME = Config.DB_NAME;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    private static Context mContext;
    private static String DB_NAME;

    public static void init(Context context) {
        init(context, DEFAULT_DB_NAME);
    }

    public static void init(Context context, String dbName) {
        if (context == null) {
            throw new IllegalArgumentException("context can't be null");
        }
        mContext = context.getApplicationContext();
        DB_NAME = dbName;
    }

    public static DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(mContext, DB_NAME);
            daoMaster = new DaoMaster(helper.getWritableDb());
        }
        return daoMaster;
    }

    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public static void enableQueryBuilderLog(){
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }
}
