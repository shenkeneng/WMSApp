package com.frxs.ktprefer.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.frxs.WMS.MyApplication
import com.frxs.WMS.comms.Config
import com.frxs.WMS.rest.service.ApiService
import com.frxs.core.base.BaseActivity
import com.frxs.core.utils.EasyPermissionsEx

/**
 * Created by Chentie on 2018/6/5.
 */
abstract class MyBaseActivity : BaseActivity() {

    companion object {
        private val MY_PERMISSIONS_REQUEST_WES = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this !is SpaceActivity) {
            // 判断当前用户是否允许存储权限//android.permission.READ_EXTERNAL_STORAGE
            if (EasyPermissionsEx.hasPermissions(this, *arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE))) {
                // 允许 - 执行更新方法
                if (MyApplication.getInstance().isNeedCheckUpgrade) {
                    MyApplication.getInstance().prepare4Update(this, false)
                }
            } else {
                // 不允许 - 弹窗提示用户是否允许放开权限
                EasyPermissionsEx.executePermissionsRequest(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE), MY_PERMISSIONS_REQUEST_WES)
            }
        }

    }

    fun reLogin() {
        MyApplication.getInstance().logout()
        gotoActivity(HelloktActivity::class.java, true)
    }

    val token: String
        get() {
            val userInfo = MyApplication.getInstance().userInfo
            return userInfo?.token ?: ""
        }

    val userAccount: String
        get() {
            val userInfo = MyApplication.getInstance().userInfo
            return userInfo?.userAccount ?: ""
        }

    val userID: Int
        get() {
            val userInfo = MyApplication.getInstance().userInfo
            return if (null != userInfo) {
                userInfo.empID
            } else {
                0
            }
        }

    val userName: String
        get() {
            val userInfo = MyApplication.getInstance().userInfo
            return if (null != userInfo) {
                userInfo.empName
            } else {
                ""
            }
        }

    val wid: Int
        get() {
            val userInfo = MyApplication.getInstance().userInfo
            return if (null != userInfo) {
                userInfo.wid
            } else {
                0
            }
//            return userInfo?.wid
        }

    val subWID: String
        get() {
            val userInfo = MyApplication.getInstance().userInfo

            return userInfo?.wid?.toString() ?: ""
        }

    val opAreaID: Int
        get() {
            val userInfo = MyApplication.getInstance().userInfo
            return if (null != userInfo) {
                userInfo.opAreaID
            } else {
                0
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            MY_PERMISSIONS_REQUEST_WES -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (MyApplication.getInstance().isNeedCheckUpgrade) {
                        MyApplication.getInstance().prepare4Update(this, false)
                    }
                } else {
                    if (!EasyPermissionsEx.somePermissionPermanentlyDenied(this, *permissions)) {
                        EasyPermissionsEx.goSettings2PermissionsDialog(this, "需要文件存储权限来下载更新的内容,但是该权限被禁止,你可以到设置中更改")
                    }
                }
            }
        }
    }

    override fun gotoActivityForResult(clz: Class<*>?, isCloseCurrentActivity: Boolean, ex: Bundle?, requestCode: Int) {
        val intent = Intent(this, clz)
        if (ex != null) {
            intent.putExtras(ex)
        }
        startActivityForResult(intent, requestCode)
        if (isCloseCurrentActivity) {
            finish()
        }
    }

    fun getService() : ApiService<*> {
        return MyApplication.getRestClient(Config.TYPE_BASE).getApiService();
    }
}