package com.frxs.ktprefer.activity

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.frxs.WMS.MyApplication
import com.frxs.core.utils.ToastUtils
import com.frxs.ktprefer.listener.ScanListener
import com.honeywell.aidc.BarcodeFailureEvent
import com.honeywell.aidc.BarcodeReadEvent
import com.honeywell.aidc.BarcodeReader
import com.honeywell.aidc.ScannerUnavailableException
import com.obm.mylibrary.ScanEvent
import java.util.*

/**
 * Created by Chentie on 2018/6/25.
 */
abstract class BaseScanActivity : MyBaseActivity() , BarcodeReader.BarcodeListener, ScanListener {
    protected var barcodeReader = null as BarcodeReader?
    private var scanEvent = null as ScanEvent?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val brand = Build.BRAND
        if (!TextUtils.isEmpty(brand) && brand.equals("Honeywell")) {
            initBarcodeReader()
        }
    }

    fun initBarcodeReader() {
        barcodeReader = MyApplication.getInstance().barcodeReader
        if (needHideQrcodeScanView()) {

            var properties = HashMap<String, Any>()
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true)
            barcodeReader!!.setProperties(properties)
        }
    }

    override fun onBarcodeEvent(BarcodeReadEvent: BarcodeReadEvent?) {
        runOnUiThread{
            if (BarcodeReadEvent != null) {
                this@BaseScanActivity.onSuccessEvent(BarcodeReadEvent)
            }
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
    }

    override fun onResume() {
        super.onResume()
        val brand = Build.BRAND
        if (!TextUtils.isEmpty(brand) && brand.equals("Honeywell")) {
            if (null != barcodeReader) {
                barcodeReader!!.addBarcodeListener(this)
                try {
                    barcodeReader!!.claim()
                } catch (e : ScannerUnavailableException) {
                    e.printStackTrace()
                    ToastUtils.show(this, "Scanner unavailable")
                }
            }
        } else if (!TextUtils.isEmpty(brand) && brand.equals("A8")) {
            scanEvent = ScanEvent(this, ScanEvent.OnScanListener {
                barcode -> this@BaseScanActivity.onSuccessEventStr(barcode)
            })
        }
    }

    override fun onPause() {
        super.onPause()
        val brand = Build.BRAND
        if (!TextUtils.isEmpty(brand) && brand.equals("Honeywell")) {
            if (barcodeReader != null) {
                barcodeReader!!.release()
                barcodeReader!!.removeBarcodeListener(this)
            }
        } else if (!TextUtils.isEmpty(brand) && brand.equals("A8")) {
            scanEvent!!.scan_stop()
            scanEvent = null
        }
    }

    fun needHideQrcodeScanView(): Boolean {
        val brand = Build.BRAND
        if (!TextUtils.isEmpty(brand) && brand.equals("Honeywell")) {
            return true
        } else if (!TextUtils.isEmpty(brand) && brand.equals("A8")) {
            return true
        } else {
            return false;
        }
    }
}