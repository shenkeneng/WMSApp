package com.frxs.ktprefer.activity

import android.os.Bundle
import android.view.View
import com.frxs.WMS.MyApplication
import com.frxs.WMS.R
import com.frxs.core.utils.SystemUtils
import com.frxs.core.utils.ToastUtils
import com.frxs.core.widget.MaterialDialog
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.android.synthetic.main.view_title.*

/**
 * Created by Chentie on 2018/6/7.
 */
class MineActivity : MyBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_mine)

        initView()
        initEvent()
    }

    private fun initEvent() {
        syncCaseTv.setOnClickListener({
            MyApplication.getInstance().syncCaseData(this)
        })

        logoutBtn.setOnClickListener({
            logOutDialog()
        })

        actionBackTv.setOnClickListener({
            finish()
        })

        versionCode.setOnClickListener({
            if (SystemUtils.isNetworkAvailable(this)) {
                MyApplication.getInstance().prepare4Update(this, false);
            } else {
                ToastUtils.show(this, "网络异常，请检查网络是否连接")
            }
        })
    }

    private fun initView() {
        titleTv!!.setText("用户中心")
        userNameTv.text = userName
        var userinfo =  MyApplication.getInstance().userInfo
        if (userinfo != null) {
            wInfoTv.text = "区域：" + userinfo.opAreaName +"      仓库：" + userinfo.wName
        }
        versionCode.text = "版本更新：v" + SystemUtils.getAppVersion(this@MineActivity)
    }

    private fun logOutDialog() {
        var mDialog = MaterialDialog(this)
        mDialog.setMessage("确认退出登录？")
        mDialog.setPositiveButton("确定") {
                MyApplication.getInstance().logout()
                gotoActivity(HelloktActivity::class.java, true)
                mDialog.dismiss()
        }
        mDialog.setNegativeButton("取消") {
            mDialog.dismiss()
        }

        mDialog.show()
    }
}