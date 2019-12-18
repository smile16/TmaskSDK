package com.zkxltech.marksdk.thread

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import java.util.*

/**
 *创建作者       : albus
 *创建时间       : 2019/12/6
 *Fuction(类描述):
 */
class DataThreadDispatch : Thread() {
    var mIsRunning:Boolean=false
    //装数据的集合
    var mMessageQueue= arrayListOf<String>()
    //同步锁块
    val ticked=Object()

    fun startThread(){
        //开启线程
        start()
        mIsRunning=true
    }

    fun shutDown(){
        //关闭线程
        mIsRunning=false
        mMessageQueue.clear()
        stop()
    }

    override fun run() {
        super.run()

    }

    //往消息队列添加消息
    fun addListData(data:String){
        sleep(5)
        if (!mIsRunning){
            return
        }
        synchronized(mMessageQueue){
            if (!TextUtils.isEmpty(data)){
                mMessageQueue.add(data)
            }
        }

        synchronized(ticked){
            ticked.notify()
        }
    }

    //从消息队列取出消息
    fun getListData():String?{
        sleep(5)
        if (!mIsRunning){
            return null
        }
        var data:String?=null
        synchronized(mMessageQueue){
            if (mMessageQueue.size>0){
                while (true) {
                    data = mMessageQueue.removeAt(0)
                    return data
                }
            }

        }
        synchronized(ticked){
            ticked.notify()
        }

        return data
    }

}