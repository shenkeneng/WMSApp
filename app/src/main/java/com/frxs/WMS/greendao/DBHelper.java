package com.frxs.WMS.greendao;

import com.frxs.WMS.greendao.service.ProductEntityService;
import com.frxs.WMS.greendao.utils.DbCore;

/**
 * Created by ewu on 2017/3/1.
 */

public class DBHelper {

    private static ProductEntityService productEntityService;

    public static ProductEntityService getProductEntityService() {
        if (productEntityService == null) {
            productEntityService = new ProductEntityService(DbCore.getDaoSession().getProductEntityDao());
        }
        return productEntityService;
    }
}
