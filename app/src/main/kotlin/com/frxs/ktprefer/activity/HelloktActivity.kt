package com.frxs.ktprefer.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import com.frxs.WMS.R
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.frxs.WMS.MyApplication
import com.frxs.WMS.comms.Config
import com.frxs.WMS.model.UserInfo
import com.frxs.WMS.rest.model.AjaxParams
import com.frxs.WMS.rest.model.ApiResponse
import com.frxs.WMS.rest.service.rxjava.BaseObserver
import com.frxs.WMS.rest.service.rxjava.RxSchedulers
import com.frxs.core.utils.InputUtils
import com.frxs.core.utils.SharedPreferencesHelper
import com.frxs.core.utils.SystemUtils
import com.frxs.core.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_title.*


/**
 * <pre>
 * author : ewu
 * e-mail : xxx@xx
 * time   : 2017/05/25
 * desc   : xxxx描述
 * version: 1.0
</pre> *
 */
class HelloktActivity : MyBaseActivity() {

    private var keyDownNum = 0
    private var environments : Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        initEvent()
    }

    private fun initView() {
        actionBackTv.visibility = View.INVISIBLE
        titleTv.text = "登录"
        userNameLayout!!.hint = resources.getString(R.string.account)
        passwordLayout!!.hint = resources.getString(R.string.password)
        val account = MyApplication.getInstance().userAccount
        if (!TextUtils.isEmpty(account)) {
            userNameEt!!.setText(account)
            userNameEt!!.setSelection(userNameEt!!.length())
        }

        initEnvironment()
    }

    private fun initEnvironment() {
        environments = resources.getStringArray(R.array.run_environments)
        for (i in environments!!.indices) {
            environments!![i] = String.format(environments!![i], Config.getBaseUrl(Config.TYPE_BASE, i))
        }
    }

    private fun initEvent() {
        loginBtn.setOnClickListener({
            clickLoginBtn();
        })

        selectEnvironment.setOnClickListener({
            keyDownNum += 1
            if (keyDownNum == 9) {
                ToastUtils.showLongToast(this@HelloktActivity, "再点击1次进入环境选择模式");
            }

            if (keyDownNum == 10) {
                ToastUtils.showLongToast(this@HelloktActivity, "已进入环境选择模式");
                var dialog = AlertDialog.Builder(this@HelloktActivity)
                var spEnv = MyApplication.getInstance().getEnvironment()
                var env = if (spEnv < environments!!.size) environments!![spEnv] else ""
                dialog.setTitle(String.format(resources.getString(R.string.tips_environment), env ))
                dialog.setCancelable(false)
                dialog.setItems(environments, DialogInterface.OnClickListener { dialog, which ->
                    if (spEnv == which) {
                        return@OnClickListener
                    }

                    if (which != 0) {
                        var verifyMasterDialog = AlertDialog.Builder(this@HelloktActivity).create()
                        var contentView = LayoutInflater.from(this@HelloktActivity).inflate(R.layout.dialog_evironments, null)
                        val pswEt = contentView.findViewById<EditText>(R.id.passwordEtDialog)
                        contentView.findViewById<Button>(R.id.confirmBtnDialog).setOnClickListener(object : View.OnClickListener{
                            override fun onClick(v: View?) {
                                if (TextUtils.isEmpty(pswEt.text.toString().trim())) {
                                    ToastUtils.show(this@HelloktActivity, "密码不能为空！")
                                    return
                                }

                                if (pswEt.text.toString().trim() { it <= ' ' } != getString(R.string.str_psw)) {
                                    ToastUtils.show(this@HelloktActivity, "密码错误！")
                                    return
                                }
                                MyApplication.getInstance().environment = which
                                MyApplication.getInstance().resetRestClient()
                                verifyMasterDialog.dismiss()
                            }
                        })

                        contentView.findViewById<Button>(R.id.cancelBtnDialog).setOnClickListener({
                                verifyMasterDialog.dismiss()
                        })
                        verifyMasterDialog.setView(contentView)
                        verifyMasterDialog.show()
                    } else {
                        MyApplication.getInstance().environment = which
                        MyApplication.getInstance().resetRestClient()
                    }
            })
                dialog.setNegativeButton(getString(R.string.cancel), object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog!!.dismiss()
                    }
                })
                dialog.show()
                keyDownNum = 0
            }
        })
    }

    private fun clickLoginBtn() {
            val username = userNameLayout!!.editText!!.text.toString()
            val password = passwordLayout!!.editText!!.text.toString();
            if (TextUtils.isEmpty(username)) {
                userNameLayout!!.error = getString(R.string.tips_null_account)
                userNameEt!!.requestFocus()
            } else if (TextUtils.isEmpty(password)) {
                passwordLayout!!.error = getString(R.string.tips_null_password)
                passwordEt!!.requestFocus()
            } else {
                if (InputUtils.isNumericOrLetter(password)) {
                    userNameLayout!!.isErrorEnabled = false
                    passwordLayout!!.isErrorEnabled = false
                    requestLogin(username, password)
                } else {
                    passwordLayout!!.error = getString(R.string.tips_input_limit)
                    passwordEt!!.requestFocus()
                }
            }
    }

    private fun requestLogin(username: String, password: String) {
        if (!SystemUtils.checkNet(this) || !SystemUtils.isNetworkAvailable(this)) {
            ToastUtils.show(this, "网络不可用")
            return
        }
        var params = AjaxParams()
        params.put("UserAccount", username)
        params.put("Password", password)
        params.put("UserType", 3)//value="1">拣货员; value="2">配送员; value="5">收货员; value="3">复核员;
        getService().UserLogin(params.urlParams)
                .compose(RxSchedulers.compose(this, true))
                .subscribe(object : BaseObserver<UserInfo>() {
                    override fun onResponse(result: ApiResponse<UserInfo>?) {
                        if(result!!.isSuccessful) {
                            ToastUtils.show(this@HelloktActivity, "登录成功")
                            var application = MyApplication.getInstance()
                            application.userAccount = username
                            var userInfo: UserInfo = result.data
                            if(userInfo != null) {
                                application.userInfo = userInfo
                                var helper = SharedPreferencesHelper.getInstance(this@HelloktActivity, Config.PREFS_NAME)
                                helper.putValue(Config.KEY_FROM, "login")
                                gotoActivity(HomeActivity::class.java, true)
                            }
                        } else {
                            ToastUtils.show(this@HelloktActivity, result.info)
                        }
                    }

                    override fun onFailure(t: Throwable?) {
                        super.onFailure(t)
                    }
                });
    }

}
