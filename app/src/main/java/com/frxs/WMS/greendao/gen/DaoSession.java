package com.frxs.WMS.greendao.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.frxs.WMS.greendao.entity.ProductEntity;

import com.frxs.WMS.greendao.gen.ProductEntityDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig productEntityDaoConfig;

    private final ProductEntityDao productEntityDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        productEntityDaoConfig = daoConfigMap.get(ProductEntityDao.class).clone();
        productEntityDaoConfig.initIdentityScope(type);

        productEntityDao = new ProductEntityDao(productEntityDaoConfig, this);

        registerDao(ProductEntity.class, productEntityDao);
    }
    
    public void clear() {
        productEntityDaoConfig.clearIdentityScope();
    }

    public ProductEntityDao getProductEntityDao() {
        return productEntityDao;
    }

}
