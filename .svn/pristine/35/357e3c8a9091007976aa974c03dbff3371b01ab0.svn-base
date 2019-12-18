package com.zkxltech.marksdk.Utils

import android.nfc.tech.NfcA
import java.io.IOException
import java.lang.Exception

/**
 *创建作者       : albus
 *创建时间       : 2019/11/30
 *Fuction(类描述):
 */
class ConnectUtils {
    fun connectNfcA(delay: Int, tryauth: Int, timeout: Int,mfc:NfcA): Boolean {
        var tryauth = tryauth
        //delay timeout都是毫秒级的单位时间
        if (mfc == null) return false
        try {
            while (tryauth > 0) {
                tryauth--
                try {
                    mfc.close()

                    Thread.sleep(delay.toLong())
                    mfc.connect()
                    mfc.timeout = timeout
                } catch (e: IOException) {
                    return false
                }

                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**关闭NFC连接*/
    fun closeNFC(mfc:NfcA) {
        if (mfc != null) {
            try {
                mfc.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}