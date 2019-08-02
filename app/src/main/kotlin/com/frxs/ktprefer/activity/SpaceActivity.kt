package com.frxs.ktprefer.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import com.frxs.WMS.MyApplication
import com.frxs.WMS.R
import com.frxs.ktprefer.activity.HelloktActivity
import com.frxs.ktprefer.activity.HomeActivity
import com.frxs.ktprefer.activity.MyBaseActivity

/**
 * Created by Chentie on 2017/10/17.
 */

class SpaceActivity : MyBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initView()
        initData()
    }

    private fun initData() {

        object : CountDownTimer(3000, 1500) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                var userInfo = MyApplication.getInstance().userInfo
                if (userInfo != null && userInfo.empID != 0) {
                    gotoActivity(HomeActivity::class.java)
                } else {
                    gotoActivity(HelloktActivity::class.java)
                }

                this@SpaceActivity.finish()
                overridePendingTransition(R.anim.just_fade_in, R.anim.just_fade_out)
            }
        }.start()
    }

    private fun initView() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


}