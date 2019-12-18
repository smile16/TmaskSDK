package com.zkxltech.marksdk.Utils

import android.util.Log
import com.zkxltech.marksdk.UpdateHelper

/**
 *创建作者       : albus
 *创建时间       : 2019/11/30
 *Fuction(类描述):
 */
object LogUtils {
    private val isDebug: Boolean = UpdateHelper.getIsLogPrint()
    private val TAG: String = "zkxl"

    /**
     *包装log.d日志
     */
    fun d(msg: String) {
        if (isDebug) {
            Log.d(TAG, msg)
        }
    }

    /**
     *包装log.e日志
     */
    fun e(msg: String) {
        if (isDebug) {
            Log.e(TAG, msg)
        }
    }

    /**
     * v类型的log.v日志
     */
    fun v(msg: String) {
        if (isDebug) {
            Log.v(TAG, msg)
        }
    }
}