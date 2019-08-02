package com.frxs.ktprefer.activity

import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.frxs.WMS.MyApplication
import com.frxs.WMS.R
import com.frxs.WMS.activity.SearchBluetoothActivity
import com.frxs.WMS.bluetooth.BluetoothService
import com.frxs.WMS.bluetooth.OnBluetoothLisenter
import com.frxs.WMS.bluetooth.PrintHelper
import com.frxs.WMS.comms.Config
import com.frxs.WMS.comms.GlobelDefines
import com.frxs.WMS.model.OrderBean
import com.frxs.WMS.model.PackQty
import com.frxs.WMS.model.PackingBoxBean
import com.frxs.WMS.rest.model.ApiRequest
import com.frxs.WMS.rest.model.ApiResponse
import com.frxs.WMS.rest.service.rxjava.BaseObserver
import com.frxs.WMS.rest.service.rxjava.RxSchedulers
import com.frxs.WMS.utils.ToastHelper
import com.frxs.WMS.widget.CountEditText
import com.frxs.core.utils.CommonUtils
import com.frxs.core.utils.SharedPreferencesHelper
import com.frxs.core.utils.SystemUtils
import com.frxs.core.utils.ToastUtils
import com.frxs.core.widget.MaterialDialog
import com.gprinter.aidl.GpService
import com.gprinter.command.EscCommand
import com.gprinter.command.GpCom
import com.gprinter.command.GpUtils
import com.gprinter.command.LabelCommand
import com.gprinter.io.GpDevice
import com.gprinter.io.PortParameters
import com.gprinter.service.GpPrintService
import com.honeywell.aidc.BarcodeReadEvent
import com.pacific.adapter.RecyclerAdapter
import com.pacific.adapter.RecyclerAdapterHelper
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.view_title.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * Created by Chentie on 2018/6/5.
 */
class HomeActivity : BaseScanActivity() {

    var orderBean = null as OrderBean?
    var group = ""
    private val mPrinterIndex = 0
    private var mTotalCopies = 0
    private var bluetoothService: BluetoothService? = null
    private val DEBUG_TAG = "SamleApp"
    private var mPrinterId = 0
    private var mGpService: GpService? = null
    private var conn: PrinterServiceConnection? = null
    private var adapter: RecyclerAdapter<PackingBoxBean>? = null
    private var localPackingBoxList: List<PackingBoxBean>? = null
    private var pickId : String? = null

    companion object {
        val MESSAGE_CONNECT = 1
        private val MAIN_QUERY_PRINTER_STATUS = 0xfe
        private val REQUEST_PRINT_LABEL = 0xfd
        private val REQUEST_PRINT_RECEIPT = 0xfc
    }

