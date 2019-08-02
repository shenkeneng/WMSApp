package com.frxs.WMS.adapter.vlayoutadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2018/03/02
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public class VBaseHolder<T> extends RecyclerView.ViewHolder {
    public ItemListener mListener;
    public Context mContext;
    public View mView;
    public T mData;
    public int position;

    public VBaseHolder(View itemView) {
        super(itemView);
        mView = itemView;
        init();
    }

    public void init() {

    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setListener(ItemListener listener) {
        mListener = listener;
    }

    public void setData(int ps, T mData) {
        this.mData = mData;
        position = ps;
    }

}