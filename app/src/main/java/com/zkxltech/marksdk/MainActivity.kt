package com.zkxltech.marksdk

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.tech.NfcA
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.duke.dfileselector.activity.DefaultSelectorActivity
import com.zkxltech.marksdk.`interface`.NFCCommunicationInterface
import com.zkxltech.marksdk.view.BaseNFCActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.security.AccessController.getContext

class MainActivity : BaseNFCActivity(), NFCCommunicationInterface {
    override fun NFCCommunicationSucess() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun NFCCommunicationFail(message: String) {
        Log.e("yanchuang","NFCCommunicationFail=$message")
          }

    override fun NFCCommunicationAgo() {
        Log.e("yanchuang","开始写入数据之前需要做的操作")    }

    override fun NFCCommunicationProgress(progress: Int) {
        Log.e("yanchuang","progress=$progress")
    }

    override fun NFCCommunicationSucessResult(message: String) {
        Log.e("yanchuang","NFCCommunicationSucessResult=$message")
    }


    var toString: String =
        Environment.getExternalStorageDirectory().getPath() + "/xianyu/bwImage.bmp"
    var canWrite = false
    private var isRegister: Boolean = false
    private val intentFilter = IntentFilter(DefaultSelectorActivity.FILE_SELECT_ACTION)
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                return
            }
            if (DefaultSelectorActivity.FILE_SELECT_ACTION.equals(intent.action)) {
                val list = DefaultSelectorActivity.getDataFromIntent(intent)
                val s = list[0]
                if (!TextUtils.isEmpty(s)) {
                    toString = s
                    canWrite = true
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initListener()
    }

    private fun initListener() {
        bt_update_mark.setOnClickListener {
            //            DefaultSelectorActivity.startActivity(this)//包含广播
//            if (canWrite){
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )

            }else{
                if (toString!=null) {
                    UpdateHelper.upDateMarkUI(1, toString, mfc, 20, this)
                }else{
                    Log.e("albus","文件路径为空")
                }

            }
        }


    }

private fun initData() {
}

override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    mfc?.let { UpdateHelper.AysncUpdate(it,1) }
}

override fun onResume() {
    super.onResume()
    if (!isRegister) {
        registerReceiver(receiver, intentFilter)
        isRegister = true
    }
}

override fun onDestroy() {
    super.onDestroy()
    if (isRegister) {
        unregisterReceiver(receiver)
        isRegister = false
    }
}
}
