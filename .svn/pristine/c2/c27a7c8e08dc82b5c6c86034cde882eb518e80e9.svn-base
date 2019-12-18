package com.zkxltech.marksdk.Utils

import android.nfc.tech.NfcA
import com.zkxltech.marksdk.UpdateHelper
import java.lang.Exception

/**
 *创建作者       : albus
 *创建时间       : 2019/11/21
 *Fuction(类描述):
 */
class ReadNFCData {
    fun readNfcA(byte: Byte, mfc: NfcA): ByteArray? {
        val cmd = ByteArray(2)
        cmd[0] = 0x30
        cmd[1] = byte
        if (mfc == null) return null
        try {
            val result = mfc.transceive(cmd)
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e("NFC连接被断开")
            UpdateHelper.dataThreadDispatch.addListData("NFC连接被断开")
        }
        return null
    }
}