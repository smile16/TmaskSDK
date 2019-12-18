package com.zkxltech.marksdk.view

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.lang.Exception

/**
 *创建作者       : albus
 *创建时间       : 2019/11/12
 *Fuction(类描述):NFC扫描基类
 */
open class BaseNFCActivity : AppCompatActivity() {
    var isNull = true//设备是否具有NFC功能
    var isEnabled = true //设备的NFC功能是否开启
    private lateinit var mTag: Tag
    var mfc: NfcA?=null
    lateinit var nfcAdapter: NfcAdapter
    lateinit var mPendingIntent: PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            isEnabled = false
            isNull = true
        } else {
            val enabled = nfcAdapter?.isEnabled
            if (!enabled) {
                //设备已开启NFC功能
                isEnabled = false
                isNull = false
            }
        }

        mPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )

    }

    //当启动模式是singTop时
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        mTag = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
        if (mTag == null) {
            return
        }
        //获得标签所支持的数据格式
        val techList = mTag.techList
        if (techList == null) {
            return
        }
        var hasNfcA = false
        techList.forEach {
            if (it.indexOf("NfcA") > 0) {
                hasNfcA = true
                return@forEach
            }
        }
        if (!hasNfcA) {
            return
        }
        mfc = NfcA.get(mTag)
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            //打开前台发布系统 使页面优于其它页面处理NFC
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }


}