    /**
     * 初始化打印服务
     */
    internal inner class PrinterServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mGpService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mGpService = GpService.Stub.asInterface(service)
            connectOrDisConnectToDevice();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initView()
        initEvent()
        initData()
    }

    private fun initView() {
        actionBackTv!!.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_head_portrait, 0, 0, 0)
        titleTv!!.setText("扫描装箱")
        actionRightTv.visibility = View.VISIBLE
        actionRightTv!!.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_bluetooth, 0, 0, 0)
        actionRightTv!!.text = ""
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayout.VERTICAL
        lvBox.layoutManager = linearLayoutManager
    }

    private fun initEvent() {
        // 注册实时状态查询广播
        registerReceiver(mBroadcastReceiver, IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS))
        registerReceiver(mBroadcastReceiver, IntentFilter(GpCom.ACTION_CONNECT_STATUS))
        /**
         * 标签模式下，可注册该广播，在需要打印内容的最后加入addQueryPrinterStatus(RESPONSE_MODE mode)
         * ，在打印完成后会接收到，action为GpCom.ACTION_LABEL_RESPONSE的广播，特别用于连续打印，
         * 可参照该sample中的sendLabelWithResponse方法与广播中的处理
         **/
        registerReceiver(mBroadcastReceiver, IntentFilter(GpCom.ACTION_LABEL_RESPONSE))
        printBtn.setOnClickListener(View.OnClickListener {
            if (CommonUtils.isFastDoubleClick()) {
                return@OnClickListener
            }
            if (orderBean != null) {
                ifOpenBluetooth()
            } else {
                ToastUtils.show(this@HomeActivity, "未获取到订单详情")
            }
        })

        cancelBtn.setOnClickListener(View.OnClickListener {
            if (CommonUtils.isFastDoubleClick()) {
                return@OnClickListener
            }
//            cetGoodCountedit.count = 0
            orderLl.visibility = View.INVISIBLE
            tvTips.visibility = View.VISIBLE
        })

        actionBackTv.setOnClickListener(View.OnClickListener {
            gotoActivity(MineActivity::class.java, false)
        })

        actionRightTv.setOnClickListener(View.OnClickListener {
            gotoActivity(SearchBluetoothActivity::class.java, false)
        })
    }

    private fun SetPackQty() {
        if (!SystemUtils.checkNet(this) || !SystemUtils.isNetworkAvailable(this)) {
            ToastUtils.show(this, "网络不可用")
            return
        }
        if (lvBox.adapter == null) {
            ToastUtils.show(this, "包材信息为空，无法打印。")
            return
        }
        var mBoxs = ArrayList<PackQty.WPackingBoxListBean>()
        mTotalCopies = 0

        var i = 0
        while(i < adapter!!.size) {
            var item = adapter!!.get(i) as PackingBoxBean
            var mBox = PackQty.WPackingBoxListBean()
            mBox.id = item.id
            mBox.packingBoxCode =  item.packingBoxCode
            mBox.packingBoxName =  item.packingBoxName
            mBox.vLength =  item.vLength
            mBox.vWidth = item.vWidth
            mBox.vHeight =  item.vHeight
            mBox.volume =  item.volume
            mBox.isTackBack =  item.isTackBack
            mTotalCopies += item.editCunt
            mBox.qty = item.editCunt
            mBoxs!!.add(mBox)
            i++
        }

        var mPackQty = PackQty()
        mPackQty.token = token
        mPackQty.userAccount = userAccount
        mPackQty.version = SystemUtils.getAppVersion(this)
        mPackQty.pickID = pickId
        mPackQty.deliveryTime =  orderBean!!.deliveryTime
        mPackQty.wPackingBoxList = mBoxs
        mPackQty.userID = MyApplication.getInstance().userInfo.empID
        mPackQty.userName = MyApplication.getInstance().userInfo.empName
        mPackQty.wid = MyApplication.getInstance().userInfo.wid
        mPackQty.opAreaID = orderBean!!.opAreaID

        if (mTotalCopies <= 0) {
            ToastUtils.show(this, "箱数不能小于等于0！")
            return
        }
        getService().SetPackQty(mPackQty)
                .compose(RxSchedulers.compose(this, true))
                .subscribe(object : BaseObserver<String>() {
                    override fun onResponse(result: ApiResponse<String>?) {
                        if(result!!.isSuccessful) {
                            if (mTotalCopies > 0) {
                                Observable.create(ObservableOnSubscribe<Any> {
                                    sendLabelWithResponse()
                                }).subscribeOn(Schedulers.io()).subscribe()
                            }
                        } else {
                            ToastUtils.show(this@HomeActivity, result.info)
                        }
                    }

                    override fun onFailure(t: Throwable?) {
                        super.onFailure(t)
                    }
                });
    }

    private fun reqPickingOrder(str: String) {
        if (!SystemUtils.checkNet(this) || !SystemUtils.isNetworkAvailable(this)) {
            ToastUtils.show(this, "网络不可用")
            return
        }
        pickId = str
        var params = ApiRequest(this)
        params.put("PickID", str)
        params.put("WID", wid)
        params.put("OpAreaID", opAreaID)

        getService().GetPickingOrder(params.urlParams)
                .compose(RxSchedulers.compose(this, true))
                .subscribe(object : BaseObserver<OrderBean>() {
                    override fun onResponse(result: ApiResponse<OrderBean>?) {
                        if(result!!.isSuccessful) {
                            ToastUtils.show(this@HomeActivity, "请求成功")
                            if (result!!.data != null) {
                                orderBean = result!!.data
                                if (orderBean != null) {
                                    for (item in orderBean!!.pickingOrderPackingList) {
                                        item.editCunt = item.qty
                                    }
                                    visibilityContent()
                                }
                            }
                        } else {
                            if (result.isAuthenticationFailed) {
                                ToastUtils.show(this@HomeActivity, getString(R.string.authentication_failed))
                                reLogin()
                            } else {
                                tvTips.visibility = View.VISIBLE
                                orderLl.visibility = View.INVISIBLE
                                ToastUtils.show(this@HomeActivity, result.info)
                            }
                        }
                    }

                    override fun onFailure(t: Throwable?) {
                        super.onFailure(t)
                    }
                });
    }

    private fun visibilityContent() {
        tvTips.visibility = View.INVISIBLE
        orderLl.visibility = View.VISIBLE
        deliverCodeTv.text = orderBean!!.sendCardNo
        storeIDTv.text = orderBean!!.storeNo + orderBean!!.storeName
        pickLineTv.text = "拣货流水线：" + orderBean!!.pGroupName
        shelfAreaTv.text = "待装区：" + orderBean!!.preAssembleCode
        deliverTimeTv.text = getDateString()
        pickIdTv.text = pickId

       if (orderBean!!.isPrintedDelivery) {
           printBtn.visibility = View.GONE
       } else {
           printBtn.visibility = View.VISIBLE
       }
        adapter = object : RecyclerAdapter<PackingBoxBean>(this, R.layout.item_box_view) {
            override fun convert(helper: RecyclerAdapterHelper?, item: PackingBoxBean?) {
                helper!!.setIsRecyclable(false)
                val mCetGoodCountedit = helper!!.getView<CountEditText>(R.id.cetGoodCountedit)
                if (orderBean!!.isPrintedDelivery) { // 已打印配送单
                    helper!!.setVisible(R.id.cetGoodCountedit, View.GONE)
                    helper.setText(R.id.pickQtyTv, item!!.packingBoxName + "：" + item.qty)
                } else {// 未打印过配送单
                    helper!!.setVisible(R.id.cetGoodCountedit, View.VISIBLE)
                    helper.setText(R.id.pickQtyTv, item!!.packingBoxName + "：")
                    mCetGoodCountedit.count = item.editCunt
                }
                mCetGoodCountedit.setOnCountChangeListener(object : CountEditText.onCountChangeListener {
                    override fun onCountAdd(count: Double) {
                        item.editCunt = count.toInt()
                    }

                    override fun onCountSub(count: Double) {
                        item.editCunt = count.toInt()
                    }

                    override fun onCountEdit(count: Double) {
                        item.editCunt = count.toInt()
                    }
                })

            }
        }

        localPackingBoxList = MyApplication.getInstance().packingBoxBean
        if (localPackingBoxList == null) {
            ToastUtils.show(this@HomeActivity, "请在用户中心同步周转箱数据")
        } else {
            if (orderBean!!.pickStatus.equals("3")) {
                val pickingOrderPackingList = orderBean!!.pickingOrderPackingList as List<PackingBoxBean>
                syncPackingBoxList(pickingOrderPackingList)
                adapter!!.addAll(pickingOrderPackingList)
                lvBox.setAdapter(adapter)
            } else {
                if (localPackingBoxList != null) {
                    for (item in localPackingBoxList!!) {
                        item.editCunt = 0
                    }
                    adapter!!.addAll(localPackingBoxList)
                    lvBox.setAdapter(adapter)
                } else {
                    ToastUtils.show(this@HomeActivity, "请在用户中心同步周转箱数据")
                }
            }
        }

    }

    private fun syncPackingBoxList(pickingOrderPackingList : List<PackingBoxBean>) {
        val localPackingBoxListHashMap = HashMap<String, PackingBoxBean>()
        if (null != localPackingBoxList) {
            for (localPackingBox in localPackingBoxList!!) {
                localPackingBoxListHashMap.put(localPackingBox.packingBoxCode, localPackingBox)
            }
        }

        if (localPackingBoxListHashMap.size > 0) {
            for (item in pickingOrderPackingList) {
                val localProduct = localPackingBoxListHashMap.get(item.packingBoxCode)
                if (null != localProduct) {
                    item.topicIcon = localProduct.topicIcon
                }
            }
        }
    }

    fun getDateString(): String {
       val source = orderBean!!.deliveryTime //原文本
        //正则式，在（三位数-八位数）的格式前后可以有任意文本
        val pattern = ".*(\\d{4}-\\d{2}-\\d{2}).*"  // ".*(\\d{3}-\\d{8}).*"
        //用正则式匹配文本获取匹配器
        val matcher = Pattern.compile(pattern).matcher(source)
        while(matcher.find()) {
            group = matcher.group(1)
        }
        return group
    }

    override fun onResume() {
        var helper = SharedPreferencesHelper.getInstance(this@HomeActivity, Config.PREFS_NAME)
        var from = helper.getString(Config.KEY_FROM, "")
        if (from.equals("login")) {
            orderLl.visibility = View.INVISIBLE
            tvTips.visibility = View.VISIBLE
            helper.putValue(Config.KEY_FROM, "mine")
        } else if (from.equals("mine") && orderLl.visibility == View.VISIBLE) {
        } else if (orderLl.visibility == View.INVISIBLE) {
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        //this.unregisterReceiver(PrinterStatusBroadcastReceiver)
        if (conn != null) {
            unbindService(conn) // unBindService
        }
        if (bluetoothService!!.isDiscovering()) {
            bluetoothService!!.cancelDevices()
        }
        bluetoothService!!.unregisterReceiver()
        unregisterReceiver(mBroadcastReceiver)
        mGpService!!.closePort(mPrinterIndex)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitConfirmDialog()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onSuccessEvent(event: BarcodeReadEvent) {
        val barcodeData = event.barcodeData
        if (!TextUtils.isEmpty(barcodeData)) {
            //TODO: 先在缓存中找 没有再请求接口

            reqPickingOrder(barcodeData)
        }
    }

    override fun onSuccessEventStr(event: String) {
        if (!TextUtils.isEmpty(event)) {
            //TODO: 先在缓存中找 没有再请求接口
            reqPickingOrder(event)
        }
    }

    private fun initData() {
        connection()
        initBtPrinter()

        MyApplication.getInstance().syncCaseData(this)
    }

    private fun ifOpenBluetooth() {
        if (bluetoothService!!.isOpen()) {
            var macAddress = SharedPreferencesHelper.getInstance(this, GlobelDefines.PREFS_NAME).getString(Config.KEY_BT_MAC, "")
            if (bluetoothService!!.checkBluetoothAddress(macAddress)) {
                val btDevice = bluetoothService!!.getRemoteDevice(macAddress)
                if (btDevice.bondState == BluetoothDevice.BOND_NONE) {
                    try {
                        val createBondMethod = BluetoothDevice::class.java.getMethod("createBond")
                        createBondMethod.invoke(btDevice)
                    } catch (e: Exception) {
                        ToastHelper.toastShort(this, "配对失败")
                    }

                } else if (btDevice.bondState == BluetoothDevice.BOND_BONDED) {
                    var status = mGpService!!.getPrinterConnectStatus(mPrinterId)
                    when (status) {
                        GpDevice.STATE_NONE -> connectOrDisConnectToDevice()
                        GpDevice.STATE_CONNECTED -> {
                            SetPackQty()
                        }
                    }
                }
            } else {
                ToastHelper.toastShort(this, "无效的蓝牙MAC地址，请重新配置")
                val intent = Intent(this, SearchBluetoothActivity::class.java)
                startActivity(intent)
            }
        } else {
            ToastHelper.toastShort(this, "蓝牙未打开")
            bluetoothService!!.openBluetooth(this)
        }
    }

    private fun packageLabelContent(tsc : LabelCommand, index : Int) {
        tsc.addText(40, 20, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_3, LabelCommand.FONTMUL.MUL_3,
                orderBean!!.sendCardNo)
        tsc.addText(40, 110, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "提货：" + getDateString())
        tsc.addText(330, 110, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "待装区：" + orderBean!!.preAssembleCode)
        if(orderBean!!.storeName.length <= 13) {
            tsc.addText(40, 140, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                    orderBean!!.storeNo.toString() + orderBean!!.storeName)
        } else {
            tsc.addText(40, 140, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                    orderBean!!.storeNo.toString() + orderBean!!.storeName.substring(0, 13))
            tsc.addText(40, 170, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                    orderBean!!.storeNo.toString() + orderBean!!.storeName.substring(13, orderBean!!.storeName.length))
        }
        //获取当前时间
        var simpleDateFormat = SimpleDateFormat("yyyy/MM/dd/ HH:mm")// HH:mm:ss
        val date = Date(System.currentTimeMillis())
        var ShelfAreaNames = ""
        for (i in orderBean!!.shelfAreaNameList.indices) {
            if (orderBean!!.shelfAreaNameList[i].equals(orderBean!!.shelfAreaName)) {
                continue
            }
            ShelfAreaNames += orderBean!!.shelfAreaNameList[i]
            if(i < orderBean!!.shelfAreaNameList.size -1) {
                ShelfAreaNames += "/"
            }
        }
        tsc.addText(40, 200, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "包含：("+ orderBean!!.shelfAreaName + ") " + ShelfAreaNames)
        tsc.addText(40, 230, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "打印：" + simpleDateFormat.format(date))
        if (mTotalCopies < 10 ) {
            tsc.addText(380, 230, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                    index.toString() + "/" + mTotalCopies)
        } else if (index < 10){
            tsc.addText(355, 230, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                    index.toString() + "/" + mTotalCopies)
        } else {
            tsc.addText(330, 230, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                    index.toString() + "/" + mTotalCopies)
        }

        tsc.addPrint(1,  1) // 打印标签
        tsc.addCls()// 清除打印缓冲区
    }

    private fun sendLabelWithResponse() {
        val tsc = LabelCommand()
        tsc.addSize(60, 40) // 设置标签尺寸，按照实际尺寸设置
        tsc.addGap(10) // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.NORMAL)// 设置打印方向
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        tsc.addReference(0, 0)// 设置原点坐标
        tsc.addTear(EscCommand.ENABLE.ON) // 撕纸模式开启
        tsc.addCls()// 清除打印缓冲区

        var i:Int = 1
        while (i <= mTotalCopies) {
            packageLabelContent(tsc, i)
            i++
        }

        tsc.addSound(2, 100) // 打印标签后 蜂鸣器响
        val datas = tsc.command // 发送数据
        val bytes = GpUtils.ByteTo_byte(datas)
        val str = Base64.encodeToString(bytes, Base64.DEFAULT)

        val rel: Int
        try {
            rel = mGpService!!.sendLabelCommand(mPrinterIndex, str)
            val r = GpCom.ERROR_CODE.values()[rel]
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Observable.create(ObservableOnSubscribe<Any> {
                    Toast.makeText(applicationContext, GpCom.getErrorText(r), Toast.LENGTH_SHORT).show()
                }).subscribeOn(AndroidSchedulers.mainThread()).subscribe()
            } else {
                Observable.create(ObservableOnSubscribe<Any> {
                    tvTips.visibility = View.VISIBLE
                    orderLl.visibility = View.INVISIBLE
                }).subscribeOn(AndroidSchedulers.mainThread()).subscribe()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    private fun initBtPrinter() {
        bluetoothService = PrintHelper.getBluetoothService(this)
        bluetoothService!!.registerReceiver()
        bluetoothService!!.setBluetoothLisenter(object : OnBluetoothLisenter {
            override fun onBluetoothStateChanged(isOpened: Boolean) {
                if (isOpened) {
                    initPrintStatus()
                } else {
                    printBtn.text = "打开蓝牙"
                }
            }

            override fun onBluetoothDiscoveryFinished(bondDevices: List<BluetoothDevice>, unbondDevices: List<BluetoothDevice>) {

            }

            override fun onBluetoothDiscoveryFound(bondDevices: List<BluetoothDevice>, unbondDevices: List<BluetoothDevice>) {

            }

            override fun onBluetoothDiscoveryStarted() {

            }

            override fun onBluetoothBondStateChanged(device: BluetoothDevice, isSuccess: Boolean) {
            }
        })

        if (bluetoothService!!.isOpen()) {
            initPrintStatus()
        } else {
            printBtn.text = "打开蓝牙"
        }
    }

    private fun initPrintStatus() {
        var status = mGpService?.getPrinterConnectStatus(mPrinterId)
        when (status) {
            GpDevice.STATE_NONE -> printBtn.text = "建立连接"
            GpDevice.STATE_CONNECTED -> printBtn.text = "开始打印"
        }
    }

    /**
     * 绑定打印服务
     */
    private fun connection() {
        conn = PrinterServiceConnection()
        val intent = Intent(this, GpPrintService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE) // bindService
    }


    /**
     * 连接设备
     */
    internal fun connectOrDisConnectToDevice() {
        var rel = 0
        var macAddress = SharedPreferencesHelper.getInstance(this@HomeActivity, GlobelDefines.PREFS_NAME).getString(Config.KEY_BT_MAC, "")
        if (!TextUtils.isEmpty(macAddress)) {
            try {
                rel = mGpService!!.openPort(mPrinterId, PortParameters.BLUETOOTH,
                        macAddress, 0)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            val r = GpCom.ERROR_CODE.values()[rel]
            Log.e(DEBUG_TAG, "result :" + r.toString())
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
//                    mPortParam!!.setPortOpenState(true)
                } else {
                    messageBox(GpCom.getErrorText(r))
                }
            }
        } else {
            Log.d(DEBUG_TAG, "DisconnectToDevice ")
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d("TAG", action)
            // GpCom.ACTION_DEVICE_REAL_STATUS 为广播的IntentFilter
            if (action == GpCom.ACTION_DEVICE_REAL_STATUS) {

                // 业务逻辑的请求码，对应哪里查询做什么操作
                val requestCode = intent.getIntExtra(GpCom.EXTRA_PRINTER_REQUEST_CODE, -1)
                // 判断请求码，是则进行业务操作
                if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

                    val status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16)
                    var str: String
                    if (status == GpCom.STATE_NO_ERR) {
                        str = "打印机正常"
                    } else {
                        str = "打印机 "
                        if ((status and GpCom.STATE_OFFLINE).toByte() > 0) {
                            str += "脱机"
                        }
                        if ((status and GpCom.STATE_PAPER_ERR).toByte() > 0) {
                            str += "缺纸"
                        }
                        if ((status and GpCom.STATE_COVER_OPEN).toByte() > 0) {
                            str += "打印机开盖"
                        }
                        if ((status and GpCom.STATE_ERR_OCCURS).toByte() > 0) {
                            str += "打印机出错"
                        }
                        if ((status and GpCom.STATE_TIMES_OUT).toByte() > 0) {
                            str += "查询超时"
                        }
                    }

                    Toast.makeText(applicationContext, "打印机：$mPrinterIndex 状态：$str", Toast.LENGTH_SHORT)
                            .show()
                } else if (requestCode == REQUEST_PRINT_LABEL) {
                    val status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16)
                    if (status == GpCom.STATE_NO_ERR) {
                    } else {
                        Toast.makeText(this@HomeActivity, "query printer status error", Toast.LENGTH_SHORT).show()
                    }
                } else if (requestCode == REQUEST_PRINT_RECEIPT) {
                    val status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16)
                    if (status == GpCom.STATE_NO_ERR) {
//                        sendReceipt()
                    } else {
                        Toast.makeText(this@HomeActivity, "query printer status error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (action == GpCom.ACTION_CONNECT_STATUS) {
                val type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0)
                if (type == GpDevice.STATE_CONNECTED) {
                    printBtn.text = "开始打印"
                    printBtn.isEnabled = true
                } else if (type == GpDevice.STATE_CONNECTING) {
                    printBtn.text = "正在连接..."
                    printBtn.isEnabled = false
                }  else if (type == GpDevice.STATE_NONE) {
                    printBtn.text = "建立连接"
                    printBtn.isEnabled = true
                } else if (type == GpDevice.STATE_INVALID_PRINTER) {
                    ToastUtils.show(this@HomeActivity, "无效的打印机")
                    printBtn.text = "建立连接"
                    printBtn.isEnabled = true
                }
            } else if (action == GpCom.ACTION_LABEL_RESPONSE) {
                /**
                 * 这里的d的内容根据RESPONSE_MODE去判断返回的内容去判断是否成功，具体可以查看标签编程手册SET
                 * RESPONSE指令
                 * 该sample中实现的是发一张就返回一次,这里返回的是{00,00001}。这里的对应{Status,######,ID}
                 * 所以我们需要取出STATUS
                 */
                /* 使用一次性发送多张标签内容，替换多次发送单张标签内容
                val data = intent.getByteArrayExtra(GpCom.EXTRA_PRINTER_LABEL_RESPONSE)
                val cnt = intent.getIntExtra(GpCom.EXTRA_PRINTER_LABEL_RESPONSE_CNT, 1)
                val d = String(data, 0, cnt)
                Log.d("LABEL RESPONSE", d)

                if (++mCurrentCopy <= mTotalCopies && d[1].toInt() == 0x00) {
                    sendLabelWithResponse(mCurrentCopy)
                }
                Log.d("LABEL RESPONSE", "" + mTotalCopies)
                */
            }
        }
    }

    private fun messageBox(err: String) {
        Toast.makeText(applicationContext,
                err, Toast.LENGTH_SHORT).show()
    }

    private fun showExitConfirmDialog() {
        val materialDialog = MaterialDialog(this)
        materialDialog.setMessage("是否退出应用？")
        materialDialog.setPositiveButton(R.string.confirm, {
            materialDialog.dismiss()
            mGpService!!.closePort(mPrinterId)

            finish()
            System.exit(0)
        })

        materialDialog.setNegativeButton(R.string.cancel, {
            materialDialog.dismiss()
        })

        materialDialog.show()
    }
}