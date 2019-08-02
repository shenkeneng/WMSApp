package com.frxs.WMS.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.frxs.WMS.R;
import com.frxs.WMS.bluetooth.BluetoothService;
import com.frxs.WMS.bluetooth.OnBluetoothLisenter;
import com.frxs.WMS.comms.Config;
import com.frxs.WMS.comms.GlobelDefines;
import com.frxs.WMS.utils.ToastHelper;
import com.frxs.core.utils.SharedPreferencesHelper;
import com.frxs.core.widget.EmptyView;
import com.frxs.ktprefer.activity.MyBaseActivity;
import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Chentie on 2017/2/23.
 */

public class SearchBluetoothActivity extends MyBaseActivity implements OnBluetoothLisenter {

    private TextView bluetoothTv;

    private TextView titleLeftTv;

    private ListView bluetoothLv;

    private BluetoothService bluetoothService;

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<BluetoothDevice>(); // 用于存放蓝牙设备

    private QuickAdapter<BluetoothDevice> adapter;

    private boolean isSearch = true;

    private EmptyView emptyView;

    private LinearLayout bluetoothLl;

    private String selectedBtAddress;

    private TextView actionBackTv;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchbluetooth);

        initViews();
        initEvent();
        initData();
    }

    protected void initViews() {

        bluetoothTv = (TextView) findViewById(R.id.tvBluetooth);
        actionBackTv = (TextView) findViewById(R.id.actionBackTv);
        titleLeftTv = (TextView) findViewById(R.id.titleTv);
        bluetoothLv = (ListView) findViewById(R.id.lvBluetooth);
        bluetoothLl = (LinearLayout) findViewById(R.id.llBluetoothList);
        emptyView = (EmptyView) findViewById(R.id.emptyview);

        titleLeftTv.setText("设置蓝牙");
        TextView headrView = new TextView(this);
        headrView.setText("我的设备");
        headrView.setTextColor(getResources().getColor(R.color.frxs_black));
        headrView.setTextSize(23);
        headrView.setPadding(10, 10, 10, 10);
        bluetoothLv.addHeaderView(headrView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService.isDiscovering()) {
            bluetoothService.cancelDevices();
        }
        bluetoothService.unregisterReceiver();
    }

    protected void initData() {
        SharedPreferencesHelper sp = SharedPreferencesHelper.getInstance(SearchBluetoothActivity.this, GlobelDefines.PREFS_NAME);
        selectedBtAddress = sp.getString(Config.KEY_BT_MAC, "");

        bluetoothService = new BluetoothService(this, this);
        bluetoothService.setBluetoothLisenter(this);
        bluetoothService.registerReceiver();
        adapter = new QuickAdapter<BluetoothDevice>(this, R.layout.item_bluetooth) {
            @Override
            protected void convert(final BaseAdapterHelper helper, final BluetoothDevice item) {
                if (!TextUtils.isEmpty(selectedBtAddress)) {
                    if (item.getAddress().equals(selectedBtAddress)){
                        helper.setVisible(R.id.iv_bluetooth_state, true);
                        helper.setBackgroundRes(R.id.ll_bluetooth, R.color.frxs_gray_dark);
                    }else{
                        helper.setVisible(R.id.iv_bluetooth_state, false);
                        helper.setBackgroundRes(R.id.ll_bluetooth, R.color.transparent);
                    }
                }else{
                    helper.setVisible(R.id.iv_bluetooth_state, false);
                    helper.setBackgroundRes(R.id.ll_bluetooth, R.color.transparent);
                }

                helper.setText(R.id.tv_bluetooth_name, item.getName());
            }
        };
        adapter.addAll(bluetoothDevices);
        bluetoothLv.setAdapter(adapter);

        initBluetoothViews();
    }

    private void initBluetoothViews() {
        if (this.bluetoothService.isOpen()) {
            bluetoothDevices.clear();
            bluetoothTv.setText("搜索蓝牙");
            if (bluetoothDevices != null && bluetoothDevices.size() > 0){   // 有蓝牙设备
                emptyView.setVisibility(View.GONE);
                bluetoothLl.setVisibility(View.VISIBLE);
                adapter.replaceAll(bluetoothDevices);
            }else{                                                          // 没有蓝牙设备
                bluetoothLl.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("尚未有成功连接蓝牙，请搜索！");
                emptyView.setImageResource(R.mipmap.icon_start_bluetooth);

                doSearchBTDevices();
            }
        } else {
            bluetoothLl.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("请尚未开启蓝牙，请开启！");
            emptyView.setImageResource(R.mipmap.icon_start_bluetooth);
            bluetoothTv.setText("开启蓝牙");
        }
    }

    protected void initEvent() {
        bluetoothTv.setOnClickListener(this);
        titleLeftTv.setOnClickListener(this);
        actionBackTv.setOnClickListener(this);

        bluetoothLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice item = (BluetoothDevice)parent.getAdapter().getItem(position);
                if (item != null) {
                    selectedBtAddress = item.getAddress();
                    SharedPreferencesHelper sp = SharedPreferencesHelper.getInstance(SearchBluetoothActivity.this, GlobelDefines.PREFS_NAME);
                    sp.putValue(Config.KEY_BT_MAC, selectedBtAddress);
                    adapter.notifyDataSetChanged();
                    ToastHelper.toastShort(SearchBluetoothActivity.this, "已保存");
                }
            }
        });
    }

    @Override
    public void onBluetoothStateChanged(boolean isOpened) {
        initBluetoothViews();
    }

    @Override
    public void onBluetoothDiscoveryStarted() {
        ToastHelper.toastShort(SearchBluetoothActivity.this, "正在搜索蓝牙");
        emptyView.setVisibility(View.GONE);
        bluetoothLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBluetoothDiscoveryFound(List<BluetoothDevice> bondDevices, List<BluetoothDevice> unbondDevices) {
        this.bluetoothDevices.clear();

        if (bondDevices != null) {
            for (BluetoothDevice bluetoothDevices : bondDevices) {
                this.bluetoothDevices.add(bluetoothDevices);
            }
        }
        this.bluetoothDevices.addAll(unbondDevices);
        if (adapter != null) {
            adapter.replaceAll(bluetoothDevices);
        }
    }

    @Override
    public void onBluetoothDiscoveryFinished(List<BluetoothDevice> bondDevices, List<BluetoothDevice> unbondDevices) {
        this.bluetoothDevices.clear();

        if (bondDevices != null) {
            for (BluetoothDevice bluetoothDevices : bondDevices) {
                this.bluetoothDevices.add(bluetoothDevices);
            }
        }

        this.bluetoothDevices.addAll(unbondDevices);
        if (adapter != null) {
            adapter.replaceAll(bluetoothDevices);
        }

        ToastHelper.toastShort(SearchBluetoothActivity.this, "搜索蓝牙完毕");
        bluetoothTv.setText("搜索蓝牙");
        isSearch = true;
    }

    @Override
    public void onBluetoothBondStateChanged(BluetoothDevice device, boolean isSuccess) {
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.tvBluetooth:
                if (adapter != null){
                    adapter.clear();
                }
                if (bluetoothService.isOpen()) {
                    doSearchBTDevices();
                } else {
                    bluetoothService.openBluetooth(SearchBluetoothActivity.this);
                }
                break;

            case R.id.actionBackTv:
                finish();
                break;
        }
    }

    private void doSearchBTDevices() {
        if (isSearch) {
            bluetoothService.searchDevices();
            bluetoothTv.setText("停止搜索");
            isSearch = false;
        } else {
            bluetoothService.cancelDevices();
            isSearch = true;
            bluetoothTv.setText("搜索蓝牙");
        }
    }
}
