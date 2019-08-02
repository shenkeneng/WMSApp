package com.frxs.WMS.adapter.vlayoutadapter;

import android.view.View;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2018/03/05
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public interface ItemListener<T> {
    void onItemClick(View view, int position, T mData);
}
