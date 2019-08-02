package com.frxs.WMS.rest.service;

import com.frxs.WMS.rest.model.ApiResponse;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2017/06/09
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public interface RequestListener {
    public abstract void handleRequestResponse(ApiResponse result);

    public abstract void handleExceptionResponse(String errMsg);
}
